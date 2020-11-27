#!/bin/bash
# ============LICENSE_START========================================================================
# ONAP : ccsdk feature sdnr wt
# =================================================================================================
# Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
# =================================================================================================
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.
# ============LICENSE_END==========================================================================
#
# Version 2
# Usage .. see help below

SDNRNAME=sdnrdb
REPLICAS=1
SHARDS=5
PREFIX=""
VERSION="-v1"
VERBOSE=0
INITFILENAME="Init.script"

#declare -a ALIAS
#declare -a MAPPING

# ------------------------------------------------------------
# Function with definition of mappings
# $1: alias name for index
# $2: mapping properties and additonal parameter for this section

set_definition() {
  def "connectionlog" '{"node-id": {"type": "keyword"},"timestamp": {"type": "date"},"status": {"type": "keyword"}}'
  def "maintenancemode" '{"node-id": {"type": "keyword"},"start": {"type": "date"},"end": {"type": "date"},"description": {"type": "keyword"},"active": {"type": "boolean"}},"date_detection":false}}'
  def "faultlog" '{"node-id": {"type": "keyword"},"severity": {"type": "keyword"},"timestamp": {"type": "date"},"problem": {"type": "keyword"},"counter": {"type": "long"},"object-id":{"type": "keyword"},"source-type":{"type": "keyword"}}'
  def "faultcurrent" '{"node-id": {"type": "keyword"},"severity": {"type": "keyword"},"timestamp": {"type": "date"},"problem": {"type": "keyword"},"counter": {"type": "long"},"object-id":{"type": "keyword"}}'
  def "eventlog" '{"node-id": {"type": "keyword"},"source-type": {"type": "keyword"},"timestamp": {"type": "date"},"new-value": {"type": "keyword"},"attribute-name": {"type": "keyword"},"counter": {"type": "long"},"object-id": {"type": "keyword"}}'
  def "inventoryequipment" '{"date": {"type": "keyword"},"model-identifier": {"type": "keyword"},"manufacturer-identifier": {"type": "keyword"},"type-name": {"type": "keyword"},"description": {"type": "keyword"},"uuid": {"type": "keyword"},"version": {"type": "keyword"},"parent-uuid": {"type": "keyword"},"contained-holder": {"type": "keyword"},"node-id": {"type": "keyword"},"tree-level": {"type": "long"},"part-type-id": {"type": "keyword"},"serial": {"type": "keyword"}}'
  def "historicalperformance24h" '{"node-name":{"type": "keyword"},"timestamp":{"type": "date"},"suspect-interval-flag":{"type":"boolean"},"scanner-id":{"type": "keyword"},"uuid-interface":{"type": "keyword"},"layer-protocol-name":{"type": "keyword"},"granularity-period":{"type": "keyword"},"radio-signal-id":{"type": "keyword"}}'
  def "historicalperformance15min" '{"node-name":{"type": "keyword"},"timestamp":{"type": "date"},"suspect-interval-flag":{"type":"boolean"},"scanner-id":{"type": "keyword"},"uuid-interface":{"type": "keyword"},"layer-protocol-name":{"type": "keyword"},"granularity-period":{"type": "keyword"},"radio-signal-id":{"type": "keyword"}}'
  def "mediator-server" '{"url":{"type": "keyword"},"name":{"type": "keyword"}}'
  def "networkelement-connection" '{"node-id": {"type": "keyword"},"host": {"type": "keyword"},"port": {"type": "long"},"username": {"type": "keyword"},"password": {"type": "keyword"},"core-model-capability": {"type": "keyword"},"device-type": {"type": "keyword"},"is-required": {"type": "boolean"},"status": {"type": "keyword"}},"date_detection":false'
  def "guicutthrough" '{"name":{"type": "keyword"},"weburi":{"type": "keyword"}}'
}

# ------------------------------------------------------------
# Functions

