package com.homeofthewizard.maven.plugins.vault.config;

import static com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory.GITHUB_TOKEN_TAG;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;

class GithubTokenAuthMethod extends AuthenticationMethod<GithubToken> {

  private final Server server;

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   *
   * @param auth Auth
   * @param server Server
   */
  GithubTokenAuthMethod(Auth auth, Server server) {
    super(auth, GithubToken.class);
    this.server = server;
  }

  /**
   * A method that helps authenticate via a git PAT.
   *
   * @throws VaultException in case authentication fails
   */
  public void login() throws VaultException {
    var githubPat = getAuthCredentials(server.getAuthentication().get(GITHUB_TOKEN_TAG)).getPat();

    String token = auth
                .loginByGithub(githubPat)
                .getAuthClientToken();

    server.setToken(token);
  }
}
