# Deployment Template Reference

The Deployment Template is an XML-file describing each configuration properties that should be applied to the artifact
before deployment. 

The basic structure is:

```
<?xml version="1.0" encoding="UTF-8"?>
<deployconf name="...">
	<TASK>...</TASK>...
</deployconf>
```

where the `name` attribute is required and used to provide a name for the deployment config in the local
repository. The `deployconf` element contains a list of tasks that should be applied to the artifact. Currently
supported tasks are:

* `<properties>`
* `<filter>`

## Properties Task

The properties task will create a Java Properties file using `ISO-8859-1` encoding with all properties listed in the
element. Note that any existing file will be completely replaced.

The properties task looks like:

```
<properties path="...">
  <property>
    <name>...</name>
    <description>...</description>
    <default>...</default>
    <value>...</value>
  </property>...
</properties>
```

The *path* attribute identifies the path of the properties file in the artifact. It should match the path displayed using the
`jar tvf` command.

`name`(required)

: the Java Property Name. It's also used as configuration property name when running deployconf in *interactive* mode.

`description`(required)

: The configuration property description. This is displayed when running deployconf in *interactive* mode.

`default`(optional)

: The default value to used for the configuration property. This is used when running deployconf in *interactive* mode.

`value`(optional)

: The initial value for the configuration property. If the property has a value in the deployment template it will be
used *as is* in the deployment config. This means that you can provide properties that normally is hidden unless
deployconf is running in *force-interactive* mode.

## Filter Task

The filter task will filter a file in the artifact and replace all tokens that match the given regex with the
corresponding value.

The Filter Task looks like:

```
<filter path="...">
  <token>
    <name>...</name>
    <regex>...</regex>
    <description>...</description>
    <default>...</default>
  </token>...
</filter>
```
  
The *path* attribute identifies the path of the file in the artifact. It should match the path displayed using the
`jar tf` command.

`name`(required)

: the configuration property name used when running deployconf in *interactive* mode

`regex`(required)

:  The regular expression in *java.util.regex.Pattern* format that is used to find token

`description`(required)

: The configuration property description. This is displayed when running deployconf in *interactive* mode.

`default`(optional)

: The default value to used for the configuration  property. This is used when running deployconf in *interactive* mode.

`value`(optional)

: The initial value for the configuration property. If the property has a value in the deployment template it will be
used *as is* in the deployment config. This means that you can provide properties that normally is hidden unless
deployconf is running in *force-interactive* mode.

