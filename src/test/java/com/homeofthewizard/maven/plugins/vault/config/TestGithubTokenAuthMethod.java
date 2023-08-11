package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.response.AuthResponse;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TestGithubTokenAuthMethod {

    @Test
    public void testLogin() throws VaultException {
        var githubTokenTag = GithubToken.class.getDeclaredFields()[0].getName();
        TreeMap map = new TreeMap<>();
        map.put(githubTokenTag,"token");
        Map<String, TreeMap> vaultGithubToken = Map.of(
                AuthenticationMethodFactory.GITHUB_TOKEN_TAG, map
        );
        var server = new Server("URL",null,true,null,vaultGithubToken,null,null,true,null);
        var authMock = Mockito.mock(Auth.class);
        var authResponseMock = Mockito.mock(AuthResponse.class);
        when(authResponseMock.getAuthClientToken()).thenReturn("TOKEN");
        when(authMock.loginByGithub(any())).thenReturn(authResponseMock);
        var githubMethod = new GithubTokenAuthMethod(authMock, server);

        githubMethod.login();

        Assert.assertTrue(server.getToken().equals("TOKEN"));
    }
}
