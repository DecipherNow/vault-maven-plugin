package com.homeofthewizard.maven.plugins.vault;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.randomPaths;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.bettercloud.vault.VaultException;
import com.google.common.collect.ImmutableList;
import com.homeofthewizard.maven.plugins.vault.client.VaultClient;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodProvider;
import com.homeofthewizard.maven.plugins.vault.config.Path;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class TestPushMojo {

    private static final URL VAULT_CERTIFICATE = IntTestVaults.class.getResource("certificate.pem");
    private static final String VAULT_HOST = System.getProperty("vault.host", "localhost");
    private static final String VAULT_PORT = System.getProperty("vault.port", "443");
    private static final String VAULT_SERVER = String.format("https://%s:%s", VAULT_HOST, VAULT_PORT);
    private static final String VAULT_TOKEN = System.getProperty("vault.token");
    private static final Map<String,String> VAULT_GITHUB_AUTH = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");

    @Test
    public void testPushSkip() throws MojoExecutionException, URISyntaxException {
        List<Path> paths = randomPaths(10, 10);
        var mojo = new PushMojo();
        mojo.project = new MavenProject();
        mojo.servers = ImmutableList.of(new Server(VAULT_SERVER, VAULT_TOKEN, true, new File(VAULT_CERTIFICATE.toURI()), VAULT_GITHUB_AUTH, "", paths, false, 2));
        mojo.skipExecution = true;

        mojo.execute();
    }

    @Test
    public void testPush() throws MojoExecutionException, URISyntaxException, VaultException {
        List<Path> paths = randomPaths(10, 10);
        var authenticationMethodProvider = Mockito.mock(AuthenticationMethodProvider.class);
        var client = Mockito.mock(VaultClient.class);
        doNothing().when(client).authenticateIfNecessary(any(),any());
        doNothing().when(client).push(any(),any());

        var mojo = new PushMojo(authenticationMethodProvider, client);
        mojo.project = new MavenProject();
        mojo.servers = ImmutableList.of(new Server(VAULT_SERVER, VAULT_TOKEN, true, new File(VAULT_CERTIFICATE.toURI()), VAULT_GITHUB_AUTH, "", paths, false, 2));
        mojo.skipExecution = true;

        mojo.execute();
    }
}
