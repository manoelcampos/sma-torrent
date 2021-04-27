#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 agent-alias" >&2
  exit 0
fi

BASE=target
CLI=$1
DIR=$BASE/clients/$CLI
mkdir -p $DIR
cp src/main/java/*.torrent $DIR
cp src/main/java/*.xml $DIR

echo "Agent/Client '$CLI' created at $DIR. To run the Agent/Client execute: ./2-exec-client.sh $CLI"
