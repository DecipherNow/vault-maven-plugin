package com.homeofthewizard.maven.plugins.vault.client;

import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.OutputMethod;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import io.github.jopenlibs.vault.VaultException;

import java.util.List;
import java.util.Properties;

/**
 * Interface for classes that provides methods to interact with a Vault server.
 * Provides static methods to give hidden implementations of a VaultClient.
 */
public interface VaultClient {

  static VaultClient createForBackend(VaultBackendProvider vaultBackendProvider) {
    return new Vaults(vaultBackendProvider);
  }

  static VaultClient create() {
    return new Vaults(new VaultBackendProvider());
  }

  void pull(List<Server> servers, Properties properties, OutputMethod outputMethod) throws VaultException;

  void push(List<Server> servers, Properties properties) throws VaultException;

  void authenticateIfNecessary(List<Server> servers, AuthenticationMethodProvider factory) throws VaultException;
}
