package com.homeofthewizard.maven.plugins.vault.client;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import com.homeofthewizard.maven.plugins.vault.config.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static com.homeofthewizard.maven.plugins.vault.VaultTestHelper.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestVaults {

    @Test
    public void testAuthenticationIfNecessaryWithMethod() throws VaultException {
        var githubTokenTag = GithubToken.class.getDeclaredFields()[0].getName();
        TreeMap map = new TreeMap<>();
        map.put(githubTokenTag,"token");
        Map<String, TreeMap> vaultGithubToken = Map.of(
                AuthenticationMethodFactory.APP_ROLE_TAG, map
        );
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", List.of(), false, 1);
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);
        var authenticationMethodMock = Mockito.mock(AuthenticationMethod.class);
        var vaultClient = VaultClient.create();
        when(authenticationProviderMock.fromServer(any())).thenReturn(authenticationMethodMock);
        doNothing().when(authenticationMethodMock).login();

        vaultClient.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultClient = VaultClient.create();

        VaultException ex = Assertions.assertThrows(
                VaultException.class,
                ()-> vaultClient.authenticateIfNecessary(List.of(server), null)
        );
        Assertions.assertTrue(ex.getMessage().contains("Either a Token of Authentication method must be provided !!"));
    }

    @Test
    public void testPullSkip() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), true, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(null);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);

        vaultClient.pull(List.of(server), null, OutputMethod.MavenProperties);

        verify(vaultBackendProviderMock, times(0)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPushSkip() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), true, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(null);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);

        vaultClient.push(List.of(server), null);

        verify(vaultBackendProviderMock, times(0)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPullEmptyPaths() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var vaultMock = Mockito.mock(Vault.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);

        vaultClient.pull(List.of(server), null, OutputMethod.MavenProperties);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPushEmptyPaths() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var vaultMock = Mockito.mock(Vault.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);

        vaultClient.push(List.of(server), null);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPull() throws VaultException {
        List<Path> paths = randomPaths(10, 10);
        var server = new Server("URL", null, false, null, null, "NAMESPACE", paths, false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var vaultMock = createVaultMock(paths);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);

        vaultClient.pull(List.of(server), new Properties(), OutputMethod.MavenProperties);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPush() throws VaultException {
        List<Path> paths = randomPaths(10, 10);
        var server = new Server("URL", null, false, null, null, "NAMESPACE", paths, false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var propertyMap = propertiesFromPaths(paths);
        var vaultMock = createVaultMock(paths);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = VaultClient.createForBackend(vaultBackendProviderMock);
        var properties = new Properties();
        properties.putAll(propertyMap);

        vaultClient.push(List.of(server), properties);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    private static Vault createVaultMock(List<Path> paths) throws VaultException {
        var vaultMock = Mockito.mock(Vault.class);
        var logicalMock = Mockito.mock(Logical.class);
        var logicalResponseMock = Mockito.mock(LogicalResponse.class);
        when(logicalResponseMock.getData()).thenReturn(secretsFromPaths(paths));
        when(logicalMock.read(any())).thenReturn(logicalResponseMock);
        when(logicalMock.list(any())).thenReturn(logicalResponseMock);
        when(logicalMock.write(any(),any())).thenReturn(logicalResponseMock);
        when(vaultMock.logical()).thenReturn(logicalMock);
        return vaultMock;
    }
}
