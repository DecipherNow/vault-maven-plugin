package com.homeofthewizard.maven.plugins.vault.config;

import java.io.Serializable;

public class AppRoleCredentials implements Serializable {
  private String roleId;
  private String secretId;

  public String getSecretId() {
    return secretId;
  }

  public String getRoleId() {
    return roleId;
  }
}
