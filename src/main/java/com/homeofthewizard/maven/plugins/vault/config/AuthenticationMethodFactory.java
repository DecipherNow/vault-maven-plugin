package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.homeofthewizard.maven.plugins.vault.AuthenticationMethod;
import com.homeofthewizard.maven.plugins.vault.GithubToken;
import com.homeofthewizard.maven.plugins.vault.client.VaultBackendProvider;

import java.util.List;

public final class AuthenticationMethodFactory implements AuthenticationMethodProvider{

  public static final String GITHUB_TOKEN_TAG = "githubToken";
  public static final List<String> methods = List.of(GITHUB_TOKEN_TAG);

  private final VaultBackendProvider vaultBackendProvider = new VaultBackendProvider();

  /**
   * Factory method that helps creating the authentication config.
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
    if (method.equals(GITHUB_TOKEN_TAG)) {
      return new GithubToken(auth, server);
    } else {
      throw new VaultException("available authentication methods are: " + methods);
    }
  }

}
