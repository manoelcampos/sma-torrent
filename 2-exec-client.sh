#!/bin/bash

clear
echo "Exec a agent from a specific folder"
echo "Input the agent folder name as a parameter to this script"

BASE=target/clients
CLI=$1
DIR=$BASE/$CLI

CUR=`pwd`
cd $DIR
java -cp SMATorrent.jar:../jade.jar jade.Boot -container $CLI:com.manoelcampos.smatorrent.TorrentClientAgent &
echo "Started Agent/Client '$CLI'"
cd $CUR

#java -cp /opt/jade/lib/jade.jar:bin jade.Boot -container $CLI:com.manoelcampos.smatorrent.TorrentClientAgent &
