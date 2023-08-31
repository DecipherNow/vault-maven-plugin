package com.homeofthewizard.maven.plugins.vault.client;

import com.google.common.base.Strings;

import io.github.jopenlibs.vault.SslConfig;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;

import java.io.File;

public class VaultBackendProvider {

  /**
   * Defines the timeout when opening a connection with Vault.
   */
  private static final int OPEN_TIMEOUT = 5;

  /**
   * Defines the timeout when reading data from Vault.
   */
  private static final int READ_TIMEOUT = 30;

  /**
   * Returns a configured instance of the {@link Vault} class.
   *
   * @param server         the server
   * @param token          the token
   * @param namespace      the namespace
   * @param sslVerify      {@code true} if the connection should be verified; otherwise, {@code false}
   * @param sslCertificate the certificate file or null if not needed
   * @return the vaultConfig
   */
  public Vault vault(String server,
                     String token,
                     String namespace,
                     boolean sslVerify,
                     File sslCertificate,
                     Integer engineVersion) throws VaultException {
    return Vault.create(vaultConfig(server, token, namespace, sslVerify, sslCertificate, engineVersion));
  }


  /**
   * Returns a configured instance of the {@link VaultConfig} class.
   *
   * @param server         the server
   * @param token          the token
   * @param namespace      the namespace
   * @param sslVerify      {@code true} if the connection should be verified; otherwise, {@code false}
   * @param sslCertificate the certificate file or null if not needed
   * @return the vaultConfig
   */
  public VaultConfig vaultConfig(String server,
                                 String token,
                                 String namespace,
                                 boolean sslVerify,
                                 File sslCertificate,
                                 Integer engineVersion) throws VaultException {
    SslConfig sslConfig = new SslConfig().verify(sslVerify);
    if (sslCertificate != null) {
      sslConfig.pemFile(sslCertificate);
    }
    VaultConfig vaultConfig = new VaultConfig()
          .address(server)
          .openTimeout(OPEN_TIMEOUT)
          .readTimeout(READ_TIMEOUT)
          .sslConfig(sslConfig)
          .token(token)
          .engineVersion(engineVersion);
    if (!Strings.isNullOrEmpty(namespace)) {
      vaultConfig.nameSpace(namespace);
    }
    return vaultConfig;
  }
}
