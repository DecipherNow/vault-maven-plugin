---
layout: page
title: By Application Type
parent: Practical use cases
---

# 1. Other maven plugins
Many CI tools are used via maven plugins for the convenience of not having to manage executables.
Sonar scanner and liquibase are one of them.
The example below shows you how to fetch the credentials necessary to use those plugins, via the vault-maven-plugin.

Let's say you are using liquibase and have the following pom.xml
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.liquibase</groupId>
            <artifactId>liquibase-maven-plugin</artifactId>
            <version>4.6.1</version>
            <executions>
                <execution>
                    <phase>compile</phase>
                    <goals>
                        <goal>update</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <propertyFile>target/classes/liquibase.properties</propertyFile>
            </configuration>
        </plugin>
    </plugins>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
            <includes>
                <include>*.properties</include>
            </includes>
        </resource>
    </resources>
</build>
``` 

Your property file might be like below.
```properties
changeLogFile=src/main/resources/liquibase/.../changelog.xml
url=...
database=...
username=...
password=${database.password}
```

Usually the property `database.password` if given to maven as system property or environment variables.

If you don't have a tool that will fetch the secret and create the environment variable for you, you can use the maven vault plugin.
You just add the following to fetch the secrets and inject them as properties in maven.
```xml
<plugin>
    <groupId>com.homeofthewizard</groupId>
    <artifactId>vault-maven-plugin</artifactId>
    <version>1.1.2-SNAPSHOT</version>
    <executions>
        <execution>
            <id>pull</id>
            <phase>initialize</phase>
            <goals>
                <goal>pull</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <servers>
            <server>
                <url>https://your-vault-host</url>
                <token>XXXXXXX</token>
                <paths>
                    <path>
                        <name>secret/data/some-app</name>
                        <mappings>
                            <mapping>
                                <key>vaultSecretKey</key>
                                <property>database.password</property>
                            </mapping>
                        </mappings>
                    </path>
                </paths>
            </server>
        </servers>
    </configuration>
</plugin>
```

and Voilà! :tada:  
When you execute your liquibase plugin `mvn clean compile`,  
the vault plugin will fetch the secrets,  
the resource plugin will put it in the property file by replacing the placeholder,  
then the liquibase plugin will run using the secret!  

No need to handle secrets in the execution environment anymore ! :massage_man:  
You only need to give set the execution environment's credentials to access Vault!

# 2. Maven Applications  

{: .warning }
As already mentionned [here](https://homeofthewizard.github.io/vault-maven-plugin/#example-use-cases), if your application is using Spring-Cloud,  
You should use [Spring Cloud Vault](https://spring.io/projects/spring-cloud-vault) instead of this maven plugin.

Many Java application like Spring applications externalise their configurations in a property file,  
as recommended by the [12 factor app](https://12factor.net/config).    
Secrets are part of those configuration files. The configuration file can be stored in version control.    
But a good security practice is not to store the secrets on version control.    
Hence, the secrets are fetched generally from environment variables on the system where we run the app.  

You can do the following to fetch the secrets from vault via the Vault Plugin,  
then run your application with maven, and your secrets will be injected as system properties.    
  
Let's say you are using Spring and have the following `application.properties`   
```properties
app.secret=${APPLICATION_SECRET}
```  

You can define the following in your pom.xml to fetch the credential for `APPLICATION_SECRET`.  
```xml
<plugins>
    <plugin>
        <groupId>com.homeofthewizard</groupId>
        <artifactId>vault-maven-plugin</artifactId>
        <version>1.1.2-SNAPSHOT</version>
        <executions>
            <execution>
                <id>pull</id>
                <phase>initialize</phase>
                <goals>
                    <goal>pull</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <servers>
                <server>
                    <url>https://your-vault-host</url>
                    <token>XXXXXXX</token>
                    <paths>
                        <path>
                            <name>secret/data/some-app</name>
                            <mappings>
                                <mapping>
                                    <key>vaultSecretKey</key>
                                    <property>APPLICATION_SECRET</property>
                                </mapping>
                            </mappings>
                        </path>
                    </paths>
                </server>
            </servers>
            <outputMethod>SystemProperties</outputMethod>
        </configuration>
    </plugin>
