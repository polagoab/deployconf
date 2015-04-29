# Developer Usage

When the Deployer runs the Deployconf utility on an artifact, it will look for the embedded deployment descriptor.  The
deployment descriptor is an XML-file with the name `deployment-template.xml` and should be located in the `/META-INF`
directory of the artifact. The deployment descriptior should describe each configuration properties that needs to b
configured as part of the Deployment process and the Developer is responsible for updating the descriptor when the
deployment requrirement changes.

Below is an example descriptor that declare two configuration properties:

```
<?xml version="1.0" encoding="UTF-8"?>
<deployconf name="org.polago.deployconf.demo">
  <properties path="WEB-INF/classes/deploy.properties">
    <property>
      <name>backendService</name>
      <description>The URL to the backend service</description>
      <default>https://localhost/backend</default>
    </property>
  </properties>
  <filter path="WEB-INF/web.xml">
    <token>
      <name>tmpDir</name>
      <regex>@tmpDirl@</regex>
      <description>The path to the temporary directory used by the application</description>
      <default>/var/tmp</default>
    </token>
  </filter>
</deployconf>
```

The descriptor contains two different tasks. The `properties`-task will always overwrite the properties file with all
the properties specified. The `filter`-task will filter the specified file and replace all occurencies of the specified
token with the corresponding values.

The `name` attribute should uniqly identify your artifact. If you publish your artifact to the
[The Central Repository](http://search.maven.org/), a good strategy is to use your groupdId and artifactId as the
name. The name will be used to locate the descriptor in Depolyconf's local repository.

For more information, see [Deployment Descriptor Reference](descriptor-reference.html).
