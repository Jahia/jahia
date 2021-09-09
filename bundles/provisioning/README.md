# Provisioning service

This service provides scripting capabilities to provision Jahia at startup. 
This can also be used to manage modules and configurations at runtime, import sites, or to script administrative tasks.

A script can be triggered at startup by setting the `executeProvisioningScript` system property. It should contain a valid URL pointing to a script.

## Syntax

The service accepts a URL that should point to a valid script. Both JSON and YAML formats are supported. A script is basically
a list of commands. Each command is a list of key/value pair.

Both examples are equivalent :

JSON :
```json
[
  {
    "installBundle": "mvn:org.jahia.modules/article/3.0.0", 
    "autoStart": true
  },
  {
    "installBundle": "mvn:org.jahia.modules/bookmarks/3.0.0"
  }
]
```

YAML :
```yaml
- installBundle: "mvn:org.jahia.modules/article/3.0.0" 
  autoStart: true
- installBundle: "mvn:org.jahia.modules/bookmarks/3.0.0"
```

## Variables interpolation

You can use anywhere in the script variables interpolation with the format : `${type:key:-default}`
These interpolations are based on https://commons.apache.org/proper/commons-text/apidocs/org/apache/commons/text/StringSubstitutor.html. 
All the default replacements are available :

```
"Base64 Decoder:        ${base64Decoder:SGVsbG9Xb3JsZCE=}"
"Base64 Encoder:        ${base64Encoder:HelloWorld!}"
"Java Constant:         ${const:java.awt.event.KeyEvent.VK_ESCAPE}"
"Date:                  ${date:yyyy-MM-dd}"
"DNS:                   ${dns:address|apache.org}"
"Environment Variable:  ${env:USERNAME}"
"File Content:          ${file:UTF-8:src/test/resources/document.properties}"
"Java:                  ${java:version}"
"Localhost:             ${localhost:canonical-name}"
"Properties File:       ${properties:src/test/resources/document.properties::mykey}"
"Resource Bundle:       ${resourceBundle:org.example.testResourceBundleLookup:mykey}"
"Script:                ${script:javascript:3 + 4}"
"System Property:       ${sys:user.dir}"
"URL Decoder:           ${urlDecoder:Hello%20World%21}"
"URL Encoder:           ${urlEncoder:Hello World!}"
"URL Content (HTTP):    ${url:UTF-8:http://www.apache.org}"
"URL Content (HTTPS):   ${url:UTF-8:https://www.apache.org}"
"URL Content (File):    ${url:UTF-8:file:///${sys:user.dir}/src/test/resources/document.properties}\n"
"XML XPath:             ${xml:src/test/resources/document.xml:/root/path/to/node}"
```

And :

```
"Jahia properties:      ${jahia:processingServer}"
```

## Available operations

### Install bundle

Install bundle from a URL (supporting all pax-url protocols). This action is triggered when `installBundle` is set to a valid URL.

These additional options are available :
- `target`: The cluster group name where the operation will be done (unset to execute on all nodes)
- `autoStart`: Autostart the bundle after installation
- `uninstallPreviousVersion`: Uninstall all other versions
- `forceUpdate`: If true, module will be updated if it is already installed. Default is `false`
- `if`: Optional condition that must be met to do the operation

Examples :

```yaml
- installBundle: "mvn:org.jahia.modules/article/3.0.0"
  autoStart: true
  uninstallPreviousVersion: true
```

```yaml
- installBundle: "file:/tmp/example-1.0.0.jar"
```

Multiple bundles can be installed with the same command and options :

```yaml
- installBundle:
  - 'mvn:org.jahia.modules/forms-snippets-extension/3.0.0'
  - 'mvn:org.jahia.modules/forms-nocss-theme/2.0.0'
  - 'mvn:org.jahia.modules/font-awesome/6.0.0'
  - 'mvn:org.jahia.modules/forms-prefill/2.0.0'
  - 'mvn:org.jahia.modules/forms-core/3.2.0'  
  autoStart: true
  uninstallPreviousVersion: true
```

Or using inline format :

```yaml
- installBundle: ['mvn:org.jahia.modules/forms-prefill/2.0.0','mvn:org.jahia.modules/forms-core/3.2.0']
  autoStart: true
  uninstallPreviousVersion: true
```

If some bundles need different options, you can use a nested block, using `url` key for the url itself :

```yaml
- installBundle:
  - 'mvn:org.jahia.modules/forms-snippets-extension/3.0.0'
  - 'mvn:org.jahia.modules/forms-nocss-theme/2.0.0'
  - { url: 'mvn:org.jahia.modules/font-awesome/6.0.0', autoStart: false}
  - 'mvn:org.jahia.modules/forms-prefill/2.0.0'
  - 'mvn:org.jahia.modules/forms-core/3.2.0'  
  autoStart: true
  uninstallPreviousVersion: true
```

The autoStart option will start all listed bundles once all of them have been installed. 
Then, previous versions will be uninstalled.

#### Conditional

