# Deployer Usage

When the Deployer receives an artifact to deploy, deployconf must be used to prepare the artifact for
deployment. Deployconf will read the embedded deployment template and compare it to the previous deployment
configuration in the local repository. If no new configuration properties are detected, the deployment configuration is
used to build a new artifact with all configuration properties applied. The new artifact is now self-contained and ready
to be deployed.

If the local deployment configuration contains configuration properties that are no longer present in the artifact's
template, they will be removed.  If the artifact's deployment template contains configuration properties that doesn't
exists in the local deployment configuration, they need to be provided before the new artifact can be created.

To run deployconf in *non-interactive* mode:

```
java -jar deployconf.jar INPUT OUTPUT
```

where `INPUT` is the artifact to preocess and `OUTPUT` is the new artifact to be created. If any configuration
properties is missing in the local deployment configuration, deployconf will exit with status `2` and display the path
to the local repository file that needs to be updated.

To run deployconf in *interactive* mode:

```
java -jar deployconf.jar -i INPUT OUTPUT
```

If any configuration properties is missing in local deployment configuration, deployconf will prompt for the required
information.

You may also run deployconf in *force-interactive* mode:

```
java -jar deployconf.jar -I INPUT OUTPUT
```

In force-interactive mode, deployconf will prompt for all configuration properties using the existing properties as
default value.


To show  help information for deployconf:

```
java -jar deployconf.jar -h
```
