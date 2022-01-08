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

import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Server;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Objects;




/**
 * Provides an abstract class for mojos that work with Vault.
 */
abstract class VaultMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  protected MavenProject project;

  @Parameter(required = true)
  protected List<Server> servers;

  @Parameter(property = "skipExecution", defaultValue = "false")
  protected boolean skipExecution;

  @Override
  public void execute() throws MojoExecutionException {
    try {
      Vaults.authenticateIfNecessary(servers);
    } catch (VaultException e) {
      throw new MojoExecutionException("Exception thrown authenticating.", e);
    }
    executeVaultOperation();
  }

  abstract void executeVaultOperation() throws MojoExecutionException;
}
