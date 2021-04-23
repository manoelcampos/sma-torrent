#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 number-of-clients-to-create" >&2
  exit -1
fi

BASE=target/clients
echo "Creating a directory in $BASE for each client/agent"

NUMBER_OF_CLIENTS=$1
for i in `seq 1 $NUMBER_OF_CLIENTS`
do
   sh create-one-client.sh cli$i
done

