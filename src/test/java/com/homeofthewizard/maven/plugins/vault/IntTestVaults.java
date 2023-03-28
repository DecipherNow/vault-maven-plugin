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

package com.homeofthewizard.maven.plugins.vault;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.randomPaths;

import com.bettercloud.vault.VaultException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.Path;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Provides integration tests for the {@link Vaults} class.
 */
public class IntTestVaults {

  private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
  private static final String VAULT_HOST = System.getProperty("vault.host", "localhost");
  private static final String VAULT_PORT = System.getProperty("vault.port", "443");
  private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);
  private static final String VAULT_TOKEN = System.getProperty("vault.token");
  private static final Map<String,String> VAULT_GITHUB_AUTH = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");

  private static class Fixture {

    private final AuthenticationMethodProvider authenticationMethodProvider;
    private final List<Server> servers;
    private final Properties properties;

    private Fixture() throws URISyntaxException {
      List<Path> paths = randomPaths(10, 10);
      File certificate = new File(VAULT_CERTIFICATE.toURI());
      boolean skipExecution = false;
      System.out.println(String.format("%s/%s", VAULT_SERVER, VAULT_TOKEN));
      this.servers = ImmutableList.of(new Server(VAULT_SERVER, VAULT_TOKEN, true, certificate, VAULT_GITHUB_AUTH, "", paths, skipExecution, 2));
      this.properties = new Properties();
      this.authenticationMethodProvider = new AuthenticationMethodFactory();
      this.servers.stream().forEach(server -> {
        server.getPaths().stream().forEach(path -> {
          path.getMappings().stream().forEach(mapping -> {
            this.properties.setProperty(mapping.getProperty(), UUID.randomUUID().toString());
          });
        });
      });
    }

    private static void with(Consumer<Fixture> test) throws URISyntaxException {
      test.accept(new Fixture());
    }

  }

  /**
   * Tests the {@link Vaults#pull(List, Properties)} and {@link Vaults#push(List, Properties)} methods.
   *
   * @throws URISyntaxException if an exception is raised parsing the certificate
   */
  @Test
  public void testPushAndPull() throws URISyntaxException {
    Fixture.with(fixture -> {
      var client = Vaults.create();
      try {
        client.push(fixture.servers, fixture.properties);
        Properties properties = new Properties();
        try {
          client.pull(fixture.servers, properties);
          assertTrue(Maps.difference(fixture.properties, properties).areEqual());
        } catch (VaultException exception) {
          fail(String.format("Unexpected exception while pulling to Vault: %s", exception.getMessage()));
        }
      } catch (VaultException exception) {
        fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
      }
    });
  }

  /**
   * Tests the {@link Vaults#authenticateIfNecessary(List<Server>, AuthenticationMethodProvider)} method.
   *
   * @throws URISyntaxException if an exception is raised parsing the certificate
   */
  @Test
  public void testAuthentication() throws URISyntaxException {
    Fixture.with(fixture -> {
      var client = Vaults.create();
      try {
        client.authenticateIfNecessary(fixture.servers, fixture.authenticationMethodProvider);
      } catch (VaultException exception) {
        fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
      }
    });
  }

}
