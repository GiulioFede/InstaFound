# InstaFound
Instafound is a social network based on crowdfunding that uses NoSQL technologies such as MongoDB and Neo4J (cluster)

All the steps of the creation, from the idea to the justifications, up to its implementation are explained in the documentation "DocumentazioneProgetto.pdf". 
Some images in the documentation may be unclear. For this reason there are some images in the "UML" folder.

A dump folder contains the MongoDB and Neo4j dumps useful to quickly and consistently rebuild the two databases.

NB: This implementation includes code for connecting both locally and remotely with MongoDB and Neo4j. However the documentation refers only to the "remote" case.
NB: to start the program you must first do the following: go to the Connection.java class and decide the type of connection to make: local or remote. Modify the initConnection function as follows:
    - If local, use the STRING_LOCAL_CONNECTION_MONGODB string (replacing the ports with your own) and the LOCAL string and then choose the ports of the three Neo4j cores 
      in the getTypeOfDriver function. 
    - If remote instead you have to choose the string STRING_REMOTE_CONNECTION_MONGODB (adapt the IPs) and the REMOTE string (adapt only the IP of the single core)

