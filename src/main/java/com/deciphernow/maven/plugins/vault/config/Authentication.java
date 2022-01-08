package com.deciphernow.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.AuthenticationMethod;
import com.deciphernow.maven.plugins.vault.GithubToken;

import java.util.List;

public class Authentication {

  public static final String GITHUB_TOKEN_TAG = "githubToken";
  public static final List<String> methods = List.of(GITHUB_TOKEN_TAG);

  /**
   * Factory method that helps creating the authentication config.
   * @param server server
   * @return AuthenticationMethod subclass
   */
  public static AuthenticationMethod authenticationMethod(Server server) throws VaultException {
    String method = server.getAuthentication().keySet().stream()
            .findFirst()
            .orElseThrow(() -> new VaultException("cannot login to vault server without authentication info"));
    return mapNameToMethod(method, server);
  }

  private static AuthenticationMethod mapNameToMethod(String method, Server server) throws VaultException {
    if (method.equals(GITHUB_TOKEN_TAG)) {
      return new GithubToken(server);
    } else {
      throw new VaultException("cannot login to vault server without authentication info");
    }
  }

}
