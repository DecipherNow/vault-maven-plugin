package com.homeofthewizard.maven.plugins.vault;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.response.AuthResponse;
import com.homeofthewizard.maven.plugins.vault.config.AuthenticationMethodFactory;
import com.homeofthewizard.maven.plugins.vault.config.Server;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestGithubToken {

    @Test
    public void testLogin() throws VaultException {
        var vaultGithubToken = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");
        var server = new Server("URL",null,true,null,vaultGithubToken,null,null,true,null);
        var authMock = Mockito.mock(Auth.class);
        var authResponseMock = Mockito.mock(AuthResponse.class);
        when(authResponseMock.getAuthClientToken()).thenReturn("TOKEN");
        when(authMock.loginByGithub(any())).thenReturn(authResponseMock);
        var githubMethod = new GithubToken(authMock, server);

        githubMethod.login();

        Assert.assertTrue(server.getToken().equals("TOKEN"));
    }
}
