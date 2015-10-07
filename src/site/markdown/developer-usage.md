# Developer Usage

When the Deployer runs the deployconf utility on an artifact, it will look for the embedded *deployment template*.  The
template is an XML-file with the name `deployment-template.xml` and should be located in the `/META-INF` directory of
the artifact. The deployment template should describe each configuration properties that needs to be configured as part
of the Deployment process and the Developer is responsible for updating the template when deployment requirement
changes.

Below is an example template that declare two configuration properties:

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
      <regex>@tmpDir@</regex>
      <description>The path to the temporary directory used by the application</description>
      <default>/var/tmp</default>
    </token>
  </filter>
</deployconf>
```

The template contains two different tasks. The `properties`-task will always overwrite the properties file with all
the properties specified. The `filter`-task will filter the specified file and replace all occurrences of the specified
token with the corresponding values.

The `name` attribute should uniquely identify your artifact. If you publish your artifact to the
[The Central Repository](http://search.maven.org/), a good strategy is to use the *groupdId* and *artifactId* as the
name. The name will be used to locate the deployment configuration in depolyconf's local repository.

Below is another example template that declare three configuration properties and two of them belongs to the same 
configuration group:

```
<?xml version="1.0" encoding="UTF-8"?>
<deployconf name="org.polago.deployconf.demo">
  <properties path="WEB-INF/classes/deploy.properties">
    <property group="org.polago.deployconf.shared">
      <name>backendService</name>
      <description>The URL to the backend service</description>
      <default>https://localhost/backend</default>
    </property>
  </properties>
  <filter path="WEB-INF/web.xml">
    <token group="org.polago.deployconf.shared">
      <name>tmpDir</name>
      <regex>@tmpDir@</regex>
      <description>The path to the temporary directory used by the application</description>
      <default>/var/tmp</default>
    </token>
  </filter>
  <filter path="WEB-INF/web.xml">
    <token group="org.polago.deployconf.shared">
      <name>backendService</name>
      <regex>@backendService@</regex>
      <description>The URL to the backend service</description>
      <default>https://localhost/backend</default>
    </token>
  </filter>
</deployconf>
```

In this case, the value of the first configuration property will be reused in the last property so when running in 
interactive mode, the Deployer only needs to answer the first question. The actual value for both proerties will be
stored in the given configuration group in the local repository.

For more information, see [Deployment Template Reference](template-reference.html).
