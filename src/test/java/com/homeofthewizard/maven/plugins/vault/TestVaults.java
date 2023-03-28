package com.homeofthewizard.maven.plugins.vault;

import com.bettercloud.vault.VaultException;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class TestVaults {

    @Test
    public void testAuthenticationIfNecessaryWithGithub() throws VaultException {
        var vaultGithubToken = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", List.of(), false, 1);
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);
        var githubTokenMock = Mockito.mock(GithubToken.class);
        var client = Vaults.create();
        when(authenticationProviderMock.fromServer(any())).thenReturn(githubTokenMock);
        doNothing().when(githubTokenMock).login();

        client.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() throws VaultException {
        exceptionRule.expect(VaultException.class);
        exceptionRule.expectMessage("Either a Token of Authentication method must be provided !!");

        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var client = Vaults.create();
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);

        client.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }
}
