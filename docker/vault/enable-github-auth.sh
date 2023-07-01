vault login 6edb70bd-3398-1f42-d67e-b9db377223cd
vault auth enable github
vault write auth/github/config organization=vault-plugin-test organization-id=0
vault write auth/github/map/teams/test-team value=dev-policy
vault policy write dev-policy /home/vault/dev-policy.hcl