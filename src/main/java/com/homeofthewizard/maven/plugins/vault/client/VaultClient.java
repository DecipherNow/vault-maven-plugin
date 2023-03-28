package com.homeofthewizard.maven.plugins.vault.client;

import com.bettercloud.vault.VaultException;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.Server;

import java.util.List;
import java.util.Properties;

public interface VaultClient {
  public void pull(List<Server> servers, Properties properties) throws VaultException;

  public void push(List<Server> servers, Properties properties) throws VaultException;

  public void authenticateIfNecessary(List<Server> servers, AuthenticationMethodProvider factory) throws VaultException;
}
