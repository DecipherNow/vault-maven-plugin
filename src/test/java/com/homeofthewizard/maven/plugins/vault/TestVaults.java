package com.homeofthewizard.maven.plugins.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.homeofthewizard.maven.plugins.vault.client.VaultBackendProvider;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestVaults {

    @Test
    public void testAuthenticationIfNecessaryWithGithub() throws VaultException {
        var vaultGithubToken = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", List.of(), false, 1);
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);
        var githubTokenMock = Mockito.mock(GithubToken.class);
        var vaultClient = Vaults.create();
        when(authenticationProviderMock.fromServer(any())).thenReturn(githubTokenMock);
        doNothing().when(githubTokenMock).login();

        vaultClient.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() throws VaultException {
        exceptionRule.expect(VaultException.class);
        exceptionRule.expectMessage("Either a Token of Authentication method must be provided !!");

        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultClient = Vaults.create();

        vaultClient.authenticateIfNecessary(List.of(server), null);
    }

    @Test
    public void testPullSkip() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), true, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(null);
        var vaultClient = Vaults.createForBackend(vaultBackendProviderMock);

        vaultClient.pull(List.of(server), null);

        verify(vaultBackendProviderMock, times(0)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPull() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var vaultMock = Mockito.mock(Vault.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = Vaults.createForBackend(vaultBackendProviderMock);

        vaultClient.pull(List.of(server), null);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }

    @Test
    public void testPush() throws VaultException {
        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var vaultBackendProviderMock = Mockito.mock(VaultBackendProvider.class);
        var vaultMock = Mockito.mock(Vault.class);
        when(vaultBackendProviderMock.vault(any(),any(),any(),anyBoolean(),any(),any())).thenReturn(vaultMock);
        var vaultClient = Vaults.createForBackend(vaultBackendProviderMock);

        vaultClient.push(List.of(server), null);

        verify(vaultBackendProviderMock, times(1)).vault(any(),any(),any(),anyBoolean(),any(),any());
    }
}
