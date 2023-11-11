---
layout: page
title: How to use it
nav_order: 2
---

## How to configure it
To include the vault-maven-plugin in your project and use it, add the following plugin to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
        </plugin>
    </plugins>
</build>
```
This plugin supports pull and pushing Maven project properties from secrets stored in [HashiCorp](https://www.hashicorp.com) [Vault](https://www.vaultproject.io/).
Inject them in maven's execution, or output as .env file, according to your need.  
You need to add the appropriate configuration to the plugin so that it will be executed as you want.  
Follow the below explanations for the different configurations. 

## Pulling Secrets
In order to pull secrets you must add an execution to the plugin.  
The following execution will pull secrets from `secret/user` path on the server `https://vault.example.com`.    
In particular, this configuration will set the value of the `${project.password}` and `${project.username}` Maven properties to the secrets `${vault.password}` and `${vault.username}` respectively.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>initialize</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <url>https://vault.example.com</url>
                                <token>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</token>
                                <paths>
                                    <path>
                                        <name>secret/user</name>
                                        <mappings>
                                            <mapping>
                                                <key>vault.password</key>
                                                <property>project.password</property>
                                            </mapping>
                                            <mapping>
                                                <key>vault.username</key>
                                                <property>project.username</property>
                                            </mapping>
                                        </mappings>
                                    </path>
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Note that the execution will fail if a specified secret key does not exist and that an existing project property will be overwritten.

Also note that there is `<outputMethod>` configuration tag that is not used here, that defines how to use the fetched credentials.  
By default, if you do not define this tag, the secrets will be injected as Maven properties.
See below on the dedicated part of this configuration for detailed info.

## Pushing Secrets
In order to pull secrets you must add an execution to the plugin.    
The following execution will pull secrets from `secret/user` path on the server `https://vault.example.com`.  
In particular, this configuration will set the value of the `${project.password}` and `${project.username}` Maven properties to the secrets `${vault.password}` and `${vault.username}` respectively.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>push</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>push</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <url>https://vault.example.com</url>
                                <token>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</token>
                                <paths>
                                    <path>
                                        <name>secret/user</name>
                                        <mappings>
                                            <mapping>
                                                <key>vault.password</key>
                                                <property>project.password</property>
                                            </mapping>
                                            <mapping>
                                                <key>vault.username</key>
                                                <property>project.username</property>
                                            </mapping>
                                        </mappings>
                                    </path>
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
Note that the execution will fail if a specified project property does not exist and that an existing secret value will be overwritten.

## Authentication
In order to pull or push secrets you may have to authenticate to the vault, which is generally the case.    
Using a prefetched token works fine (provided in the `<token>` tag),

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <url>https://vault.example.com</url>
                                <token>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</token>
                                <paths>
                                    ...
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

But if you want to automate the login process as well you can do it via the plugin directly.  
You can provide the configs under the `<authentication>` tag.
:warning: You should now remove the `token` tag entirely. An empty token tag is not allowed.

You have the following options enabled currently (others will follow soon):
* Github PAT
* AppRole

### How to use Github PAT ?

Use `<githubToken>` under the `<authentication>` tag, as in the following example.  
`<pat>` tag is for providing the personal access token.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <authentication>
                                    <githubToken>
                                        <pat>XXXXXXXXXXXXXXXXXXXXXXXXXXXXX</pat>
                                    </githubToken>
                                </authentication>
                                <url>https://vault.example.com</url>
                                <paths>
                                    ...
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

For general information about github token authentication in hashicorp Vault, see [here](https://developer.hashicorp.com/vault/docs/auth/github).


### How to use AppRole ?

Use `<appRole>` under the `<authentication>` tag, as in the following example.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            <server>
                                <authentication>
                                    <appRole>
                                        <roleId>xxxx</roleId>
                                        <secretId>yyyy</secretId>
                                    </appRole>
                                </authentication>
                                <url>https://vault.example.com</url>
                                <paths>
                                    ...
                                </paths>
                            </server>
                        </servers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

For general information about AppRole authentication in hashicorp Vault, see [here](https://developer.hashicorp.com/vault/docs/auth/approle).


## How to use the fetched secrets
There are 3 ways you can use the secrets once they are pulled from Vault server.  
By giving the corresponding value to the `<outputMethod>` configuration:  
* MavenProperties: inject secrets as Maven project properties
* SystemProperties: inject secrets as System properties
* EnvFile: output as .env file

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                <execution>
                    <id>pull</id>
                    <phase>initialize</phase>
                    <goals>
                        <goal>pull</goal>
                    </goals>
                    <configuration>
                        <servers>
                            ...
                        </servers>
                        <outputMethod>YOUR_OUTPUT_METHOD</outputMethod>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```