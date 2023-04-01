
# vault-maven-plugin
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/HomeOfTheWizard/vault-maven-plugin/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/HomeOfTheWizard/vault-maven-plugin/tree/master)
[![codecov](https://codecov.io/gh/HomeOfTheWizard/vault-maven-plugin/branch/develop/graph/badge.svg)](https://codecov.io/gh/HomeOfTheWizard/vault-maven-plugin)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.homeofthewizard/vault-maven-plugin?label=nexus-snapshots&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/homeofthewizard/vault-maven-plugin/)
[![Maven Central](https://img.shields.io/maven-central/v/com.homeofthewizard/vault-maven-plugin?color=green)]()

This Maven plugin supports pull and pushing Maven project properties from secrets stored in [HashiCorp](https://www.hashicorp.com) [Vault](https://www.vaultproject.io/).  
  
Forked project from [dechiphernow/vault-maven-plugin](https://github.com/DecipherNow/vault-maven-plugin) :thumbsup:
  
Added new features :rocket: :    
* Written with Java 11  
* Upgraded vault driver to use KV2 engine
* Added vault authentication methods (see below for details).
* Added Enterprise :factory: features like Namespace 

## Usage

To include the vault-maven-plugin in your project add the following plugin to your `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.1-SNAPSHOT</version>
        </plugin>
    </plugins>
</build>
```

### Pulling Secrets

In order to pull secrets you must add an execution to the plugin.  The following execution will pull secrets from `secret/user` path on the Vault server `https://vault.example.com`.  In particular, this configuration will set the value of the `${project.password}` and `${project.username}` Maven properties to the secrets `${vault.password}` and `${vault.username}` respectively.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.1-SNAPSHOT</version>
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
                                <token>bf6ba314-47f1-4b9d-ab87-2b8e53fc640f</token>
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

### Pushing Secrets

In order to pull secrets you must add an execution to the plugin.  The following execution will pull secrets from `secret/user` path on the Vault server `https://vault.example.com`.  In particular, this configuration will set the value of the `${project.password}` and `${project.username}` Maven properties to the secrets `${vault.password}` and `${vault.username}` respectively.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.1-SNAPSHOT</version>
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
                                <token>bf6ba314-47f1-4b9d-ab87-2b8e53fc640f</token>
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

### Authentication
In order to pull or push secrets you may have to authenticate to the vault, which is generally the case.    
Using a prefetched token works fine (provided in the `<token>` tag),

```
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.1-SNAPSHOT</version>
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
                                <token>bf6ba314-47f1-4b9d-ab87-2b8e53fc640f</token>
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

#### Github PAT
  
Use `<githubToken>` under the `<authentication>` tag, as in the following example.  

```
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.1-SNAPSHOT</version>
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
                                <authentication>
                                    <githubToken>XXXXXXXXXXXXXXXXXXXXXXXXXXXXX</githubToken>
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

## Building

This build uses standard Maven build commands but assumes that the following are installed and configured locally:

1) Java (11 or greater)
1) Maven (3.0 or greater)
1) Docker  

:warning: The package produced by the project is compatible with Java 8 as runtime,  
But you do need JDK 11 or higher to modify or build the source code of this library itself.

:warning: You also need to create a Github PAT,  
pass it as an environment variable in your [pom.xml](https://github.com/HomeOfTheWizard/vault-maven-plugin/blob/b8202ffe3afd1ec523a5cfa963f8a5caca6406bb/pom.xml#L55) `${env.MY_GITHUB_PAT_FOR_VAULT_LOGIN}`.
This is needed for integration tests related to authentication features.

## Contributing

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request
