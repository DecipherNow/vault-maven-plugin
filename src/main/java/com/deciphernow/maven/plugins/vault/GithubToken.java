package com.deciphernow.maven.plugins.vault;

import static com.deciphernow.maven.plugins.vault.Vaults.vaultConfig;
import static com.deciphernow.maven.plugins.vault.config.Authentication.GITHUB_TOKEN_TAG;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.deciphernow.maven.plugins.vault.config.Authentication;
import com.deciphernow.maven.plugins.vault.config.Server;



public class GithubToken extends AuthenticationMethod {

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   *
   * @param server server
   * @throws VaultException if an exception is thrown based upon the vault configuration
   */
  public GithubToken(Server server) throws VaultException {
    super(server);
  }

  /**
   * A method that helps authenticate via a git PAT.
   *
   * @throws VaultException in case authentication fails
   */
  public void login() throws VaultException {

    String token = new Auth(vaultConfig)
                .loginByGithub(server.getAuthentication().get(GITHUB_TOKEN_TAG))
                .getAuthClientToken();

    server.setToken(token);
  }
}
