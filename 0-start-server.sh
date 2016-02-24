#!/bin/bash

clear

BASE=clients

echo "Starting the JADE server."
java -cp $BASE/jade.jar jade.Boot -gui & 

if [ $? -ne 0 ]; then
	  echo "It was not possible to run the jade server. Check your $BASE dir for the jade.jar file." 
  exit $?
fi

echo -e "\n\nWait until the GUI interface is shown and start a client using the exec-client.sh script.\n\n"
