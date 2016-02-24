#!/bin/bash

BASE=clients
echo "Create a directory in $BASE for each client/agent"

NUMBER_OF_CLIENTS=5
for i in `seq 1 $NUMBER_OF_CLIENTS`
do
   sh create-one-client.sh cli$i
done

