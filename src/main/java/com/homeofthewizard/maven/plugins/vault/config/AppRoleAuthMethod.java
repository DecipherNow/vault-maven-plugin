package com.homeofthewizard.maven.plugins.vault.config;

import static com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory.APP_ROLE_TAG;

import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Auth;


public class AppRoleAuthMethod extends AuthenticationMethod<AppRoleCredentials> {
  private final Server server;

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   *
   * @param auth Auth
   * @param server Server
   */
  AppRoleAuthMethod(Auth auth, Server server) {
    super(auth, AppRoleCredentials.class);
    this.server = server;
  }

  /**
   * A method that helps authenticate via a git AppRole.
   *
   * @throws VaultException in case authentication fails
   */
  public void login() throws VaultException {
    var appRoleCredentials = getAuthCredentials(server.getAuthentication().get(APP_ROLE_TAG));

    String token = auth
            .loginByAppRole(appRoleCredentials.getRoleId(), appRoleCredentials.getSecretId())
            .getAuthClientToken();

    server.setToken(token);
  }
}
