
# vault-maven-plugin
[![CircleCI](https://dl.circleci.com/status-badge/img/gh/HomeOfTheWizard/vault-maven-plugin/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/HomeOfTheWizard/vault-maven-plugin/tree/master)
[![codecov](https://codecov.io/gh/HomeOfTheWizard/vault-maven-plugin/branch/develop/graph/badge.svg)](https://codecov.io/gh/HomeOfTheWizard/vault-maven-plugin)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.homeofthewizard/vault-maven-plugin?label=nexus-snapshots&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/homeofthewizard/vault-maven-plugin/)
[![Maven Central](https://img.shields.io/maven-central/v/com.homeofthewizard/vault-maven-plugin?color=green)](https://central.sonatype.com/artifact/com.homeofthewizard/vault-maven-plugin/1.1.1)
[![Vault Version](https://img.shields.io/badge/vault-latest-blue?label=vault-version)](https://hub.docker.com/r/hashicorp/vault/tags)

This Maven plugin supports pull and pushing Maven project properties from secrets stored in [HashiCorp](https://www.hashicorp.com) [Vault](https://www.vaultproject.io/).  
  
Forked project from [dechiphernow/vault-maven-plugin](https://github.com/DecipherNow/vault-maven-plugin) :thumbsup:
  
Added new features :rocket: :    
* Written with Java 11  
* Upgraded vault driver to use KV2 engine
* Added vault authentication methods (see below for details).
* Added Enterprise :factory: features like Namespace 

## Usage

To include the vault-maven-plugin in your project add the following plugin to your `pom.xml` file:  
You need to setup an execution phase and configuration.  
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.homeofthewizard</groupId>
            <artifactId>vault-maven-plugin</artifactId>
            <version>1.1.2</version>
            <executions>
                ...
                <configuration>
                    ...
                </configuration>
            </executions>
        </plugin>
    </plugins>
</build>
```

The documentation [here](https://homeofthewizard.github.io/vault-maven-plugin/) is where we explain how to do it in detail.

## Building

This build uses standard Maven build commands but assumes that the following are installed and configured locally:

1. Java (11 or greater)
2. Maven (3.0 or greater)
3. Docker  

:warning: The package produced by the project is compatible with Java 8 as runtime,  
But you do need JDK 11 or higher to modify or build the source code of this library itself.

:warning: You also need to create a GitHub PAT,  
pass it as an environment variable in your [pom.xml](https://github.com/HomeOfTheWizard/vault-maven-plugin/blob/b8202ffe3afd1ec523a5cfa963f8a5caca6406bb/pom.xml#L55) `${env.MY_GITHUB_PAT_FOR_VAULT_LOGIN}`.
This is needed for integration tests related to authentication features.

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request
