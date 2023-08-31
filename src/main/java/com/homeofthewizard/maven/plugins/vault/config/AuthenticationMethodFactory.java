package com.homeofthewizard.maven.plugins.vault.config;

import com.homeofthewizard.maven.plugins.vault.client.VaultBackendProvider;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Auth;

import java.util.List;

/**
 * A class that provides methods to get the authentication methods from the server config.
 */
public final class AuthenticationMethodFactory implements AuthenticationMethodProvider{

  public static final String GITHUB_TOKEN_TAG = "githubToken";
  public static final String APP_ROLE_TAG = "appRole";

  public static final List<String> methods = List.of(GITHUB_TOKEN_TAG, APP_ROLE_TAG);

  private final VaultBackendProvider vaultBackendProvider = new VaultBackendProvider();

  /**
   * Factory method that helps to create the authentication config.
   * @param server server
   * @return AuthenticationMethod subclass
   */
  @Override
  public AuthenticationMethod fromServer(Server server) throws VaultException {
    String method = server.getAuthentication().keySet().stream()
            .findFirst()
            .orElseThrow(() -> new VaultException("cannot login to vault server without authentication info"));
    return fromMethodName(method, server);
  }

  private AuthenticationMethod fromMethodName(String method, Server server) throws VaultException {
    var vaultConfig = vaultBackendProvider.vaultConfig(
            server.getUrl(),
            server.getToken(),
            server.getNamespace(),
            server.getSslVerify(),
            server.getSslCertificate(),
            server.getEngineVersion());
    var auth = new Auth(vaultConfig);

    switch (method) {
      case GITHUB_TOKEN_TAG: return new GithubTokenAuthMethod(auth, server);
      case APP_ROLE_TAG: return new AppRoleAuthMethod(auth, server);
      default: throw new VaultException("available authentication methods are: " + methods);
    }
  }

}
