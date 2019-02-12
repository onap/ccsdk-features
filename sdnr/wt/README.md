# wt - wireless transport microservices

## apigateway

The API gateway provides WEB access to all wt related web service providers to prevent cross site requests. The Opendaylight default UI port is extendet by additonal URIs.

  * /database to access elasticsearch
  * /aai to access AAI
  * /ms to access mediator servers

## devicemodel

Model classes, specified by ONF Model and provided as yang files for Java class generation for NETCONF devices.

## websocketmanager2

Notification service for browser clients.

## devicemanager

Management application for NETCONF devices.

## odlux

Web applications for user operations for devices

## helpserver

Providing help pages for related web applications

## featureaggregator

karaf feature with name sdnr-wt-feature-aggregator to start all services.
