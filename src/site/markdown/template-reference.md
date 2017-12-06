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

where the `name` attribute is required and used to provide a name for the deployment config in the local repository. The
`deployconf` element contains a list of tasks that should be applied to the artifact. Currently supported tasks are:

* `<properties>`
* `<filter>`

## Properties Task

The properties task will create a Java Properties file using `ISO-8859-1` encoding with all properties listed in the
element. Note that any existing file will be completely replaced.

The properties task looks like:

```
<properties path="...">
  <property group="...">
    <name>...</name>
    <description>...</description>
    <condition>...</condition>
    <default>...</default>
    <value>...</value>
  </property>...
</properties>
```

`path`(optional)

: The *path* attribute identifies the path of the properties file in the artifact. It should match the path displayed
using the `jar tvf` command. The path attribute may be omitted to define properties in configuration groups that are
only used in property expressions.

`group`(optional)

: The *group* attribute binds the property to a [Configuration Group](#Configuration_Groups).

`name`(required)

: The Java Property Name. It's also used as configuration property name when running deployconf in *interactive* mode.

`description`(required)

: The configuration property description that is displayed when running deployconf in *interactive* mode.

`condition`(optional)

: If present, the condition is evaluated as a JavaScript expression and is used to determine if the property should
be enabled or disabled. A disabled property is ignored when processing the deployment template.

`default`(optional)

: The default value to used for the configuration property. This is used when running deployconf in *interactive* mode.

`value`(optional)

: The value of the configuration property. If the property is part of a configuration group, property expressions
referring to other properties in the same group will be expanded when the value is used. If the property has a value in
the deployment template it will be used *as is* in the deployment config. This means that you can provide properties
that normally are hidden unless deployconf is running in *force-interactive* mode.

## Filter Task

The filter task will filter a file in the artifact and replace all tokens that match the given regex with the
corresponding value.

The Filter Task looks like:

```
<filter path="..." encoding="...">
  <token group="...">
    <name>...</name>
    <regex>...</regex>
    <description>...</description>
    <condition>...</condition>
    <default>...</default>
  </token>...
</filter>
```

`path`(required)

: The *path* attribute identifies the path of the file in the artifact. It should match the path displayed using the `jar
tf` command.

`encoding`(optional)

: The *encoding* attribute specifies the encoding to use when processing the given path. Default
encoding is `UTF-8`.

`group`(optional)

: The *group* attribute binds the token to a [Configuration Group](#Configuration_Groups).

`name`(required)

: The configuration property name used when running deployconf in *interactive* mode

`regex`(required)

:  The regular expression in *java.util.regex.Pattern* format that is used to find the token

`description`(required)

: The configuration property description that is displayed when running deployconf in *interactive* mode.

`condition`(optional)

: If present, the condition is evaluated as a JavaScript expression and is used to determine if the token should
be enabled or disabled. A disabled property is ignored when processing the deployment template.

`default`(optional)

: The default value to used for the configuration  property. This is used when running deployconf in *interactive* mode.

`value`(optional)

: The value for the configuration property. If the token is part of a configuration group, property expressions
referring to other properties in the same group will be expanded when the value is used. If the property has a value in
the deployment template it will be used *as is* in the deployment config. This means that you can provide tokens that
normally are hidden unless deployconf is running in *force-interactive* mode.

## Configuration Groups

When using the *group* attribute, the property or token is declared to be part of a *Configuration Group*. This means
that the actual configuration value is stored in the given configuration group and not in the deployment template
itself.

This means that the configuration value may be reused in another property or token with the same name that belongs to
the same configuration group and thus allows for reusing the same value in multiple properties and tokens. This
mechanism works both in the same deployment template and between different artifacts using it's own template, allowing
for sharing values between a group of artifacts. The group itself is stored in the deployconf repository as a standard
Java Properties file.