</plugins>
```

The above code will first fetch the secrets from vault with the vault-plugin, then they will be added to Maven's system properties (`<outputMethod>` configuration).    
After that you can run your java app with either [exec-maven-plugin](http://www.mojohaus.org/exec-maven-plugin/usage.html) or the [spring-boot-maven-plugin](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/)
By running your application with maven, maven's system properties will be shared with your application.  

<p><img src="../assets/images/old%20vs%20new%20way.png"/></p>

### In case you have a Non-Spring Java application
Spring boot manages system properties and environment variables differently that the standard way.  
You will find here a [blog post](https://homeofthewizard.github.io/secrets-in-java) that explains how the two works in java, and [here](https://www.baeldung.com/spring-boot-properties-env-variables) a post explaining spring's difference.  
But in short, Spring allows you to access both via the same API (using placeholders like `${myVar}`). So for a spring application that requires environment variables, we can emulate them by injecting System properties instead.    

For a non Spring application that requires environment variables, we cannot do that, we need to give the JVM real environment variables.  
Let's say you have a simple java application, with a main class like this
```java
package org.example;

public class Main {
    public static void main(String[] args) {
        System.out.println("Env variable fetched: "+ System.getenv("ENV_VAR_SECRET"));
    }
}
```

you have the following pom.xml
```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
            <archive>
                <manifest>
                    <addClasspath>true</addClasspath>
                    <mainClass>org.example.Main</mainClass>
                </manifest>
            </archive>
        </configuration>
    </plugin>
</plugins>
```

This app is using some environment variables given by its execution environment  
when run via `java -jar myApp.jar`.

Now if we use the Vault Maven Plugin, we can fetch the secret, run the app via maven, and the env variable will be injected directly.  
By adding the followings to your pom.xml like so:
```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
            <archive>
                <manifest>
                    <addClasspath>true</addClasspath>
                    <mainClass>org.example.Main</mainClass>
                </manifest>
            </archive>
        </configuration>
    </plugin>
    <plugin>
        <groupId>com.homeofthewizard</groupId>
        <artifactId>vault-maven-plugin</artifactId>
        <version>1.1.2-SNAPSHOT</version>
        <executions>
            <execution>
                <id>pull</id>
                <phase>initialize</phase>
                <goals>
                    <goal>pull</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <servers>
                <server>
                    <url>https://your-vault-host</url>
                    <token>XXXXX</token>
                    <paths>
                        <path>
                            <name>secret/data/myJavaApp</name>
                            <mappings>
                                <mapping>
                                    <key>secretKey</key>
                                    <property>mavenPropertyForSecret</property>
                                </mapping>
                            </mappings>
                        </path>
                    </paths>
                </server>
            </servers>
        </configuration>
    </plugin>
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
        <version>1.2.1</version>
        <configuration>
            <executable>java</executable>
            <environmentVariables>
                <ENV_VAR_SECRET>${mavenPropertyForSecret}</ENV_VAR_SECRET>
            </environmentVariables>
            <arguments>
                <argument>-classpath</argument>
                <classpath />
                <argument>org.example.Main</argument>
            </arguments>
        </configuration>
    </plugin>
</plugins>
```
And Voilà! :tada:  
Now you just need to run `mvn vault:pull exec:exec`  
The only thing to provide is the token to authenticate to Vault server (`<token>XXXXX</token>`).  
Isn't that magical ? :mage:

# 3. Application that does not use Maven
Many developers use .env files to manage environment variables on their localhost.  
It may be the case even on production for application that do not have tools to fetch the secrets directly from a secure shared place like Vault.      

If that is the case, the plugin can generate a .env file for you.    
The configuration `<outputMethod>EnvFile</outputMethod>` allows this.    

Then you can inject it with [dotenv-java](https://github.com/cdimascio/dotenv-java) for example.    

There are also IntelliJ plugins that help you inject secrets via .env files before running/debugging the application in your local environment.    
