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

import com.homeofthewizard.maven.plugins.vault.client.VaultBackendProvider;
import com.homeofthewizard.maven.plugins.vault.client.VaultClient;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.OutputMethod;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import io.github.jopenlibs.vault.VaultException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

/**
 * Provides an abstract class for mojos that work with Vault.
 */
abstract class VaultMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  protected MavenProject project;

  @Parameter(required = true)
  protected List<Server> servers;

  @Parameter(defaultValue = "MavenProperties")
  protected OutputMethod outputMethod;

  @Parameter(property = "skipExecution", defaultValue = "false")
  protected boolean skipExecution;

  private final AuthenticationMethodProvider authenticationMethodProvider;
  protected final VaultClient vaultClient;

  VaultMojo() {
    this.authenticationMethodProvider = new AuthenticationMethodFactory();
    var vaultBackendProvider = new VaultBackendProvider();
    this.vaultClient = VaultClient.createForBackend(vaultBackendProvider);
  }

  VaultMojo(AuthenticationMethodProvider authenticationMethodProvider,
            VaultClient vaultClient) {
    this.authenticationMethodProvider = authenticationMethodProvider;
    this.vaultClient = vaultClient;
  }

  @Override
  public void execute() throws MojoExecutionException {
    if (this.skipExecution) {
      return;
    }
    executeVaultAuthentication();
    executeVaultOperation();
  }

  private void executeVaultAuthentication() throws MojoExecutionException {
    try {
      vaultClient.authenticateIfNecessary(servers, authenticationMethodProvider);
    } catch (VaultException e) {
      throw new MojoExecutionException("Exception thrown authenticating.", e);
    }
  }

  abstract void executeVaultOperation() throws MojoExecutionException;
}
