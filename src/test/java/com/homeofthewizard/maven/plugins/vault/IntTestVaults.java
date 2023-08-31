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

import io.github.jopenlibs.vault.VaultException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.homeofthewizard.maven.plugins.vault.client.VaultClient;
import com.homeofthewizard.maven.plugins.vault.config.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.randomPaths;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Provides integration tests for the {@link VaultClient} class.
 */
public class IntTestVaults {

  private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
  private static final String VAULT_HOST = System.getProperty("vault.host", "localhost");
  private static final String VAULT_PORT = System.getProperty("vault.port", "443");
  private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);
  private static final String VAULT_TOKEN = System.getProperty("vault.token");
  private static String githubTokenTag = GithubToken.class.getDeclaredFields()[0].getName();
  private static TreeMap map;
  static {
    map = new TreeMap<>();
    map.put(githubTokenTag,"token");
  }
  private static final Map<String, TreeMap> VAULT_GITHUB_AUTH = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, map);
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
   * Tests the {@link VaultClient#pull(List, Properties, OutputMethod)} and {@link VaultClient#push(List, Properties)} methods.
   *
   * @throws URISyntaxException if an exception is raised parsing the certificate
   */
  @Test
  public void testPushAndPull() throws URISyntaxException {
    Fixture.with(fixture -> {
      var client = VaultClient.create();
      try {
        client.push(fixture.servers, fixture.properties);
        Properties properties = new Properties();
        try {
          client.pull(fixture.servers, properties, OutputMethod.MavenProperties);
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
   * Tests the {@link VaultClient#authenticateIfNecessary(List, AuthenticationMethodProvider)}
   *
   * @throws URISyntaxException if an exception is raised parsing the certificate
   */
  @Test
  public void testAuthentication() throws URISyntaxException {
    Fixture.with(fixture -> {
      var client = VaultClient.create();
      try {
        client.authenticateIfNecessary(fixture.servers, fixture.authenticationMethodProvider);
      } catch (VaultException exception) {
        fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
      }
    });
  }

}
