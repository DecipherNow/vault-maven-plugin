package com.homeofthewizard.maven.plugins.vault.config;

import io.github.jopenlibs.vault.VaultException;

/**
 * Interface providing methods to get the authentication method from the server config.
 */
public interface AuthenticationMethodProvider {

  AuthenticationMethod fromServer(Server server) throws VaultException;
}
