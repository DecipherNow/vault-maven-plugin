package com.deciphernow.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.AuthenticationMethod;

public interface AuthenticationMethodProvider {

  public AuthenticationMethod fromServer(Server server) throws VaultException;
}
