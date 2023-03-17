package com.deciphernow.maven.plugins.vault;

import com.bettercloud.vault.VaultException;
import com.deciphernow.maven.plugins.vault.GithubToken;
import com.deciphernow.maven.plugins.vault.Vaults;
import com.deciphernow.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.deciphernow.maven.plugins.vault.config.Server;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class TestVaults {

    @Test
    public void testAuthenticationIfNecessaryWithGithub() throws VaultException {
        var vaultGithubToken = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", List.of(), false, 1);
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);
        var githubTokenMock = Mockito.mock(GithubToken.class);
        when(authenticationProviderMock.fromServer(any())).thenReturn(githubTokenMock);
        doNothing().when(githubTokenMock).login();

        Vaults.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() throws VaultException {
        exceptionRule.expect(VaultException.class);
        exceptionRule.expectMessage("Either a Token of Authentication method must be provided !!");

        var server = new Server("URL", null, false, null, null, "NAMESPACE", List.of(), false, 1);
        var authenticationProviderMock = Mockito.mock(AuthenticationMethodFactory.class);

        Vaults.authenticateIfNecessary(List.of(server), authenticationProviderMock);
    }
}
