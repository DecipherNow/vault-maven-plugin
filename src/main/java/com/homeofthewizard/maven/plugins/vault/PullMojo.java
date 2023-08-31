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

import com.homeofthewizard.maven.plugins.vault.client.VaultClient;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import io.github.jopenlibs.vault.VaultException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Provides a Mojo that pulls values from Vault and sets Maven properties.
 */
@Mojo(name = "pull", defaultPhase = LifecyclePhase.INITIALIZE)
public class PullMojo extends VaultMojo {

  public PullMojo(){}

  public PullMojo(AuthenticationMethodProvider authenticationMethodProvider,
                  VaultClient vaultClient) {
      super(authenticationMethodProvider, vaultClient);
  }

  /**
   * Executes this Mojo which pulls project property values from Vault.
   *
   * @throws MojoExecutionException if an exception is thrown based upon the project configuration
   */
  @Override
  void executeVaultOperation() throws MojoExecutionException {
    if (this.skipExecution) {
      return;
    }
    try {
      vaultClient.pull(this.servers, this.project.getProperties(), this.outputMethod);
    } catch (VaultException exception) {
      throw new MojoExecutionException("Exception thrown pulling secrets.", exception);
    }
  }

}
