# Developing a ODLUX application

## Introduction

ODLUX bundle contains the Browser based Grapical User Interface for SDN-R.
ODLUX is available as OSGi bundle that is running in Opendaylight Karaf environment, using the configured jetty server of Opendaylight.
Since ONAP Frankfurt a second WEB Server setup "sdncweb" is available, that extracts the JavaScrip files.

## Prerequisites

Actual version in framework pom.xml in the frontend-maven-plugin definition.
  * Node
  * Yarn
  * Lerna

You can install these globally or let it be installed by maven due "mvn clean install"

* Maven: 3 or higher
* Java: 8

## Dev-Environment Installation

 * install NodeJS LTS https://nodejs.org/en/ or via packetmanager
 * sudo npm install -g yarn
 * sudo yarn global add lerna
 * get framework from repository: git clone https://gerrit.onap.org/r/ccsdk/features
 * in features/sdnr/wt/odlux you find a structure like this:
 ```
  odlux
  |-apps
  |-core
  |-framework

 ```
 * go to features/sdnr/wt/odlux/apps and create your app:
 ```
 mvn archetype:generate -DarchetypeGroupId=org.onap.ccsdk.features.sdnr.wt \
  -DarchetypeArtifactId=odlux-app-archetype \
  -DgroupId=<groupId> \
  -DartifactId=<artifactId> \
  -Dversion=<version> \
  -DappName=<applicationName>
 ```

 * your start folder for your web application is src/
 * in src2/main/java are located the Java files and in src2/main/resources/ is the blueprint located
 * with ```yarn start``` you can run your application due runtime in your application folder
 * by default this will run on http://localhost:3100/index.html
 * if you have added new dependencies you have to run ```lerna bootstrap``` in odlux/
 * build your app for development version you can use ```yarn run build``` or ```yarn run build:dev```
 * build for karaf with ```mvn clean install```


## Including app into karaf environment

 * copy maven repository files to karaf repository e.g.: ```cp ~/.m2/repository/path/of/groupId/artifactId $KARAF_HOME/system/path/of/groupId/```
 * check if odlux-core is started in karaf console: ```feature:list | grep odlux```
 * if not install: ```sdnr-wt-odlux-core-feature```
 * start your app in karaf console: ```bundle:install -s mvn:<groupId>/<artifactId>/<version>```

## Including into ONAP sdnc docker container

 * add maven module to odlux/pom.xml
 * add dependency to odlux/apps/app-feature/pom.xml and odlux/apps/app-installer/pom.xml
 * build odlux/pom.xml
 * this will automatically package your app into the packaged zip file of the installer

## Details

### Default menu positions

 * from 0 for top to 999 for bottom.

```
0    Connect
10    Fault
20    Maintenance
30    Configuration
40    Protection
50    Performance
60    Security
70    Inventory
80    Topology
90    Mediator
100    Help
```

### blueprint.xml

```
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0">
    <reference id="loadersvc" availability="mandatory" activation="eager" interface="org.onap.ccsdk.features.sdnr.wt.odlux.model.bundles.OdluxBundleLoader"/>
    <bean id="bundle" init-method="initialize" destroy-method="clean" class="org.onap.ccsdk.features.sdnr.wt.odlux.bundles.MyOdluxBundle">
        <property name="loader" ref="loadersvc"/>
        <property name="bundleName" value="demoApp"/>
        <property name="index" value="999"/>
    </bean>
</blueprint>
```
 * bundleName defines the applicationName => default javascript file: <applicationName>.js
 * index defines the menu position.

### MyOdluxBundle.java

 * is just for getting access to the resources of its bundle (implemented because of OSGi access restrictions)

### pom.xml

 * The pom.xml in the framework subdirectory is the reference for ODLUX creation. [framework pom](framework/pom.xml)
 * The node and yarn versions are specified
 * A specific variant of "frontend-maven-plugin" is used to create the environment to compile to javascript. This modified frontend-maven-plugin installs node, yarn and (optionally lerna) to compile the typescript sources to javascript. These will be build into the dist folder.
