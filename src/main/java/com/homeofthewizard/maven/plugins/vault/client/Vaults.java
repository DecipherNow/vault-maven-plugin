package com.homeofthewizard.maven.plugins.vault.client;

import static com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory.methods;

import com.google.common.base.Strings;

import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.Mapping;
import com.homeofthewizard.maven.plugins.vault.config.OutputMethod;
import com.homeofthewizard.maven.plugins.vault.config.Path;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Properties;

/**
 * Provides implementations of operations from {@link VaultClient} to interact with a Vault Server.
 * Includes static methods for working {@link Vault}.
 */
final class Vaults implements VaultClient {

  private final VaultBackendProvider vaultBackendProvider;

  /**
   * Initializes a new instance of the {@link Vaults} class.
   */
  Vaults(VaultBackendProvider vaultBackendProvider) {
    this.vaultBackendProvider = vaultBackendProvider;
  }

  /**
   * Pulls secrets from one or more Vault servers and paths and updates a {@link Properties} instance with the values.
   *
   * @param servers      the servers
   * @param properties   the properties
   * @param outputMethod the output method
   * @throws VaultException if an exception is throw pulling the secrets
   */
  @Override
  public void pull(List<Server> servers, Properties properties, OutputMethod outputMethod) throws VaultException {
    for (Server server : servers) {
      if (server.isSkipExecution()) {
        continue;
      }
      Vault vault = vaultBackendProvider.vault(server.getUrl(), server.getToken(), server.getNamespace(),
              server.getSslVerify(), server.getSslCertificate(), server.getEngineVersion());
      for (Path path : server.getPaths()) {
        Map<String, String> secrets = get(vault, path.getName());
        for (Mapping mapping : path.getMappings()) {
          if (!secrets.containsKey(mapping.getKey())) {
            String message = String.format("No value found in path %s for key %s", path.getName(), mapping.getKey());
            throw new NoSuchElementException(message);
          }
          properties.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
          outputMethod.flush(properties, secrets, mapping);
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
  @Override
  public void push(List<Server> servers, Properties properties) throws VaultException {
    for (Server server : servers) {
      if (server.isSkipExecution()) {
        continue;
      }
      Vault vault = vaultBackendProvider.vault(server.getUrl(), server.getToken(), server.getNamespace(),
              server.getSslVerify(), server.getSslCertificate(), server.getEngineVersion());
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
   * @throws VaultException if an exception is throw authenticating
   */
  @Override
  public void authenticateIfNecessary(List<Server> servers, AuthenticationMethodProvider factory)
          throws VaultException {
    for (Server s : servers) {
      if (!Strings.isNullOrEmpty(s.getToken())) {
        return;
      } else if (!Objects.isNull(s.getAuthentication())) {
        factory.fromServer(s).login();
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

}
