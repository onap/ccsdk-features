# sdnr-wt-data-provider

Data-provider bundle manages all access to SDN-R database.
Database is actually ElasticSearch.

## Initilization

Central SDN-R script initializing and preparing the database BEFORE SDN-R usage.

### sdnr-dmt.jar Tool

Initialization and migration tool for elasticsearch database

Reference: https://wiki.onap.org/display/DW/SDN-R+Data+Migration+Tool

Example:
<code>java -jar $ODL_HOME/system/org/onap/ccsdk/features/sdnr/wt/sdnr-wt-data-provider-setup/0.7.1-SNAPSHOT/sdnr-dmt.jar -c init -db $SDNRDBURL -dbu $SDNRDBUSERNAME -dbp $SDNRDBPASSWORD</code>


### ES-INIT Script (deprecated)

Name: es-init.sh
Dir: provider/src/main/resources

  * Elasticsearch database initialization script using bash and curl commands.
  * Intended to be executed in a linux command line environment
  * Needs to be executed before the database is used to initialize type mapping of the indexes
  * The script is included into the ZIP created by sdnr-wt-feature-aggregator to be available in containers