# Get ip of container with database
getsdnrurl() {
  if [ ! -z "$DBURL" ]; then
    return
  fi
  cmd=$(which docker)
  if [ ! -z "$cmd" ]; then
    SDNRIP=$($cmd inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "$SDNRNAME")
    if [ "$?" = "1" ] ; then
      echo "WARN: Container $SDNRNAME not running. Start the sdnrdb container or enter a database url."
      echo "continuing with localhost"
      SDNRIP="localhost"
    fi
  else
    # if no docker and no db url given
    if [ -z "$DBURL" ]; then
      echo "WARN: Please enter a database url."
      echo "continuing with localhost"
      SDNRIP="localhost"
    fi
  fi
  DBURL="http://$SDNRIP:9200"
}

# Add elements to the array ALIAS and MAPPING
# $1 alias
# $2 mapping properties
def() {
   ALIAS=("${ALIAS[@]}" "$1")
   MAPPING=("${MAPPING[@]}" "$2")
}

# $1 Response
print_response() {
    response="$1"
    body=$(echo $response | sed -E 's/HTTPSTATUS\:[0-9]{3}$//')
    code=$(echo $response | tr -d '\n' | sed -E 's/.*HTTPSTATUS:([0-9]{3})$/\1/')
    if [ "$VERBOSE" = "0" -a "$code" -ne "200" ] ; then
        echo "Error response $code $body"
    fi
    if [ "$VERBOSE" -ge 1 ] ; then
        echo "response $code"
    fi
    if [ "$VERBOSE" -ge 2 ] ; then
       echo "content: $body"
    fi
}

#Write ini file for elasticsearch
# $1 index
# $1 data
file_append() {
    echo "PUT:"$1"/:"$2 >> $INITFILENAME
}

# Send get request to database
# USes DBURL
# $1 url path
# $2 data
http_get_request() {
    url="$DBURL/$1"
    if [ "$VERBOSE" -ge 2 ] ; then
       echo "PUT to $url data $data"
    fi
    response=$(curl --silent --write-out "HTTPSTATUS:%{http_code}" -X GET -H "Content-Type: application/json" "$url")
    print_response "$response"
}

# Send put request to database
# USes DBURL
# $1 url path
# $2 data
http_put_request() {
    url="$DBURL/$1"
    if [ "$VERBOSE" -ge 2 ] ; then
       echo "PUT to $url data $data"
    fi
    response=$(curl --silent --write-out "HTTPSTATUS:%{http_code}" -X PUT -H "Content-Type: application/json" -d "$2" "$url")
    print_response "$response"
}

# Send delete request to database
# $1 url
http_delete_request() {
    url="$DBURL/$1"
    if [ "$VERBOSE" -ge 2 ] ; then
       echo "DELETE to $url"
    fi
    echo "DELETE to $url"
    response=$(curl --silent --write-out "HTTPSTATUS:%{http_code}" -X DELETE -H "Content-Type: application/json" $url)
    print_response "$response"
}

# Delete index and alias
# $1 alias
delete_index_alias() {

    echo "deleting alias $alias"
     # Delete alias
    alias="$PREFIX$1"
    index="$PREFIX$1$VERSION"

    url="$index/_alias/$alias"
    http_delete_request "$url"

     # Delete index
    echo "deleting index $index"
    url="$index"
    http_delete_request "$url"

     # Delete alias that was falsely autocreated as index
    echo "deleting index $index"
    url="$alias"
    http_delete_request "$url"
}

