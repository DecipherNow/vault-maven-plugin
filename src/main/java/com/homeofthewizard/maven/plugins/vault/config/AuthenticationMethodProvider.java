package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;

/**
 * Interface providing methods to get the authentication method from the server config.
 */
public interface AuthenticationMethodProvider {

  AuthenticationMethod fromServer(Server server) throws VaultException;
}
