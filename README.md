# sample-vault
This is a sample project in scala to interact with Vault which is a credential management tool.

### Installing Vault

- Download Vault from the URL: https://www.vaultproject.io/downloads.html
- Extract the downloaded package.
- Verify installation using vault command.

### Starting the Dev Server

- vault server -dev -dev-listen-address "[ip]:[port]"

[ip] - Ip address of your machine e.g 192.168.1.1
[port] - port where vault needs to be run e.g 8200
 
- The output log should be:

```
==> Vault server configuration:

             Api Address: http://192.168.1.1:8200
                     Cgo: enabled
         Cluster Address: https://192.168.1.1:8201
              Listener 1: tcp (addr: "192.168.1.1:8200", cluster address: "192.168.1.1:8201", max_request_duration: "1m30s", max_request_size: "33554432", tls: "disabled")
               Log Level: info
                   Mlock: supported: true, enabled: false
                 Storage: inmem
                 Version: Vault v1.1.1
```                 

- From a new terminal, export the environment below

```
export VAULT_ADDR='http://192.168.1.1:8200'

export VAULT_DEV_ROOT_TOKEN_ID="{Root Token to be copied from terminal}"

```

### Writing AWS secrets through CLI

```
./vault kv put secret/dev/aws aws_access_token_key=1234 aws_access_token_secret=1234
```

Where the key represents - 
- secret - keyword representing the secrets stored in the vault
- dev - 1st level package or Store
- aws - 2nd Level package or Store
- Aws_access_token_key - Key stored in above Store
- Aws_access_token_secret - Key stored in above Store

### Reading AWS secrets through CLI

```
./vault kv get secret/dev/aws
```

### Reading AWS secrets through cURL Request

```
curl -H "X-Vault-Token: {Root Token to be copied from terminal}" -X GET http://127.0.0.1:8200/v1/secret/data/dev/aws
```

### Reading AWS secrets through the sample-vault project

```
sample-vault
```
