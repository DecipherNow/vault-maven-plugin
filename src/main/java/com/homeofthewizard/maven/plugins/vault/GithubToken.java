package com.homeofthewizard.maven.plugins.vault;

import static com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory.GITHUB_TOKEN_TAG;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.homeofthewizard.maven.plugins.vault.config.Server;

public class GithubToken extends AuthenticationMethod {

  Server server;

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   *
   * @param auth Auth
   * @param server Server
   */
  public GithubToken(Auth auth, Server server) {
    super(auth);
    this.server = server;
  }

  /**
   * A method that helps authenticate via a git PAT.
   *
   * @throws VaultException in case authentication fails
   */
  public void login() throws VaultException {

    String token = auth
                .loginByGithub(server.getAuthentication().get(GITHUB_TOKEN_TAG))
                .getAuthClientToken();

    server.setToken(token);
  }
}
