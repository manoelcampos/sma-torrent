#!/bin/bash

clear
echo "Exec a agent from a specific folder"
echo "Input the agent folder name as a parameter to this script"
# $1 indicates the client alias (representing the client directory)
CLI=$1
CUR=`pwd`
BASE="$CUR/target"
DIR="$BASE/clients/$CLI"
cd $DIR
java -cp ../../sma-torrent.jar:../../jade.jar jade.Boot -container $CLI:com.manoelcampos.smatorrent.TorrentClientAgent &
echo "Started Agent/Client '$CLI'"
cd $CUR

#java -cp /opt/jade/lib/jade.jar:bin jade.Boot -container $CLI:com.manoelcampos.smatorrent.TorrentClientAgent &
