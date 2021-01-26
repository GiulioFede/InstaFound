package com.instafound.javafx.backend.queries;

import com.instafound.javafx.backend.Connection;
import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

public class Update {

    public static Connection connection = Connection.getInstance();

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //Neo4J-----------------------------------------------------------------------------------

    //given username of user and friend, the method delete friend's relationship
    public static boolean removeFriend(String user, String friend) throws Neo4jException{

        try( Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (n:User {username:$user})-[r:FRIEND_OF]-(f:User{username:$friend}) DELETE r RETURN r",
                            parameters("user", user, "friend", friend));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }

    //given username of user and id of team, remove belongs relationship between them
    public static boolean removeMemberFromTeam(String usernameOldMember, long idTeam ) throws Neo4jException{

        try( Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (n:User), (t:Team),(n)-[c:BELONGS]->(t) WHERE n.username=$usernameOldMember AND ID(t)=$idTeam " +
                                    "DELETE c RETURN c;",
                            parameters("usernameOldMember", usernameOldMember, "idTeam", idTeam));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }

    //given id of team, remove it and all the realationships with it
    //given username of user and id of team, remove belongs relationship between them
    public static boolean removeTeam(long idTeam ) throws Neo4jException{

        try( Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (t:Team) WHERE ID(t)=$idTeam " +
                                                       "DETACH DELETE t RETURN t;",
                            parameters("idTeam", idTeam));
                    if(result.hasNext()) {
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    //given username of user and id of campaign, unfollow the campaign
    public static boolean unfollowCampaign(String username, String idCampaign) throws Neo4jException{

        try(Session session = connection.getDriver().session()){
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (u:User)-[f:FOLLOWS]->(c:Campaign) WHERE u.username=$username AND c.idMongoDB=$idCampaign " +
                                                       "DELETE f RETURN f",
                            parameters("username", username,"idCampaign",idCampaign));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }

    //given username and new url, update the url profile image of a user with that username
    public static boolean updateUrlProfileImageUser(String username, String newUrl) throws Neo4jException {
        try(Session session = connection.getDriver().session()){
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (u:User) WHERE u.username=$username SET u.urlProfileImage=$url RETURN u",
                            parameters("username", username,"url",newUrl));

                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }


    //MongoDB-----------------------------------------------------------------------------------

    //given the id of campaign, insert a new donations and increment the field value amountAchived accordingly
    public static boolean donateCampaign(String idCampaign, String category, String username, Double amountDonated) throws MongoTimeoutException{

        LocalDateTime now = LocalDateTime.now();

        //create a new document for a collection campaign
        Document donation = new Document("donor",username)
                .append("amount",amountDonated)
                .append("timestamp", dtf.format(now));

        //create a new document for a collection monthlyDonations
        Document donationLog = new Document("timestamp",dtf.format(now))
                .append("donor",username)
                .append("amount",amountDonated)
                .append("category", category);

        //start a transaction (the only way to have an atomic operation among different document of different collections)
        //we want to avoid that user donate a campaign and then return to the same campaign and doesn't see its donation. The donor should donate again!
        final ClientSession clientSession = Connection.getInstance().getMongoClient().startSession();
        TransactionOptions txnOptions = TransactionOptions.builder()
                .writeConcern(WriteConcern.W3) //<------wait all the replica
                .build();

        TransactionBody transactionBody = new TransactionBody() {
            @Override
            public Void execute() {

                //update campaign
                connection.getMongoDBCollection("campaigns").withWriteConcern(WriteConcern.W3).updateOne(Filters.eq("_id", new ObjectId(idCampaign)),
                        combine(push("donations", donation),
                                inc("amountAchived", amountDonated),
                                inc("countDonations", 1)));

                //insert donation in the log
                connection.getMongoDBCollection("monthlyDonations").insertOne(donationLog);

                return null;
            }
        };

        try {
            clientSession.withTransaction(transactionBody, txnOptions);
        }catch (MongoWriteConcernException mwce) {
            return false;
        }

        return true;
    }

}
