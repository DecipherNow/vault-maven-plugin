---
layout: page
title: By Execution Environment
parent: Practical use cases
---

# 1. CI environments
Lets say you have a CI build on jenkins using tools like Sonar. 
```groovy
pipeline{
    ...
    
    stages{
        stage('Sonar Analysis'){
            withCredentials([ string(credentialsId: 'mySonarJenkinsToken', variable: 'TOKEN')]){
                echo 'running sonar analysis'
                sh"""
                    mvn sonar:sonar -Dsonar.login=$TOKEN
                """
            }
        }
    }
}
```

another pipeline to deploy changes on your DB with Liquibase
```groovy
pipeline {
    ...

    stages {
        stage('Update PostgreDB') {
            withCredentials([usernamePassword(credentialsId: 'myPostgreCredentials', usernameVariable: 'USER', passwordVariable: 'PASSWORD')]) {
                echo 'running liquibase'
                sh """
                    mvn compile -Dpostgre.user=$USER -Dpostgre.password=$PASSWORD
                """
            }
        }
    }
}
```

and you have another CD pipeline to deploy your app on K8s
```groovy
pipeline {
    ...
    stages {
        stage('Deploying app to K8s') {
            withCredentials([file(credentialsId: 'myKubeConfig', variable: 'KUBECONFIG')]) {
                echo 'deploying app via kubectl'
                sh """
                    kubectl apply -f deployment.yaml --namespace ns-my-namespace
                """
            }
        }
    }
}
```

As you can see you are using lots of secrets to run all those pipelines, which have to be stored on jenkins.
If you use the maven vault plugin and you declare them in your pom.xml with associated secret key from Vault, you don't need to store them on Jenkins anymore. 
Then you can have simple pipelines like the following.

```groovy
pipeline {
    ...

    stages {
        withCredentials([usernamePassword(credentialsId: 'myVaultAppRole', usernameVariable: 'ROLEID', passwordVariable: 'SECRET')]) {
            stage('Sonar Analysis') {
                echo 'running sonar analysis'
                sh """
                    mvn sonar:sonar -DroleId=ROLEID -DsecretId=SECRET
                """
            }

            stage('Update PostgreDB') {
                echo 'running liquibase'
                sh """
                    mvn compile -DroleId=ROLEID -DsecretId=SECRET
                """
            }

            stage('Deploying app to K8s') {
                echo 'deploying app via kubectl'
                sh """
                    mvn vault:pull -D"vault.outputMethod=EnvFile" -D"vault.roleId=$ROLEID" -D"vault.secretId=$SECRET"
                    source .env
                    kubectl apply -f deployment.yaml --namespace ns-my-namespace
                """
            }
        }
    }
}
```

# 2. Production environments
Let's say you have an application running on K8s, and you deploy your app and its secrets via a jenkins CD pipeline.
Best way to upload your secrets to K8s is the vault plugins or agents. 
<p><img src="../assets/images/hashicorp_jenkins_kubernetes.png"/></p>

But this will require using a new config file where you declare your secrets (agent-config.hcl and secret-template.yaml).
Your pipeline will look like this.
```groovy
stage('vault agent'){
    withEnv(["VAULT_NAMESPACE=myNameSpace"]){}
        withCredentials([usernamePassword(credentialsId: 'appRole', usernameVariable: 'ROLEID', passwordVariable: 'SECRET')]) {
            sh """
                echo ROLEID > roleid
                echo SECRET > secretid
                vault agent -config ./agent-config.hcl
            """
        }
    }
}
```

Instead with the help of vault-maven-plugin, you can do this.
```groovy
stage('vault maven plugin'){
    withCredentials([usernamePassword(credentialsId: 'appRole', usernameVariable: 'ROLEID', passwordVariable: 'SECRET')]){
        sh"""
            mvn vault:pull -D"vault.outputMethod=EnvFile" -D"vault.roleId=$ROLEID" -D"vault.secretId=$SECRET"
            source .env #to make kubectl work with KUBECONFIG env variable
            kubectl create secret generic my-app-secret --from-env-file=./.env
        """
    }
}
```

For other environments like VMs or bare metal servers, or if you do not need to store you secrets on K8s cause you do not need a quick startup,
you can use this plugin to run your application via maven, just like you do in local (see next chapter below).

If you are interested in all the best practices for managing your secrets on PROD, see the blog post [here](https://homeofthewizard.github.io/secrets-in-java),

# 3. Local development environments

If you want to debug your spring application locally, there are two ways to do this:
1.  In IntelliJ, you can run and debug maven goals, see [here](https://www.jetbrains.com/help/idea/run-debug-configuration-maven.html).  
    If you want to debug, the catch is not to let spring boot plugin to fork its JVM from the initial maven process. see [here](https://youtrack.jetbrains.com/issue/IDEA-175246)

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <fork>false</fork>
    </configuration>
</plugin>
```

<p><img src="../assets/images/intellij_debug.PNG"/></p>

2. Or, if you want to fork the JVM and debug only your application, you can set a remote debugger on any IDE by adding spring boot JVM args for the debugger connexion.

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <jvmArguments>-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8282</jvmArguments>
    </configuration>
</plugin>
```
