package com.homeofthewizard.maven.plugins.vault.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class TestOutputMethod {

    @Test
    public void shouldStoreMavenProperties(){
        var mavenPropOutMethod = OutputMethod.MavenProperties;
        var properties = new Properties();
        var secrets = new HashMap<String, String>();
        secrets.put("testSecretKey", "testSecretVal");
        var mapping = new Mapping("testSecretKey", "testPropertyName");

        mavenPropOutMethod.flush(properties, secrets, mapping);

        Assertions.assertTrue(properties.containsKey("testPropertyName"));
        Assertions.assertTrue(properties.getProperty("testPropertyName").equals("testSecretVal"));
    }

    @Test
    public void shouldStoreSystemProperties(){
        var sysPropOutMethod = OutputMethod.SystemProperties;
        var properties = new Properties();
        var secrets = new HashMap<String, String>();
        secrets.put("testSecretKey", "testSecretVal");
        var mapping = new Mapping("testSecretKey", "testPropertyName");

        sysPropOutMethod.flush(properties, secrets, mapping);

        Assertions.assertTrue(System.getProperties().containsKey("testPropertyName"));
        Assertions.assertTrue(System.getProperty("testPropertyName").equals("testSecretVal"));
    }

    @Test
    public void shouldStoreEnvFile(){
        var envFileOutMethod = OutputMethod.EnvFile;
        var properties = new Properties();
        var secrets = new HashMap<String, String>();
        secrets.put("testSecretKey", "testSecretVal");
        var mapping = new Mapping("testSecretKey", "testPropertyName");

        envFileOutMethod.flush(properties, secrets, mapping);

        var envFile = Paths.get(".env").toFile();
        Assertions.assertTrue(envFile.exists());
        envFile.delete();
    }
}
