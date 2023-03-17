package com.deciphernow.maven.plugins.vault;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;

public abstract class AuthenticationMethod {

  Auth auth;

  /**
   * Initializes a new instance of the {@link AuthenticationMethod} class.
   * @param auth Auth
   */
  public AuthenticationMethod(Auth auth) {
    this.auth = auth;
  }

  public abstract void login() throws VaultException;
}
