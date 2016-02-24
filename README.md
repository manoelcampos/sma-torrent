SMA Torrent
-----------

A java BitTorrent client using the [JADE Multi-Agent Systems Framework](http://jade.tilab.com).
For more information, refer to [this paper](paper-sma-torrent.pdf) (only in Portuguese).


How to compile and run the project
-----------------------------------

The application cannot be run directly from the IDE or directly from its jar file.
If you try to run it, you will receive a message informing how to run the application.

The following steps show how to compile the project, start the JADE server and create several clients/agents in the same machine in order to test the BitTorrent client without needing several computers and network configurations.

Execute the following commands in a terminal of a Unix-based operating system, such as Linux or Mac OS X.

1. Open a terminal in the project's root folder
1. Compile the project: `mvn package` to generate the project jar file.
1. Start the JADE server: `./0-start-server.sh` and wait until the JADE GUI is shown.
1. You can use the command `./1-create-clients.sh` to create several clients/agents at the folder `clients`. The number of clients to be create can be changed editing the script. The JADE framework requires that each client/agent has a alias. This scripts gives numbered alias for the created clients, such as cli1, cli2, etc.
1. To run each created client/agent you have to execute the command `./2-exec-client.sh agent-alias` for each client/agent you want to run. For instance, executing `./2-exec-client.sh cli1` will run the agent named `cli1` that has to be created previously at the folder `clients/cli1`.


To start seeding a file, you have to place the file and its respective torrent in the same directory and add it to a client/agent GUI.


KNOWN BUGS
-----------

- When adding or removing a torrent from a client, the torrent table in the GUI may not update automatically. If such a problem happens, you can resize the window in order to allow the table update. When the client is receiving files, the table is only updated when you enter and exit a given column.

- When closing the application, some torrents may not be unregistered from the JADE Directory Facilitator (DF) Service, what causes error when trying to reopen the same client GUI. In this case, you can restart the JADE server or manually unregister the corresponding torrent in the server GUI.

- To remove or pause a torrent while a agent behaviour is accessing it can cause the application to hang unexpectedly.