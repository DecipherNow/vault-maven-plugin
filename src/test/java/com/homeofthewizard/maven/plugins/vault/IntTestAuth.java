package com.homeofthewizard.maven.plugins.vault;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.randomPaths;

import com.bettercloud.vault.VaultException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.Path;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class IntTestAuth {

    private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
    private static final String VAULT_HOST = System.getProperty("vault.host", "localhost");
    private static final String VAULT_PORT = System.getProperty("vault.port", "443");
    private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);
    private static final Map<String,String> VAULT_GITHUB_AUTH = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, System.getProperty("vault.github.token"));

    private static class Fixture {

        private final List<Server> servers;
        private final Properties properties;

        private final AuthenticationMethodProvider authenticationMethodProvider;

        private Fixture() throws URISyntaxException {
            List<Path> paths = randomPaths(10, 10);
            File certificate = new File(VAULT_CERTIFICATE.toURI());
            this.servers = ImmutableList.of(new Server(VAULT_SERVER, null, true, certificate, VAULT_GITHUB_AUTH, "", paths, false, 2));
            this.properties = new Properties();
            this.authenticationMethodProvider = new AuthenticationMethodFactory();
            this.servers.stream().forEach(server -> {
                server.getPaths().stream().forEach(path -> {
                    path.getMappings().stream().forEach(mapping -> {
                        this.properties.setProperty(mapping.getProperty(), UUID.randomUUID().toString());
                    });
                });
            });
        }

        private static void with(Consumer<Fixture> test) throws URISyntaxException {
            test.accept(new IntTestAuth.Fixture());
        }

    }

    private VaultMojo mojoStub = new VaultMojo() {
        @Override
        void executeVaultOperation() {
            getLog().info("execution ended successfully");
        }
    };

    @Test
    public void testVaultAuthentication() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            mojoStub.project = new MavenProject();
            mojoStub.servers = fixture.servers;
            mojoStub.skipExecution = false;
            try {
                mojoStub.execute();
                mojoStub.servers.stream().forEach(server -> assertNotNull(server.getToken()));
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            }
        });
    }


    /**
     * Tests the {@link PushMojo#execute()} method.
     *
     * @throws URISyntaxException if an exception is raised parsing the certificate
     */
    @Test
    public void testAuthenticatedPushExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PushMojo mojo = new PushMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            fixture.properties.stringPropertyNames().stream().forEach(key -> {
                mojo.project.getProperties().setProperty(key, fixture.properties.getProperty(key));
            });
            Properties properties = new Properties();
            var client = Vaults.create();
            try {
                mojo.execute();
                client.pull(fixture.servers, properties);
                assertTrue(Maps.difference(fixture.properties, mojo.project.getProperties()).areEqual());
            } catch (MojoExecutionException exception) {
                fail(String.format("Unexpected exception while executing: %s", exception.getMessage()));
            } catch (VaultException exception) {
                fail(String.format("Unexpected exception while pushing to Vault: %s", exception.getMessage()));
            }
        });
    }


    @Test
    public void testAuthenticatedPullExecute() throws URISyntaxException {
        IntTestAuth.Fixture.with(fixture -> {
            PullMojo mojo = new PullMojo();
            mojo.project = new MavenProject();
            mojo.servers = fixture.servers;
            mojo.skipExecution = false;
            var client = Vaults.create();
            fixture.properties.stringPropertyNames().stream().forEach(key -> {
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
        });
    }
}

