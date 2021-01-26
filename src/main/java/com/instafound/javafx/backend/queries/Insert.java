package com.instafound.javafx.backend.queries;


import com.instafound.javafx.backend.Connection;
import com.instafound.javafx.model.CampaignMongoDB;
import com.instafound.javafx.model.Team;
import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.result.InsertOneResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.exceptions.Neo4jException;

import static org.neo4j.driver.Values.parameters;

public class Insert {

    private static Connection connection = Connection.getInstance();

    public static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public Insert(){ }


    //NEO4J:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    //register a new user. The return value (a short) is the code of the result of the operation
    public static boolean registerNewUser(String username, String password, String urlProfileImage, String region, String province, int cap) throws Neo4jException {

        try(Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                        Result result = transaction.run("MERGE (u:User{username:$username,password:$pass, urlProfileImage:$url,region:$region,province:$province,cap:$cap}) RETURN u ",
                                parameters("username", username, "pass", password, "url", urlProfileImage, "region", region, "province", province, "cap", cap));

                        if(result.hasNext())
                            return true;

                    return false;
                }
            });
        }
    }

    public static boolean insertNewMemberToTheTeam(String usernameNewMember, long idTeam ) throws Neo4jException{


        try( Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (n:User), (t:Team) WHERE n.username=$usernameNewMember AND ID(t)=$idTeam " +
                                                       "MERGE (n)-[c:BELONGS]->(t) RETURN n,t,c;",
                            parameters("usernameNewMember", usernameNewMember, "idTeam", idTeam));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }

    //given the username of creator and the name of new team, create a new team with relationship :Creates among them
    public static Team createNewTeam(String usernameCreator, String nameOfTeam) throws Neo4jException{

        try( Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Team>() {
                @Override
                public Team execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (n:User) WHERE n.username=$usernameOfCreator" +
                                                      " CREATE (t:Team {name:$nameOfNewTeam} ),  (n)-[r:CREATES]->(t) RETURN ID(t) as idTeam",

                            parameters("usernameOfCreator", usernameCreator, "nameOfNewTeam", nameOfTeam));
                    if(result.hasNext()){
                        Map<String, Object> row = result.next().asMap();

                        Team team = new Team((Long)row.get("idTeam"),
                                nameOfTeam,
                                usernameCreator,
                                new ArrayList<>(),
                                new ArrayList<>());

                        return team;
                    }

                    return null;
                }
            });
        }
    }

    //create a new friend relationship :FRIEND_OF between current user and other user
    public static boolean addNewFriend(String usernameCurrentUser, String usernameNewFriend) throws Neo4jException{

        try (Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (u:User), (f:User) WHERE u.username=$currentUser AND f.username=$newFriend \n" +
                                       "MERGE (u)-[r:FRIEND_OF]->(f) RETURN u,r,f",
                                        parameters("currentUser",usernameCurrentUser,"newFriend",usernameNewFriend));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }

    //create a new friend relationship :FOLLOWS between current user a
    public static boolean followCampaign(String usernameCurrentUser, String idCampaign){

        try (Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
                @Override
                public Boolean execute(Transaction transaction) {

                    LocalDateTime now = LocalDateTime.now();
                    Result result = transaction.run("MATCH (u:User), (c:Campaign) WHERE u.username=$currentUser AND c.idMongoDB=$idCampaign \n" +
                                                       "CREATE (u)-[r:FOLLOWS{timestamp:$timestamp}]->(c) RETURN u,r,c",
                            parameters("currentUser",usernameCurrentUser,"idCampaign",idCampaign,"timestamp",dtf.format(now)));
                    if(result.hasNext())
                        return true;

                    return false;
                }
            });
        }
    }


    //MongoDB & Neo4J:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    public static boolean insertNewCampaign(CampaignMongoDB campaign, String nameOfTeam) throws MongoTimeoutException{

        Document document = new Document("title", campaign.getTitle())
                .append("urlImage",campaign.getUrlImage())
                .append("startDate", campaign.getStartDate())
                .append("amountRequired",campaign.getAmountRequired())
                .append("amountAchived", campaign.getAmountAchived())
                .append("countDonations", campaign.getCountDonations())
                .append("description", campaign.getDescription())
                .append("category", campaign.getCategory())
                .append("organizer",campaign.getOrganizer())
                .append("membersOfTeam",campaign.getMembersOfTeam())
                .append("beneficiary", campaign.getBeneficiary())
                .append("donations",new ArrayList<>());

        //create a session for transaction
        final ClientSession clientSession = Connection.getInstance().getMongoClient().startSession();
        TransactionOptions txnOptions = TransactionOptions.builder()
                .writeConcern(WriteConcern.W3)
                .build();

        TransactionBody transaction = new TransactionBody<Void>() {
            @Override
            public Void execute() {

                //insert into MongoDB

                InsertOneResult result = connection.getMongoDBCollection("campaigns").withWriteConcern(WriteConcern.W3).insertOne(clientSession,document);

                String objectId = ((ObjectId)result.getInsertedId().asObjectId().getValue()).toHexString();

                //insert into neo4J


                boolean outocome = insertNewCampaignIntoNeo4J(campaign.getOrganizer(),objectId,campaign.getTitle(),campaign.getUrlImage(),nameOfTeam);

                if(outocome!=true)
                    throw new RuntimeException();


                return null;
            }
        };

        try {
            clientSession.withTransaction(transaction,txnOptions);
        }
        catch (Neo4jException n){
            System.out.println("Error: "+n.getMessage());
            return false;
        }
        catch (MongoWriteException mew){
            System.out.println(mew.toString());
            return false;
        }
        catch (MongoWriteConcernException mwce){
            System.out.println("Now it's no possible to ensure the creation of campaign. Try later please. ");
            return false;
        }


       return true;
    }

    private static boolean insertNewCampaignIntoNeo4J(String organizer, String idMongoDB, String titleCampaign, String urlImageCampaign, String team) {

        try (Session session = connection.getDriver().session())
        {
            return session.writeTransaction(new TransactionWork<Boolean>() {
            @Override
            public Boolean execute(Transaction transaction) {

                System.out.println("Inserisco campagna in neo4...");

                if(team!=null) {
                    Result result1 = transaction.run("MATCH (u:User), (t:Team) WHERE u.username=$organizer AND t.name=$team " +
                                    "CREATE (c:Campaign {idMongoDB:$idMongoDB, title:$title, urlImage: $urlImage} ), (u)-[:ORGANIZES]->(c)<-[:SUPPORTS]-(t) RETURN c",
                            parameters("organizer",organizer,"idMongoDB", idMongoDB, "title", titleCampaign, "urlImage", urlImageCampaign, "team", team));
                    if (result1.hasNext())
                        return true;
                }else {
                    Result result1 = transaction.run("MATCH (u:User) WHERE u.username=$organizer " +
                                    "CREATE (c:Campaign {idMongoDB:$idMongoDB, title:$title, urlImage: $urlImage} ), (u)-[:ORGANIZES]->(c) RETURN c",
                            parameters("organizer",organizer,"idMongoDB", idMongoDB, "title", titleCampaign, "urlImage", urlImageCampaign));
                    if (result1.hasNext())
                        return true;
                }

                return false;
            }
        });
        }
    }



}