You can add a condition on install, by adding the `if` option. The condition is a groovy expression, as described [below](#script-includes-and-conditional-flow)

```yaml
- installBundle: 'mvn:org.jahia.modules/sdl-generator-tools/2.1.0'
  if: "'${jahia:operatingMode}' == 'development'"
```

#### Additional syntax

As as alternate to `autoStart: true`, you can use `installAndStartBundle` :

```yaml
- installAndStartBundle: "file:/tmp/example-1.0.0.jar"
```

An additional command `installOrUpgradeBundle` is also available for upgrades. Its behaviour changes if it's a new install or an upgrade :
- If no version of this bundle is installed, it behaves like `installAndStartBundle`.
- If another version of the bundle is installed, it will install the new version, uninstall the previous ones (as with `uninstallPreviousVersion`) 
  and restore the state of the previous version (started or stopped)

```yaml
- installOrUpgradeBundle: "mvn:org.jahia.modules/article/3.0.0"
```

### Uninstall bundle

Uninstall a bundle, based on a key composed of the symbolic name and optionally the version : `<symbolic-name>[/<version>]`

These additional options are available :
- `target`: The cluster group name where the operation will be done (unset to execute on all nodes)

```yaml
- uninstallBundle: "article/3.0.0"
```

### Start/stop bundle

Start or stop a bundle, based on a key composed of the symbolic name and optionally the version : `<symbolic-name>[/<version>]`

These additional options are available :
- `target`: The cluster group name where the operation will be done (unset to execute on all nodes)

```yaml
- stopBundle: "article/3.0.0"
- startBundle: "article/3.0.1"
```

A list of bundles can be passed :

```yaml
- startBundle: ["article/3.0.1", "news/3.0.0"]
```


### Install / edit configuration

Install or edit configuration. Install a configuration from a URL :

```yaml
- installConfiguration: "file:/tmp/org.jahia.services.usermanager.ldap-config-rqa5.cfg"
```

You can also create or edit configurations by using `editConfiguration` with the configuration pid. If the pid is a factory pid, you will need to specify the `configIdentifier` value, or use the syntax `<factory-pid>-<config-Id>` 

```yaml
- editConfiguration: "org.jahia.modules.test"
  configIdentifier: "id1"
  properties: 
    user.uid.search.name: "dc=jahia,dc=com"
    group.search.name: "dc=jahia,dc=com"
    url: "ldap://rqa5.jahia.com:389/"
```

You can also directly put the content of the properties file in the script (the formatting will be kept for config creation only) :

```yaml
- editConfiguration: "org.jahia.modules.test"
  configIdentifier: "id1"
  content: |
    # LDAP configuration
    user.uid.search.name:dc=jahia,dc=com
    group.search.name:dc=jahia,dc=com
    url=ldap://rqa5.jahia.com:389/
```

### Import zip

Import a zip from the export at the specified url. The file must be a zip expot.

```yaml
- import: "file:/Users/toto/users.zip"
```

You can add the `rootPath` option to specify where the content will be imported, or leave it undefined to import in `/`

Multiple import files can be specified.

### Import site

Import a site from the export at the specified url. The file must be a site export, in zip format.

```yaml
- importSite: "file:/Users/toto/mySite_export_2020-12-30-10-37/mySite.zip"
```

Multiple import files can be specified.

### Create site

Create a site with the provided site info:
 - siteKey
 - title: default is the value of `siteKey`
 - language: default is `en`
 - templateSet
 - modulesToDeploy
 - language
 - serverName: default is `localhost`
 - serverNameAliases
 - description
 - siteAdmin
````yaml
- createSite:
  siteKey: "test1"
  templateSet: "templates-system"
  modulesToDeploy: 
    - "facets"
    - "channels"
  language: "en"
````


### Enable module on site

Enable modules on a site :

```yaml
- enable: "news"
  site: "digitall"
```

Both arguments can be list, allowing to install multiple modules on multiple sites :

```yaml
- enable: ["news", "article"]
  site: ["digitall", "demo"]
```

### Add maven repository

Add a maven repository in the maven pax-url configuration. The repository will be available when using a `mvn://` URL.

```yaml
- addMavenRepository: "https://user:xxx@devtools.jahia.com/nexus/content/groups/enterprise@id=jahia-enterprise@snapshots"
```

### Features management

Install or uninstall karaf features : 

```yaml
- installFeature: "transaction-api"
- uninstallFeature: "webconsole"
```

You can also add a feature repository : 
```yaml
- addFeatureRepository: "mvn:org.ops4j.pax.jdbc/pax-jdbc-features/LATEST/xml/features"
```

### Karaf command 

Execute a karaf command. This can be useful to perform a command which is not available as a dedicated operation. The command output will be displayed in the logs.

```yaml
- karafCommand: "bundle:refresh news"
- karafCommand: "bundle:list"
```

You can specify an optional timeout in ms (default 1s):

```yaml
- karafCommand: "shell:exec git clone https://github.com/Jahia/personal-api-tokens.git" 
  timeout: 10000
```

### Execute scripts

It's possible to execute `.groovy` or `.graphql` scripts using the `executeScript` command

```yaml
- executeScript: "http://myserver.com/a-graphql-query.graphql"
- executeScript: "file:/tmp/my-new-script.groovy"
```

Multiple scripts can be specified.

### Sleep

You can tell the script to wait for a specific amount of time (in ms) before proceeding with the next instructions :

```yaml
-sleep: 1000
```

### Script includes and conditional flow

It's possible to include another script with the `include` operation :
```yaml
- include: "http://myserver.com/modules-provisioning.yaml"
```

You can add condition in the script based on any groovy expression values :
```yaml
- if: org.jahia.settings.SettingsBean.getInstance().isClusterActivated()
  do: 
    - installFeature: "dx-clustering"
```

## Adding new operations

Adding a new operation can be done by extending the `org.jahia.services.provisioning.Operation` class and exposing it as an OSGi service.

## Packages and provisioning script

All packages based on `jahia-packages-parent` from 8.0.3.0 have an associated provisioning script, generated when building.
It's then possible to install a "package" by just referencing its script :

```yaml
- include: "mvn:org.jahia.packages/forms-package/3.2.1/yaml/provisioning"
```
