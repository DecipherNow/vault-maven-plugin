/*
 * Copyright 2017 Decipher Technology Studios LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deciphernow.maven.plugins.vault;

import static com.deciphernow.maven.plugins.vault.config.Authentication.authenticationMethod;
import static com.deciphernow.maven.plugins.vault.config.Authentication.methods;

import com.google.common.base.Strings;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;


/**
 * Provides static methods for working with Vault.
 */
public final class Vaults {

  /**
   * Defines the timeout when opening a connection with Vault.
   */
  private static final int OPEN_TIMEOUT = 5;

  /**
   * Defines the timeout when reading data from Vault.
   */
  private static final int READ_TIMEOUT = 30;

  /**
   * Initializes a new instance of the {@link Vaults} class.
   */
  private Vaults() {}

  /**
   * Pulls secrets from one or more Vault servers and paths and updates a {@link Properties} instance with the values.
   *
   * @param servers the servers
   * @param properties the properties
   * @throws VaultException if an exception is throw pulling the secrets
   */
  public static void pull(List<Server> servers, Properties properties) throws VaultException {
    for (Server server : servers) {
      if (server.isSkipExecution()) {
        continue;
      }
      Vault vault = vault(server.getUrl(), server.getToken(), server.getNamespace(),
              server.getSslVerify(), server.getSslCertificate());
      for (Path path : server.getPaths()) {
        Map<String, String> secrets = get(vault, path.getName());
        for (Mapping mapping : path.getMappings()) {
          if (!secrets.containsKey(mapping.getKey())) {
            String message = String.format("No value found in path %s for key %s", path.getName(), mapping.getKey());
            throw new NoSuchElementException(message);
          }
          properties.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
        }
      }
    }
  }

  /**
   * Pushes secrets to one or more Vault servers and paths from a {@link Properties} instance.
   *
   * @param servers the servers
   * @param properties the properties
   * @throws VaultException if an exception is throw pushing the secrets
   */
  public static void push(List<Server> servers, Properties properties) throws VaultException {
    for (Server server : servers) {
      if (server.isSkipExecution()) {
        continue;
      }
      Vault vault = vault(server.getUrl(), server.getToken(), server.getNamespace(),
              server.getSslVerify(), server.getSslCertificate());
      for (Path path : server.getPaths()) {
        Map<String, String> secrets = exists(vault, path.getName()) ? get(vault, path.getName()) : new HashMap<>();
        for (Mapping mapping : path.getMappings()) {
          if (!properties.containsKey(mapping.getProperty())) {
            String message = String.format("No value found for property %s", mapping.getProperty());
            throw new NoSuchElementException(message);
          }
          secrets.put(mapping.getKey(), properties.getProperty(mapping.getProperty()));
        }
        set(vault, path.getName(), secrets);
      }
    }
  }

  /**
   * Authenticate to one or more Vault servers and paths from a {@link Properties} instance.
   *
   * @param servers the servers
   * @param properties the properties
   * @throws VaultException if an exception is throw pushing the secrets
   */
  public static void authenticateIfNecessary(List<Server> servers, Properties properties) throws VaultException {
    for (Server s : servers) {
      if (!Strings.isNullOrEmpty(s.getToken())) {
        return;
      } else if (!Objects.isNull(s.getAuthentication())) {
        authenticationMethod(s).login();
      } else {
        throw new VaultException("Either a Token of Authentication method must be provided !!\n"
                + "Put in your server configuration in the pom.xml:\n"
                + "<token>"
                + "YOUR_VAULT_TOKEN"
                + "</token>\n"
                + "or\n"
                + "<authentication>\n"
                + "  <AUTH_METHOD>__AUTH_CREDENTIALS__</AUTH_METHOD>\n"
                + "</authentication>\n"
                + "available authentication methods are: " + methods + "\n");
      }
    }
  }

  /**
   * Returns a value indicating whether a path exists.
   *
   * @param vault the vault
   * @param path the path
   * @return {@code true} if the path exists; otherwise, {@code false}
   * @throws VaultException if an exception is thrown connecting to vault
   */
  private static boolean exists(Vault vault, String path) throws VaultException {
    return !vault.logical().list(path).getData().isEmpty();
  }

  /**
   * Gets the secrets at a path.
   *
   * @param vault the vault
   * @param path the path
   * @return the secrets
   * @throws VaultException if an exception is thrown connecting to vault or the path does not exist
   */
  private static Map<String, String> get(Vault vault, String path) throws VaultException {
    return vault.logical().read(path).getData();
  }


  /**
   * Sets the secrets at a path.
   *
   * @param vault the vault
   * @param path the path
   * @param secrets the secrets
   * @return the data
   * @throws VaultException if an exception is thrown connecting to vault or the path does not exist
   */
  private static void set(Vault vault, String path, Map<String, String> secrets) throws VaultException {
    Map<String,Object> nameValuePairs = (Map) secrets;
    vault.logical().write(path, nameValuePairs);
  }

  /**
   * Returns a configured instance of the {@link Vault} class.
   *
   * @param server the server
   * @param token the token
   * @param sslCertificate the certificate file or null if not needed
   * @param sslVerify {@code true} if the connection should be verified; otherwise, {@code false}
   * @return the vault
   */
  private static Vault vault(String server,
                             String token,
                             String namespace,
                             boolean sslVerify,
                             File sslCertificate) throws VaultException {


    return new Vault(vaultConfig(server,token,namespace,sslVerify,sslCertificate));
  }


  /**
   * Returns a configured instance of the {@link VaultConfig} class.
   * @param server the server
   * @param token the token
   * @param namespace the namespace
   * @param sslVerify {@code true} if the connection should be verified; otherwise, {@code false}
   * @param sslCertificate the certificate file or null if not needed
   * @return the vaultConfig
   */
  public static VaultConfig vaultConfig(String server,
                                        String token,
                                        String namespace,
                                        boolean sslVerify,
                                        File sslCertificate) throws VaultException {
    SslConfig sslConfig = new SslConfig().verify(sslVerify);
    if (sslCertificate != null) {
      sslConfig.pemFile(sslCertificate);
    }
    VaultConfig vaultConfig = new VaultConfig()
            .address(server)
            .openTimeout(OPEN_TIMEOUT)
            .readTimeout(READ_TIMEOUT)
            .sslConfig(sslConfig)
            .token(token);
    if (!Strings.isNullOrEmpty(namespace)) {
      vaultConfig.nameSpace(namespace);
    }
    return vaultConfig;
  }

}
