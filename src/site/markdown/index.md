# Introduction

Deployconf is a Command Line Tool used to configure Java artifacts for the actual deployment environment. It is designed
to be part of the deployment process to both improve the configuration experience and prevent common mistakes.

## The Developer
 
As a Developer, you do your best to deliver your project on time and on budget, meeting and preferable exceeding your
customer's expectation.

You have a deterministic build process that delivers the correct artifacts to the Deployer and provides well-written
documentation how to correctly configure the project for the production environment.

Normally, the configuration is the same between releases and the Deployer applies the same changes to your artifacts
each time for the target environment. In the best of all worlds, using a script.

The problem is that over time, the configuration changes. New installation properties are needed and old ones are
removed.

Old configuration properties just clutter the configuration and obscure the configuration but otherwise is not much of a
problem. Missing new properties, most certainly, cause the deployment to fail, resulting in service downtime and support
calls for yourself.

You do your best to prevent these situations by highlighting new configuration properties in your release notes, but the
Deployer often only sees them after the deployment failed. You're not completely foolproof either, it sometimes happens
that you actually forget to update your documentation.

What you really want is a configuration tool that detects missing configuration properties and alerts the Deployer
before causing system downtime.

## The Deployer

As a Deployer, receiving a new distribution from your developers, the standard procedure is to extract the distribution
to a temporary directory and run a homegrown script that tries to ensure that you correctly configure and applies
environment-specific information to the software. Normally, this means reusing an external property file and setting an
environment variable so the software will read the file when it starts. Most of the time this works great, until the
software suddenly refuses to start with a large stack trace in your logs.

You know your environment but have limited java-experience so trying to interpret the logs is out of your league. You
grab your phone and call the Developer for help. You find out that you missed a new configuration property so you
quickly add it to your property file and run your script again. The system now starts and all seems to be well. Now you
only have to distribute the configuration to all other nodes in your deployment cluster.

It's all happened before, but every time you think; what if there was a way for your script to detect a missing property
and alert you before trying to deploy the new software release.

Since the software is a web application in the form of a war-file, it would also be great if the configuration could be
merged into the actual war-file since it would allow you to deploy the same war-file to the application server cluster,
using the provided tools, without having to manually distribute the configuration changes to all nodes.

## DeployConf

Deployconf is an attempt to ease the burden for both Developers and Deployers.  The Developer provides a
self-documenting deployment template that describes all configuration properties necessary for the application, along
with default values, if any. The template is embedded in the artifact.

The deployment template also contains rules for how the configuration property value should be applied to the generated
artifact, supporting Java properties files as well as regex-style substitutions for any types of files.

The Deployer, when preparing the artifact for deployment, runs the deployconf utility with the artifact as input. The
input is processed together with previous deployment configuration from a local repository to generate a new artifact as
output with embedded configuration as described in the deployment template.  The new artifact is then ready for
deployment. As long as the deployment template is unchanged from the previous release, the output is automatically
configured and ready for deployment.

Deployconf may be run either in interactive or non-interactive mode.  In the default non-interactive mode, deployconf
fails with an error message when a configuration property is missing, allowing the Deployer to manually editing the
local repository with the correct information.

In the interactive mode, deployconf asks the Deployer for the required information and then updates the local
repository.
