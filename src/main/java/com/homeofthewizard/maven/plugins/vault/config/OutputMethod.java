package com.homeofthewizard.maven.plugins.vault.config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

public enum OutputMethod {
  MavenProperties{
    @Override
    public void flush(Properties properties, Map<String, String> secrets, Mapping mapping) {
      setMavenProperties(properties, secrets, mapping);
    }
  },
  SystemProperties{
    @Override
    public void flush(Properties properties, Map<String, String> secrets, Mapping mapping) {
      setSystemProperties(secrets, mapping);
    }
  },
  EnvFile{
    @Override
    public void flush(Properties properties, Map<String, String> secrets, Mapping mapping) {
      setEnvFile(secrets, mapping);
    }
  };

  public abstract void flush(Properties properties, Map<String, String> secrets, Mapping mapping);

  /**
   * Creates an .envFile and put the secrets in it, respecting the key/property mapping definition given.
   * @param secrets secrets fetched from Vault.
   * @param mapping mapping defined in maven project.
   */
  private static void setEnvFile(Map<String, String> secrets, Mapping mapping) {
    Properties prop = new Properties();
    try (OutputStream outputStream = new FileOutputStream(".env")) {
      prop.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
      prop.store(outputStream, null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Sets the secrets in System.getProperties(), respecting the key/property mapping definition given.
   * @param secrets secrets fetched from Vault.
   * @param mapping mapping defined in maven project.
   */
  private static void setSystemProperties(Map<String, String> secrets, Mapping mapping) {
    System.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
  }

  /**
   * Sets the secrets in mavenProject.properties, respecting the key/property mapping definition given.
   * @param properties maven project properties
   * @param secrets secrets fetched from Vault.
   * @param mapping mapping defined in maven project.
   */
  private static void setMavenProperties(Properties properties, Map<String, String> secrets, Mapping mapping) {
    properties.setProperty(mapping.getProperty(), secrets.get(mapping.getKey()));
  }
}
