package com.homeofthewizard.maven.plugins.vault.config;

import com.bettercloud.vault.VaultException;
import com.homeofthewizard.maven.plugins.vault.GithubToken;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.List.of;

public class TestAuthenticationMethodFactory {

    @Test
    public void testAuthenticationMethod() throws VaultException {
        var authentication = Map.of(AuthenticationMethodFactory.GITHUB_TOKEN_TAG, "token");
        var server = new Server("VAULT_SERVER", "VAULT_TOKEN", true, null, authentication, "", of(), false, 2);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var method = authenticationMethodFactory.fromServer(server);

        Assert.assertTrue(method instanceof GithubToken);
    }

    @Test
    public void testAuthenticationIfNecessaryUnrecognizedMethod() throws VaultException {
        var vaultGithubToken = Map.of("UNRECOGNIZED_AUTH_METHOD", "UNRECOGNIZED_TOKEN");
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", of(), false, 1);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var ex = Assertions.assertThrows(VaultException.class, ()-> authenticationMethodFactory.fromServer(server));
        Assertions.assertTrue(ex.getMessage().contains("available authentication methods are:"));

    }

    @Test
    public void testAuthenticationIfNecessaryWithoutMethod() throws VaultException {
        var vaultGithubToken = Map.<String,String>of();
        var server = new Server("URL", null, false, null, vaultGithubToken, "NAMESPACE", of(), false, 1);
        var authenticationMethodFactory = new AuthenticationMethodFactory();

        var ex = Assertions.assertThrows(VaultException.class, ()->authenticationMethodFactory.fromServer(server));
        Assertions.assertTrue(ex.getMessage().contains("cannot login to vault server without authentication info"));
    }
}
