#!/bin/bash
# Java startup helper
# if JAVA_HOME is specified, use related version

echo "Java run helper"

if [ ! -z "$JAVA_HOME" ] ; then
  echo "JAVA_HOME=$JAVA_HOME"
  $JAVA_HOME/bin/java "$@"
else
  echo "Java on path"
  java "$@"
fi

