#!/bin/bash

clear

JAR=repo/com/tilab/jade/4.4.0/jade-4.4.0.jar

echo "Starting the JADE server."
java -cp $JAR jade.Boot -gui &

if [ $? -ne 0 ]; then
	  echo "It was not possible to run the jade server. Check if the $JAR file exists."
  exit $?
fi

echo -e "\n\nWait until the GUI interface is shown and start a client using the exec-client.sh script.\n\n"