# Write mappings
# Uses version, SHARDS and REPLICAS parameters
# $1 alias and datatype "mydatatype"
# $2 mapping properties
# $3 filename or empty for WEB
create_index_alias() {
   # Create index
    alias="$PREFIX$1"
    index="$PREFIX$1$VERSION"
    mappings='"mappings":{"'$1'":{"properties":'$2'}}'
    settings='"settings":{"index":{"number_of_shards":'$SHARDS',"number_of_replicas":'$REPLICAS'},"analysis":{"analyzer":{"content":{"type":"custom","tokenizer":"whitespace"}}}}'

    if [ -z "$mappings" ]; then
        data="{$settings}"
    else
        data="{$settings,$mappings}"
    fi

    url=$index
    echo "creating index $index"
    if [ -z "$3" ] ; then
        http_put_request "$url" "$data"
    else
        file_append "$url" "$data"
    fi

    #Create alias
    url="$index/_alias/$alias"
    echo "creating alias $alias for $index"
    if [ -z "$3" ] ; then
        http_put_request "$url"
    else
        file_append "$url" "{}"
    fi
}

# Wait for status
# $1 time to wait
es_wait_yellow() {
  ESSTATUS="yellow"
  attempt_counter=0
  max_attempts=5
  echo "Wait up to $max_attempts attempts for $DBURL availability"
  until $(curl --output /dev/null --silent --head --fail $DBURL); do
    if [ ${attempt_counter} -eq ${max_attempts} ];then
      echo "Error: Max attempts reached."
      exit 3
    fi
    attempt_counter=$(($attempt_counter+1))
    printf '.'
    sleep 5
  done
  sleep 2
  echo "Wait up to $1 for status $ESSTATUS"
  RES=$(curl GET "$DBURL/_cluster/health?wait_for_status=$ESSTATUS&timeout=$1&pretty" 2>/dev/null)
  if [ "$?" = "0" ] ; then
    if [[ "$RES" =~ .*status.*:.*yellow.* || "$RES" =~ .*status.*:.*green.* ]] ; then
      echo "Status $ESSTATUS reached: $RES"
    else
      echo "Error: DB Reachable, but status $ESSTATUS not reached"
      exit 2
    fi
  else
    echo "Error: $DBURL not reachable"
    exit 2
  fi
}

# Commands

cmd_create() {
    if [ -n "$WAITYELLOW" ] ; then
        es_wait_yellow "$WAITYELLOW"
    fi
    for i in "${!ALIAS[@]}"; do
          create_index_alias "${ALIAS[$i]}" "${MAPPING[$i]}"
    done
}

cmd_delete() {
    if [ -n "$WAITYELLOW" ] ; then
        es_wait_yellow "$WAITYELLOW"
    fi
    for i in "${!ALIAS[@]}"; do
          delete_index_alias "${ALIAS[$i]}"
    done
    for i in "${!ALIAS[@]}"; do
        delete_index_alias "${ALIAS[$i]}"
    done
}
cmd_purge() {
#    http_get_request '_cat/aliases'
#    body=$(echo $response | sed -E 's/HTTPSTATUS\:[0-9]{3}$//')
#    echo "$response" | awk '/^([^ ]*)[ ]*([^ ]*).*$/{ print $2"/_alias/"$1 }'
#    http_get_request '_cat/indices'
#    echo "indices"
#    echo "$response"
#    echo "$response" | awk '/^[^ ]*[ ]*[^ ]*[ ]*([^ ]*).*$/{ print $3 }'
    echo "not yet implemented"
}
cmd_initfile() {
    echo "Create script initfile: $INITFILENAME"
    if [ -f "$INITFILENAME" ] ; then
       rm $INITFILENAME
    else
       mkdir -p $(dirname $INITFILENAME )
    fi
    for i in "${!ALIAS[@]}"; do
          create_index_alias "${ALIAS[$i]}" "${MAPPING[$i]}" file
    done
}

# Prepare database startup
cmd_startup() {
   ESWAIT=30s
   echo "Startup ElasticSearch DBURL=$DBURL CMD=$STARTUP_CMD CLUSTER=$CLUSTER_ENABLED INDEX=$NODE_INDEX"
   if $CLUSTER_ENABLED ; then
     if [ "$NODE_INDEX" = "0" ] ; then
       echo "Cluster node 0 detected .. create"
       es_wait_yellow $ESWAIT
       cmd_create
     else
       echo "Cluster node > 0 detected .. do nothing"
     fi
   else
     echo "No cluster"
     es_wait_yellow $ESWAIT
     cmd_create
   fi
}

