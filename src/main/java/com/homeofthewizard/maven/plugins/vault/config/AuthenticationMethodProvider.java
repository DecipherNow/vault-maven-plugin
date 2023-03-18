package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.homeofthewizard.maven.plugins.vault.AuthenticationMethod;

public interface AuthenticationMethodProvider {

  public AuthenticationMethod fromServer(Server server) throws VaultException;
}
