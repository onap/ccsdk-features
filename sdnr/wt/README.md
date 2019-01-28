# wt - wireless transport microservices

## apigateway

The API gateway provides WEB access to all wt related web service providers to prevent cross site requests. The Opendaylight default UI port is extendet by additonal URIs.

  * /database to access elasticsearch
  * /aai to access AAI
  * /ms to access mediator servers

## devicemodel

Model classes, specified by ONF Model and provided as yang files for Java class generation for NETCONF devices.

## websocketmanager

Notification service for browser clients.
