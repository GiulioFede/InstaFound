package com.instafound.javafx.backend;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.net.ServerAddress;

import java.util.Arrays;
import java.util.HashSet;

import static java.util.concurrent.TimeUnit.SECONDS;


// the singleton pattern (in particular the lazy singlethon pattern) will be used to ensure a single instance of the Connection class.
//more: we let the class implement Autoclosable to avoid, every time, closing the resource
public class Connection implements AutoCloseable {

    //the unique instance of a class used among all the others
    private static final Connection INSTANCE = new Connection();

    //the method to return the unique instance of connection
    public static Connection getInstance(){
        return Connection.INSTANCE;
    }


    private MongoClient mongoClient;
    private Driver driver;
    private MongoDatabase mongoDatabase;
    //locale
    private static final String STRING_LOCAL_CONNECTION_MONGODB = "mongodb://localhost:27018,localhost:27019,localhost:27020/?wtimeout=5000";
    //remoto
    private static final String STRING_REMOTE_CONNECTION_MONGODB = "mongodb://172.16.3.116:27020,172.16.3.117:27020,172.16.3.118:27020";
    private static final String STRING_INSTAFOUND_DATABASE_MONGODB = "instafound";
    private static final String LOCAL = "LOCAL";
    private static final String REMOTE = "REMOTE";

   //private access avoid the directly creation of the class
    private Connection(){

            initConnection();
    }

    private void initConnection() {

        //the server connections are created on daemon threads. So delay check connections directly when read or write.
        //NEO4J_________________________________________
        driver = getTypeOfDriver(REMOTE);

        //default config (some queries could override it)
        //MONGODB________________________________________________
            ConnectionString uri = new ConnectionString(STRING_REMOTE_CONNECTION_MONGODB);

            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(uri)
                    .readPreference(ReadPreference.secondaryPreferred())
                    .retryWrites(true)
                    .writeConcern(WriteConcern.MAJORITY).build();


            mongoClient = MongoClients.create(mongoClientSettings);

            mongoDatabase = mongoClient.getDatabase(STRING_INSTAFOUND_DATABASE_MONGODB);

    }

    private Driver getTypeOfDriver(String type) {

        if(type.compareTo(LOCAL)==0)

            return createClusterDriver("neo4j://localhost:7687","neo4j","1234", ServerAddress.of("localhost",7688), ServerAddress.of("localhost",7689));

        else
            return GraphDatabase.driver("bolt://172.16.3.116:7687",
                    Config.builder()
                            .withMaxTransactionRetryTime( 5, SECONDS ) //retry a transaction for a max 5 seconds
                            .build());

    }

    public Driver createClusterDriver(String uri, String user, String password, ServerAddress... addresses){
        Config config = Config.builder()
                .withResolver( address -> new HashSet<>( Arrays.asList( addresses ) ) )
                .withMaxTransactionRetryTime(5,SECONDS)
                .build();
        return GraphDatabase.driver( uri, AuthTokens.basic( user, password ), config );
    }

    public Driver getDriver(){
        return driver;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoCollection<Document> getMongoDBCollection(String nameOfCollection){
        return mongoDatabase.getCollection(nameOfCollection);
    }


    @Override
    public void close() throws Exception {
            mongoClient.close();
            driver.close();
    }
}
