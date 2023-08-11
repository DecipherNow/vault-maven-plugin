package com.homeofthewizard.maven.plugins.vault;

import com.bettercloud.vault.VaultException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.homeofthewizard.maven.plugins.vault.client.VaultClient;
import com.homeofthewizard.maven.plugins.vault.config.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.randomPaths;
import static org.junit.jupiter.api.Assertions.*;

public class IntTestAuth {

    public static final String VAULT_GITHUB_PROPERTY_NAME = "vault.github.token";
    public static final String VAULT_APPROLE_ID_PROPERTY_NAME = "vault.appRole.id";
    private static final String VAULT_APPROLE_SECRET_ID_PROPERTY_NAME = "vault.appRole.secret";
    private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
    private static final String VAULT_HOST = System.getProperty("vault.host", "localhost");
    private static final String VAULT_PORT = System.getProperty("vault.port", "443");
    private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);

    // GitHub PAT authentication XML tags for mocking Server configuration
    private static final String patTag = GithubToken.class.getDeclaredFields()[0].getName();
    private static final TreeMap authMapGithub = new TreeMap<>();
    static {
        authMapGithub.put(patTag,System.getProperty(VAULT_GITHUB_PROPERTY_NAME));
    }
    private static final Map<String, TreeMap> VAULT_GITHUB_AUTH = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, authMapGithub);

    // AppROLE authentication XML tags for mocking Server configuration
    private static final String appRoleIdTag = AppRoleCredentials.class.getDeclaredFields()[0].getName();
    private static final String appRoleSecretIdTag = AppRoleCredentials.class.getDeclaredFields()[1].getName();
    private static final TreeMap authMapAppRole = new TreeMap<>();
    static {
        authMapAppRole.put(appRoleIdTag,System.getProperty(VAULT_APPROLE_ID_PROPERTY_NAME));
        authMapAppRole.put(appRoleSecretIdTag,System.getProperty(VAULT_APPROLE_SECRET_ID_PROPERTY_NAME));
    }
    private static final Map<String, TreeMap> VAULT_APPROLE_AUTH = Map.of(AuthenticationMethodFactory.APP_ROLE_TAG, authMapAppRole);

    private static class Fixture {

        private final List<Server> servers;
        private final Properties properties;

        private final AuthenticationMethodProvider authenticationMethodProvider;

        private Fixture(Map<String, TreeMap> authTags) throws URISyntaxException {
            List<Path> paths = randomPaths(10, 10);
            File certificate = new File(VAULT_CERTIFICATE.toURI());
            this.servers = ImmutableList.of(new Server(VAULT_SERVER, null, true, certificate, authTags, "", paths, false, 2));
            this.properties = new Properties();
            this.authenticationMethodProvider = new AuthenticationMethodFactory();
            this.servers.forEach(server -> {
                server.getPaths().forEach(path -> {
                    path.getMappings().forEach(mapping -> {
                        this.properties.setProperty(mapping.getProperty(), UUID.randomUUID().toString());
                    });
                });
            });
        }

        private static void with(Consumer<Fixture> test, Map<String, TreeMap> authTags) throws URISyntaxException {
            test.accept(new IntTestAuth.Fixture(authTags));
        }

    }

    private final VaultMojo mojoStub = new VaultMojo() {
        @Override
        void executeVaultOperation() {
            getLog().info("execution ended successfully");
        }
    };

    @Test
    public void testVaultGitHubAuthentication() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            mojoStub.project = new MavenProject();
            mojoStub.servers = fixture.servers;
            mojoStub.skipExecution = false;
            try {
                mojoStub.execute();
                mojoStub.servers.forEach(server -> assertNotNull(server.getToken()));
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            }
        }, VAULT_GITHUB_AUTH);
    }

    public void testVaultAppRoleAuthentication() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            mojoStub.project = new MavenProject();
            mojoStub.servers = fixture.servers;
            mojoStub.skipExecution = false;
            try {
                mojoStub.execute();
                mojoStub.servers.forEach(server -> assertNotNull(server.getToken()));
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            }
        }, VAULT_APPROLE_AUTH);
    }


    /**
     * Tests the {@link PushMojo#execute()} method.
     *
     * @throws URISyntaxException if an exception is raised parsing the certificate
     */
    @Test
    public void testGitHubAuthenticatedPushExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PushMojo mojo = new PushMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            fixture.properties.stringPropertyNames().forEach(key -> {
                mojo.project.getProperties().setProperty(key, fixture.properties.getProperty(key));
            });
            Properties properties = new Properties();
            var client = VaultClient.create();
            try {
                mojo.execute();
                client.pull(fixture.servers, properties);
                assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            } catch (VaultException exception) {
                fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
            }
        }, VAULT_GITHUB_AUTH);
    }


    /**
     * Tests the {@link PushMojo#execute()} method.
     *
     * @throws URISyntaxException if an exception is raised parsing the certificate
     */
    @Test
    public void testAppRoleAuthenticatedPushExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PushMojo mojo = new PushMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            fixture.properties.stringPropertyNames().forEach(key -> {
                mojo.project.getProperties().setProperty(key, fixture.properties.getProperty(key));
            });
            Properties properties = new Properties();
            var client = VaultClient.create();
            try {
                mojo.execute();
                client.pull(fixture.servers, properties);
                assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            } catch (VaultException exception) {
                fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
            }
        }, VAULT_APPROLE_AUTH);
    }


    @Test
    public void testGitHubAuthenticatedPullExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PullMojo mojo = new PullMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            var client = VaultClient.create();
            fixture.properties.stringPropertyNames().forEach(key -> {
                mojo.project.getProperties().setProperty(key, fixture.properties.getProperty(key));
            });
            try {
                client.authenticateIfNecessary(fixture.servers, fixture.authenticationMethodProvider);
                client.push(fixture.servers, fixture.properties);
                mojo.execute();
                assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            } catch (VaultException exception) {
                fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
            }
        }, VAULT_GITHUB_AUTH);
    }

    @Test
    public void testAppRoleAuthenticatedPullExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PullMojo mojo = new PullMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            var client = VaultClient.create();
            fixture.properties.stringPropertyNames().forEach(key -> {
                mojo.project.getProperties().setProperty(key, fixture.properties.getProperty(key));
            });
            try {
                client.authenticateIfNecessary(fixture.servers, fixture.authenticationMethodProvider);
                client.push(fixture.servers, fixture.properties);
                mojo.execute();
                assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            } catch (VaultException exception) {
                fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
            }
        }, VAULT_APPROLE_AUTH);
    }
}

