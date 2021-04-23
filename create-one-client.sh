#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 agent-alias" >&2
  exit 0
fi

BASE=target/clients
CLI=$1
DIR=$BASE/$CLI
mkdir -p $DIR
cp repo/com/tilab/jade/4.4.0/jade-4.4.0.jar $BASE/jade.jar
cp target/SMATorrent-0.8.jar $DIR/SMATorrent.jar
cp src/main/java/*.torrent $DIR
cp src/main/java/*.xml $DIR


echo "Agent/Client '$CLI' created at '$DIR'. To run the Agent/Client execute: ./2-exec-client.sh $CLI"
