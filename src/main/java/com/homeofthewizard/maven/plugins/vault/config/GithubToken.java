package com.homeofthewizard.maven.plugins.vault.config;

import java.io.Serializable;

public class GithubToken implements Serializable {

  private String pat;

  public String getPat() {
    return pat;
  }

  public void setPat(String pat) {
    this.pat = pat;
  }
}
