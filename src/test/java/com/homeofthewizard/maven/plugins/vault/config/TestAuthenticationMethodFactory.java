package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static java.util.List.of;

public class TestAuthenticationMethodFactory {

    @Test
    public void testAuthenticationMethod() throws VaultException {
        var githubTokenTag = GithubToken.class.getDeclaredFields()[0].getName();
        TreeMap map = new TreeMap<>();
        map.put(githubTokenTag,"token");
        Map<String, TreeMap> vaultGithubToken = Map.of(
                AuthenticationMethodFactory.GITHUB_TOKEN_TAG, map
        );
        var server = new Server("VAULT_SERVER", "VAULT_TOKEN", true, null, vaultGithubToken, "", of(), false, 2);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var method = authenticationMethodFactory.fromServer(server);

        Assert.assertTrue(method instanceof GithubTokenAuthMethod);
    }

    @Test
    public void testAuthenticationIfNecessaryUnrecognizedMethod() throws VaultException {
        TreeMap map = new TreeMap<>();
        map.put("UNRECOGNIZED_TOKEN_TYPE","UNRECOGNIZED_TOKEN");
        Map<String, TreeMap> vaultGithubToken = Map.of(
                "UNRECOGNIZED_AUTH_METHOD", map
        );
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", of(), false, 1);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var ex = Assertions.assertThrows(VaultException.class, ()-> authenticationMethodFactory.fromServer(server));
        Assertions.assertTrue(ex.getMessage().contains("available authentication methods are:"));

    }

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() throws VaultException {
        var vaultGithubToken = Map.<String,TreeMap>of();
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", of(), false, 1);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var ex = Assertions.assertThrows(VaultException.class, ()->authenticationMethodFactory.fromServer(server));
        Assertions.assertTrue(ex.getMessage().contains("cannot login to vault server without authentication info"));
    }
}
