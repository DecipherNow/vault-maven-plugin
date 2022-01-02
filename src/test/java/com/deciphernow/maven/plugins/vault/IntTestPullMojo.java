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

import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Authentication;
import com.deciphernow.maven.plugins.vault.config.Mapping;
import com.deciphernow.maven.plugins.vault.config.Path;
import com.deciphernow.maven.plugins.vault.config.Server;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class IntTestPullMojo {

  private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
  private static final String VAULT_HOST = "localhost"; //System.getProperty("vault.host", "localhost");
  private static final String VAULT_PORT = System.getProperty("vault.port", "443");
  private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);
  private static final String VAULT_TOKEN = System.getProperty("vault.token");
  private static final Map<String,String> VAULT_GITHUB_AUTH = Map.of(Authentication.GITHUB_TOKEN_TAG, "token");

  private static Mapping randomMapping() {
    return new Mapping(UUID.randomUUID().toString(), UUID.randomUUID().toString());
  }

  private static List<Mapping> randomMappings(int count) {
    return IntStream.range(0, count).mapToObj(i -> randomMapping()).collect(Collectors.toList());
  }

  private static Path randomPath(int mappingCount) {
    return new Path(String.format("secret/%s", UUID.randomUUID().toString()), randomMappings(mappingCount));
  }

  private static List<Path> randomPaths(int pathCount, int mappingCount) {
    return IntStream.range(0, pathCount).mapToObj(i -> randomPath(mappingCount)).collect(Collectors.toList());
  }

  private static class Fixture {

    private final List<Server> servers;
    private final Properties properties;

    private Fixture() throws URISyntaxException {
      List<Path> paths = randomPaths(10, 10);
      File certificate = new File(VAULT_CERTIFICATE.toURI());
      System.out.println(String.format("%s/%s", VAULT_SERVER, VAULT_TOKEN));
      this.servers = ImmutableList.of(new Server(VAULT_SERVER, VAULT_TOKEN, true, certificate, VAULT_GITHUB_AUTH, "", paths, false));
      this.properties = new Properties();
      this.servers.stream().forEach(server -> {
        server.getPaths().stream().forEach(path -> {
          path.getMappings().stream().forEach(mapping -> {
            this.properties.setProperty(mapping.getProperty(), UUID.randomUUID().toString());
          });
        });
      });
    }

    private static void with(Consumer<IntTestPullMojo.Fixture> test) throws URISyntaxException {
      test.accept(new IntTestPullMojo.Fixture());
    }

  }

  /**
   * Tests the {@link PullMojo#execute()} method.
   *
   * @throws URISyntaxException if an exception is raised parsing the certificate
   */
  @Test
  public void testExecute() throws URISyntaxException {
    IntTestPullMojo.Fixture.with(fixture -> {
      PullMojo mojo = new PullMojo();
      mojo.project = new MavenProject();
      mojo.servers = fixture.servers;
      mojo.skipExecution = false;
      try {
        Vaults.push(fixture.servers, fixture.properties);
        mojo.execute();
        assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
      } catch (MojoExecutionException exception) {
        fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
      } catch (VaultException exception) {
        fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
      }
    });
  }
}
