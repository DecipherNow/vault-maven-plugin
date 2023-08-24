vault login 6edb70bd-3398-1f42-d67e-b9db377223cd
vault auth enable github
vault write auth/github/config organization=vault-plugin-test organization-id=0
vault write auth/github/map/teams/test-team value=dev-policy
vault auth enable approle
vault write auth/approle/role/test-role token_policies=dev-policy
vault read auth/approle/role/test-role/role-id | tail -n 1 | sed 's/ /\n/g' | tail -n 1 | sed 's/\(.*\)/roleId=\1/' >> /home/vault/appRole.properties
vault write -f auth/approle/role/test-role/secret-id | sed -n '3{p;q}' | sed 's/ /\n/g' | tail -n 1 | sed 's/\(.*\)/secretId=\1/' >> /home/vault/appRole.properties
vault policy write dev-policy /home/vault/dev-policy.hcl
