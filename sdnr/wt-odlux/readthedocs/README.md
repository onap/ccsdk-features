# SDNR/WT specific scripts to generate ReadTheDocs content

This repository is creating a maven artifact with SDNR readthedocs documents.

The repository is using the md and png files from "help app" input to create readthedocs structure with rst files. 
Further files are added from src directory of this maven project.

Script "convert.sh" is merging all information to "target/docs". 

Build command: "mvn clean install"

Result for test purpose is bundled in a zip artifact (version may vary):
```
    <groupId>org.onap.ccsdk.features.sdnr.wt.sulfur.sr1</groupId>
    <artifactId>sdnr-wt-readthedocs-installer</artifactId>
    <version>1.4.0-SNAPSHOT</version>
```

## 5GBerlin Test

The artifact is only used for doing a local test of the readme system.

## ONAP gerrit

For synchronization with gerrit the output in target can be used: "target/docs".<br>
The docs content has to be placed in ONAP/cssdk/features repository in docs content. 

It is not a direct copy .. so only "target/docs" parts have to be copied over to "ONAP gerrit:cssdk/features/docs"

ONAP/gerrit has its own propess of creating the ONAP Readthedocs.

## Places


### sdnr/wt/readthedocs
 Scripts and source data for creation of documentation. 
 
 Other sources are located in related implementation directory
 
### docs 
 destination of documentation files to be placed in a SDNC repository. 
 Fully created by script. <- Stimmt nicht