# Parse arguments
parse_args() {
    while [[ $# -gt 0 ]]
    do
      par=($(echo $1 | tr '=' '\n'))
      echo ""
      if [ ${#par[@]} == "2" ] ; then
        # Equal sign found
        key=${par[0]}
        value=${par[1]}
      else
        # No equal sign
        key="$1"
        value="$2"
      fi
      shift
      #Further shift if parameter is used
      case $key in
        -db|--dburl|--database)
          DBURL="$value"
          shift
          ;;
        -r|--replicas)
          REPLICAS="$value"
          shift
          ;;
        -s|--shards)
          SHARDS="$value"
          shift
          ;;
        -p|--prefix)
          PREFIX="$value"
          shift
          ;;
        -f|--file)
          INITFILENAME="$value"
          shift
          ;;
        -x|--verbose)
          VERBOSE="${value:-0}"
          shift
          ;;
        -v|--version)
          VERSION="${value:--v1}"
          shift
          ;;
        -vx|--versionx)
          VERSION=""
          ;;
        -w|--wait)
          WAITYELLOW="${value:-30s}"
          shift
          ;;
        --cmd)
          STARTUP_CMD="$value"
          shift
          ;;
        --odlcluster)
          CLUSTER_ENABLED="$value"
          shift
          ;;
         --index)
           NODE_INDEX="$value"
           shift
           ;;
        *)
          ;;
      esac;
    done
}

# -----------------------------------------
# Main starts here

TASK=$1
shift
parse_args "$@"

set_definition


echo "------------------------------"
echo "Elasticsearch for SDN-R helper"
echo "------------------------------"
echo "Uses database container $SDNRNAME"
echo "Database url $DBURL"
echo "  shards=$SHARDS replicas=$REPLICAS prefix=$PREFIX verbose=$VERBOSE version='$VERSION'"


case "$TASK" in
    "create")
        getsdnrurl
        if [ -z "$DBURL" ] ; then
          echo "Error: unable to detect database url."
          exit 1
        fi
        cmd_create
        ;;
    "delete")
        getsdnrurl
        if [ -z "$DBURL" ] ; then
          echo "Error: unable to detect database url."
          exit 1
        fi
        cmd_delete
        ;;
    "purge")
        getsdnrurl
        if [ -z "$DBURL" ] ; then
          echo "Error: unable to detect database url."
          exit 1
        fi
        cmd_purge
        ;;
    "initfile")
        cmd_initfile
        ;;
    "startup")
        cmd_startup
        ;;
     *)
        echo "usage:"
        echo "  es-init.sh COMMAND [OPTIONS]"
        echo "    Commands:"
        echo "       create           create SDN-R used indices and aliases"
        echo "       delete           delete SDN-R used indices and aliases"
        echo "       initfile         Create initfile for java unit tests"
        echo "       purge            Clear complete database (indices and aliases)"
        echo "       startup          Initial database write if node number 01"
        echo "    Options:"
        echo -e "      -db\--database   DATABASEURL"
        echo -e "      -r\--replicas    REPLICAS"
        echo -e "      -s\--shards      SHARDS"
        echo -e "      -p\--prefix      DATABASE-PREFIX"
        echo -e "      -f\--file        init filename"
        echo -e "      -x\--verbose     Verbose level less 0 .. 2 full"
        echo -e "      -v\--version     Version prefix"
        echo -e "      -vx\--versionx   Version prefix empty"
        echo -e "      -i\--ignore      Ignore error responses"
        echo -e "      --odlcluster     true/false if odl configured as cluster"
        echo -e "      --index          Cluster node 0.."
        echo -e "      --cmd            startup sub command"
        echo " examples:"
        echo "   single node db:"
        echo "      es-init.sh create -db http://sdnrdb:9200 -r 0"
       ;;
esac
