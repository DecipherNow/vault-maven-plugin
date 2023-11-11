---
layout: page
title: About Vault Maven Plugin
nav_order: 1
---

# No need to store your credentials locally anymore 
{: .fs-9 }

The Vault Maven Plugin allows you to fetch credentials from HashiCorp Vault and inject them as environment variables. Allowing your program to use them directly, instead of managing the credentials locally on each environment (developpers' local, CI pipelines, or production environment)
{: .fs-6 .fw-300 }

[![Quick Start](https://img.shields.io/badge/-Quick%20Start%20%F0%9F%9A%80-blue?style=for-the-badge&logo=rocket)](/vault-maven-plugin/usage.html)
[![Github button](https://img.shields.io/badge/-View%20it%20on%20Github-gray?style=for-the-badge&logo=github)](https://github.com/HomeOfTheWizard/vault-maven-plugin)
[![Maven Central Releases](https://img.shields.io/badge/-Maven%20Releases-orange?style=for-the-badge&logo=apache%20maven)](https://central.sonatype.com/artifact/com.homeofthewizard/vault-maven-plugin)
[![JavaDoc](https://img.shields.io/badge/-JavaDocs%F0%9F%93%84-green?style=for-the-badge)](https://www.javadoc.io/doc/com.homeofthewizard/vault-maven-plugin/latest)


---


## What is it ?
Basically, a maven plugin, hence an application that can be executed via maven command line,  
like `mvn vault:pull` to retrieve your application's secrets from Hashicorp Vault for you,  
or `mvn vault:push` to push new secrets to your Vault instance.     
   
By providing a simple configuration via a `pom.xml`, giving the necessary identifications and secrets' keys to fetch, it fetches them for you and inject them as environment variables to the execution context of maven.  
  
As a result your application can use its secrets directly from those env variables, which are existing only during it's execution, instead of storing them locally. Which is more secure :policeman:  
The only thing to manage is the credentials to login into Vault, and the list of secret keys you need :massage_man:.  

## Why and When to use it ?
Today, most of the software applications are communicating with other applications, using some sort of credentials for identifying themselves before establishing a secure communication,    
or some encryption keys for securing the communication itself.  
[Hashicorp Vault](https://www.vaultproject.io/) allows us to securely store those secrets, and share them with necessary counterparts.
Those counterparts may be developers who want to run the application on their local environments, or the application's different execution environments (Test, Production, CI ect...)  

Organisations that has a Hashicorp Vault, in general, are already using a cloud based architecture.    
Hence, already have some ways for the application's credentials to be fetched automatically from Vault, instead of storing them locally on each environment separately.  
Ex: Orchestrated Container environments like Kubernetes,  
or CI tools like Jenkins have a Vault plugin to do so.  

{: .warning }
All those plugins/tools, all require you to declare your secrets on their own configuration file, causing duplication among different config files.

{: .important }
By using this plugin to fetch your secrets, you can unify all your configuration and secrets declaration in a single file, the pom.xml.  
So everything, except the credentials to vault, can be stored on version control.  
And if you use Github PAT authentication, even that can be on version control!  

{: .warning }
The idea behind using those plugins/tools, like jenkins Vault plugin or the Vault Agent that is used to save your secrets on K8s, is to fetch the credentials in advance, so that they are available right away at the start time of your application, making it to boot faster.

{: .important }
But there are some cases, for example batch applications, where we may prefer to tradeoff for the simplicity of configuration over the speed of startup :massage_man:.  
Just keep in mind that fetching the secrets in time of execution has an implication of I/O, network and additional time of startup.  

But still If you are sensible to the boot time of your application, lets say you have a web scaling app running on K8s,  
You can still use this plugin, still with a single definition of your secrets in the pom.xml, to create your secret.yaml for K8s at its deployment time instead of the boot time. :wink:   


But it may also be the case that : 
* Your enterprise's IT infrastructure does not have such tools yet,  
* It may be in a transition phase, or maybe some legacy applications that are not compatible with such tools.  
* For some reason even if you have such tools, the Vault plugins are not available.   
* **Such tools are not available on developers' local environments,**  
where lots of enterprises still provide Windows PCs without a Docker or K8s.

This plugin aims to help all those cases :santa:.
 

## Example use cases
Some CI tools that already working with maven, for example liquibase or sonar-scanner which both have maven plugins,    
are the best suite for using the Vault Maven Plugin.  
  
Java applications that use maven to build/package can also profit from it.     
Spring already have a Spring-Vault library providing the same help. But it may be the case that you do not use spring-cloud.    
  
Even if you do not use maven at all, you can create a `pom.xml` just for the configuration of this plugin that will fetch the secrets,   
and pass the secrets to your application via some other way.  
Example: another maven plugin that exports the environment variables to the execution context of your application.      
   
We provide an example for each use cases above, [further](/vault-maven-plugin/examples.html) in this documentation.  