---
layout: page
title: About Vault Maven Plugin
---

# No need to store your credentials locally anymore 
The Vault Maven Plugin allows you to fetch credentials from HashiCorp Vault and inject them as environment variables. Allowing your program to use them directly, instead of managing the credentials locally on each environment (developpers' local, CI pipelines, or production environment)

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
  
As a result your application can to use its secrets directly from those env variables, which are existing only during it's execution, instead of storing them locally. Which is more secure :policeman:  
The only thing to manage is the credentials to login into Vault, and the list of secret keys you need :massage_man:.  

## Why and When to use the plugin ?
Today, most of the software applications are communicating with other applications, using some sort of credentials for identifying themselves before establishing a secure communication,    
or some encryption keys for securing the communication itself.  
Hashicorp Vault allows us to securely store those secrets, and share them with necessary counterparts.  
Those counterparts may be developers who want to run the application on their local environments, or the application's different execution environments (Test, Production, CI ect...)  

{: .warning }
Applications that has a Hashicorp Vault, in general, are already using a cloud based architecture.    
Hence, already have some ways to the application's credentials be fetched automatically from Vault, instead of storing them locally on each environment separately.  
Ex: Orchestrated Container environments like Kubernetes, or CI tools like Jenkins have a Vault plugin to do so.  
The idea behind is usually to fetch the credentials in advance, to speed up the start time of your application.

But it may be the case that your enterprise's IT infrastructure doest not have such tools yet,  
it may be in a transition phase, or maybe some legacy applications that are not compatible with such tools.  
It may be the case that, for some reason even if you have such tools, the Vault plugins are not available.   
It is even more true for developers' local environments,  
where lots of enterprises still provide Windows PCs without a Docker or K8s.
This plugin aims to help those cases :santa:.  
But not only! There are some cases where we don't want to store any configuration related to Vault in any execution environment, but only credentials to access it.    
For example batch applications may prefer to tradeoff for the simplicity of configuration over the speed of startup :massage_man:.

{: .important }
In short, this plugin gives you a simpler and more secure way to manage your secrets.
Just keep in mind that fetching the secrets in time of execution has an implication of I/O, network and additional time of startup. 

## Example use cases
Some CI tools that already working with maven, for example maven plugins, like liquibase or sonar-scanner plugins,    
are the best suite for using the Vault Maven Plugin.  
  
Java applications that use maven to build/package can also profit from it.     
Spring already have a Spring-Vault library providing the same help. But it may be the case that you do not use spring-cloud.    
  
Even if you do not use maven at all, you can create a `pom.xml` just for the configuration of this plugin that will fetch the secrets,   
and pass the secrets to your application via some other way.  
Example: another maven plugin that exports the environment variables to the execution context of your application.      
   
We provide an example for each use cases above, [further](/vault-maven-plugin/examples.html) in this documentation.  