# wt - wireless transport microservices

## apigateway (osgi)

The API gateway provides WEB access to all wt related web service providers to prevent cross site requests. The Opendaylight default Web server is extended by additional URIs.

  * /database to access elasticsearch
  * /aai to access AAI

## common (osgi)

Common classes, included into most bundles

## data-provider (osgi)

Interface to devicemanager database. In Frankfurt database is elasticsearch.

## devicemanager (osgi)

Devicemanager services, used by specfic devicemanagers for NETCONF devices.

## devicemanager-specific (osgi)

Devicemanager implementation for specfic devicemodel. Today available are:
  * onf: ONF Core model
  * oran: O-RAN model
  * gran: 3GPP model

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
