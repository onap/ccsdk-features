# sdnr-wt-data-provider

Data-provider bundle manages all access to SDN-R database.
Database is actually ElasticSearch.

## ES-INIT Script

Central SDN-R script initializing and preparing the database BEFORE SDN-R usage.

Name: es-init.sh
Dir: provider/src/main/resources

  * Elasticsearch database initialization script using bash and curl commands.
  * Intended to be executed in a linux command line environment
  * Needs to be executed before the database is used to initialize type mapping of the indexes
  * The script is included into the ZIP created by sdnr-wt-feature-aggregator to be available in containers
