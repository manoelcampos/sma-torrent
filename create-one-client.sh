#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 agent-alias" >&2
  exit 0
fi

JAR="repo/com/tilab/jade/4.4.0/jade-4.4.0.jar"
BASE=target
CLI=$1
DIR=$BASE/clients/$CLI
mkdir -p $DIR
cp $JAR $BASE/jade.jar
cp src/main/java/*.torrent $DIR
cp src/main/java/*.xml $DIR

echo "Agent/Client '$CLI' created at $DIR. To run the Agent/Client execute: ./2-exec-client.sh $CLI"
