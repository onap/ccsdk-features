# wt - wireless transport microservices

ODL version: scandium sr2

## common (osgi)

Common classes, included into most bundles

## common-yang (osgi)

yang files converted into jar bundles. Preferred way to include basic yang specs into a devicemanager project.

## data-provider (osgi)

Interface to devicemanager database. In Frankfurt database is elasticsearch.

## devicemanager (osgi)

Devicemanager services, used by specfic devicemanagers for NETCONF devices.

## devicemanager-specific (osgi)

Devicemanager implementation for specfic devicemodel.

## featureaggregator (osgi)

karaf features
  * sdnr-wt-feature-aggregator to start all services
  * sdnr-wt-feature-aggregator-devicemanager to start devicemanager service

## helpserver (osgi)

Providing help pages for related web applications

## netconfnode-state-provider (osgi)

Provide netconf state information to devicemanager and mountpoint-state-provider

## odlux (osgi)

Web applications for user operations for devices

## websocketmanager2 (osgi)

Notification service for browser clients.

## readthedocs

readthedocs - Providing documentation. Scripts to convert documentation within helpserver to the [ccsdk/features]/doc folder
~
~
~
~
