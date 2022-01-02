package com.deciphernow.maven.plugins.vault;

import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.config.Server;

public abstract class AuthenticationMethod {

  VaultConfig vaultConfig;
  Server server;

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   * @param server server
   * @throws VaultException if an exception is thrown based upon the vault configuration
   */
  public AuthenticationMethod(Server server) throws VaultException {
    this.server = server;
    this.vaultConfig = Vaults.vaultConfig(server.getUrl(), server.getToken(), server.getNamespace(),
            server.getSslVerify(), server.getSslCertificate());
  }

  public abstract void login() throws VaultException;
}
