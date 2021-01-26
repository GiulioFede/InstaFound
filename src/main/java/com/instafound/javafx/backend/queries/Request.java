package com.instafound.javafx.backend.queries;

import java.util.*;

import com.instafound.javafx.model.*;
import com.mongodb.*;
import com.mongodb.client.model.Filters;

import com.google.gson.Gson;
import com.instafound.javafx.backend.Connection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.Neo4jException;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Indexes.*;
import static org.neo4j.driver.Values.parameters;


public class Request {

    private static Connection connection = Connection.getInstance();

    // QUERY NEO4J-----------------------------------------------------------------------------------------

    private static Gson gsonBuilder = new Gson();


    //given the username of a user check if password is the same, if it's return the complete informations about the user
    public static UserInfo login(String username, String password) throws Neo4jException {

        try (Session session = connection.getDriver().session()) {

          return session.readTransaction(new TransactionWork<UserInfo>() {
                @Override
                public UserInfo execute(Transaction transaction) {

                    Result result = transaction.run("MATCH (u:User) WHERE u.username=$username AND u.password=$password " +
                                    "RETURN u.username as username, u.urlProfileImage as url, u.region as region, u.province as province, u.cap as cap",
                            parameters("username", username, "password", password));

                    if (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUsername(username);
                        userInfo.setUrlProfileImage((String) row.get("url"));
                        userInfo.setRegion((String) row.get("region"));
                        userInfo.setProvince((String) row.get("province"));
                        userInfo.setCap((long) row.get("cap"));

                        return userInfo;
                    }
                    return null;
                }
            }
            );
        }

    }


    /*
      Typical Neo4J Query
      given a username of current user, get all the information about the teams the user belongs to or has created. The informations are:
      1) id and name of team
      2) username of members (organizer is always the first)
      3) all the id of campaigns each of one has supported
     */
    public static ArrayList<Team> getUserTeamsDetails(String username) throws Neo4jException {

        try (Session session = connection.getDriver().session()) {
            return session.readTransaction(new TransactionWork<ArrayList<Team>>() {
                @Override
                public ArrayList<Team> execute(Transaction transaction) {

                    ArrayList<Team> teams = new ArrayList<>();

                    Result result = transaction.run("MATCH (u:User)-[r:CREATES|BELONGS]->(t:Team) WHERE u.username=$username\n" +
                                    "WITH t as team\n" +
                                    "MATCH (u:User)-[r:CREATES|BELONGS]->(t:Team) WHERE ID(t)=ID(team)\n" +
                                    "WITH ID(t) as idTeam,t.name as nameOfTeam, u.username as username, CASE type(r) WHEN \"CREATES\" THEN \"organizer\" ELSE \"member\" END AS rule\n" +
                                    "OPTIONAL MATCH (t:Team)-[s:SUPPORTS]-(c:Campaign) WHERE ID(t)=idTeam\n" +
                                    "WITH idTeam, nameOfTeam, username, rule, collect(DISTINCT ID(c)) as idCampaignsSupported\n" +
                                    "ORDER BY idTeam, rule DESC\n" +
                                    "RETURN idTeam, nameOfTeam as name, collect(username) as members, idCampaignsSupported",
                            parameters("username", username));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        Collection<String> collectionMembers = (Collection<String>) row.get("members");
                        Collection<String> collectionCampaigns = (Collection<String>) row.get("idCampaignsSupported");
                        ArrayList<String> members = new ArrayList<>(collectionMembers);
                        ArrayList<String> idCampaigns = new ArrayList<>(collectionCampaigns);

                        Team team = new Team((Long) row.get("idTeam"),
                                (String) row.get("name"),
                                members.remove(0),
                                members,
                                idCampaigns);

                        teams.add(team);
                    }
                    return teams;
                }
            });
        }
    }

    //Typical Neo4J Query
    public static ArrayList<AnalyticalData> getFollowersTrend(String idCampaign) {

        try (Session session = connection.getDriver().session()) {
            return session.readTransaction(new TransactionWork<ArrayList<AnalyticalData>>() {
                @Override
                public ArrayList<AnalyticalData> execute(Transaction transaction) {

                    ArrayList<AnalyticalData> data = new ArrayList();

                    Result result = transaction.run("MATCH (c:Campaign)-[f:FOLLOWS]-(u:User) WHERE c.idMongoDB=$idCampaign \n" +
                                                       "RETURN f.timestamp as timestamp, count(*) as count " +
                                                       "ORDER BY timestamp ASC",
                            parameters("idCampaign", idCampaign));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        AnalyticalData analyticalData = new AnalyticalData();
                        analyticalData.setKey(((String) row.get("timestamp")));
                        analyticalData.setValue((Long) row.get("count"));
                        data.add(analyticalData);
                    }

                    return data;
                }
            });
        }
    }

    //given username of current user, get his friends
    public static ArrayList<UserInfo> getUserFriends(String username) throws Neo4jException {

        try (Session session = connection.getDriver().session()) {
            return session.readTransaction(new TransactionWork<ArrayList<UserInfo>>() {
                @Override
                public ArrayList<UserInfo> execute(Transaction transaction) {

                    ArrayList<UserInfo> listOfFriends = new ArrayList();

                    Result result = transaction.run("MATCH (n:User)-[:FRIEND_OF]-(u) WHERE n.username = $name" +
                                    " RETURN u.username as Username, u.urlProfileImage as UrlProfileImage, u.province as Province, u.region as Region, u.cap as Cap",
                            parameters("name", username));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        UserInfo friendInfo = new UserInfo();
                        friendInfo.setUsername((String) row.get("Username"));
                        friendInfo.setUrlProfileImage((String) row.get("UrlProfileImage"));
                        friendInfo.setRegion((String) row.get("Region"));
                        friendInfo.setProvince((String) row.get("Province"));
                        friendInfo.setCap((long) row.get("Cap"));
                        listOfFriends.add(friendInfo);
                    }

                    return listOfFriends;
                }
            });
        }
    }

    //given username of current user, get his basic information (no password)
    public static UserInfo getUserInfo(String username) throws Neo4jException {

        try (Session session = connection.getDriver().session()) {
            return session.readTransaction(new TransactionWork<UserInfo>() {
                @Override
                public UserInfo execute(Transaction transaction) {
                    Result result = transaction.run("MATCH (n:User) WHERE n.username=$name" +
                                    " RETURN n.cap as Cap, n.province as Province , n.region as Region, n.urlProfileImage as UrlProfileImage",
                            parameters("name", username));

                    Record r = result.next();
                    final UserInfo userInfo = new UserInfo();
                    userInfo.setUsername(username);
                    userInfo.setUrlProfileImage(r.get("UrlProfileImage").asString());
                    userInfo.setRegion(r.get("Region").asString());
                    userInfo.setProvince(r.get("Province").asString());
                    userInfo.setCap(r.get("Cap").asInt());

                    return userInfo;
                }
            });
        }
    }

    //given username of current user, retrieve all the preview informations about the campaigns
    public static ArrayList<CampaignNeo4J> getPreviewInfoOfUserCreatedCampaigns(String username) throws Neo4jException{

        try (Session session = connection.getDriver().session()){
            return session.readTransaction(new TransactionWork<ArrayList<CampaignNeo4J>>() {
                @Override
                public ArrayList<CampaignNeo4J> execute(Transaction transaction) {

                    ArrayList<CampaignNeo4J> previewCampaigns = new ArrayList<>();

                    Result result = transaction.run(" MATCH (u:User)-[o:ORGANIZES]->(c:Campaign) WHERE u.username=$username RETURN c.idMongoDB as id, c.title as title ,c.urlImage as url",
                            parameters("username", username));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        String id  = (String)row.get("id");
                        String title  = (String)row.get("title");
                        String url  = (String)row.get("url");

                        CampaignNeo4J previewCampaign = new CampaignNeo4J(new ObjectId(id), title, url);

                        previewCampaigns.add(previewCampaign);
                    }


                    return previewCampaigns;
                }
            });
        }
    }

    //given username of current user, retrieve all the preview informations about the campaigns
    public static ArrayList<CampaignNeo4J> getPreviewInfoOfUserFollowedCampaigns(String username) throws Neo4jException {

        try (Session session = connection.getDriver().session()){
            return session.readTransaction(new TransactionWork<ArrayList<CampaignNeo4J>>() {
                @Override
                public ArrayList<CampaignNeo4J> execute(Transaction transaction) {

                    ArrayList<CampaignNeo4J> previewCampaigns = new ArrayList<>();

                    Result result = transaction.run(" MATCH (u:User)-[o:FOLLOWS]->(c:Campaign) WHERE u.username=$username RETURN c.idMongoDB as id, c.title as title ,c.urlImage as url",
                            parameters("username", username));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        String id  = (String)row.get("id");
                        String title  = (String)row.get("title");
                        String url  = (String)row.get("url");

                        CampaignNeo4J previewCampaign = new CampaignNeo4J(new ObjectId(id), title, url);

                        previewCampaigns.add(previewCampaign);
                    }


                    return previewCampaigns;
                }
            });
        }
    }

    //given username of current user, return the id of the followed campaigns
    public static ArrayList<String> getIdOfFollowedCampaigns(String username) throws Neo4jException{

        try (Session session = connection.getDriver().session()){
            return session.readTransaction(new TransactionWork<ArrayList<String>>() {
                @Override
                public ArrayList<String> execute(Transaction transaction) {

                    ArrayList<String> idFollowedCampaigns = new ArrayList<>();

                    Result result = transaction.run(" MATCH (n:User)-[o:FOLLOWS]-(c:Campaign) WHERE n.username=$username RETURN c.idMongoDB",
                            parameters("username", username));

                    while (result.hasNext())
                        idFollowedCampaigns.add(result.next().get(0).asString());

                    return idFollowedCampaigns;
                }
            });
        }
    }

    //Typical Neo4J Query
    //given username of current user, return 3 suggested friend first on the basis of proximity and then on the basis of the categories of interest in common
    public static ArrayList<SuggestedFriendModel> getSuggestedFriend(String username) throws Neo4jException{

        try (Session session = connection.getDriver().session()){
            return session.readTransaction(new TransactionWork<ArrayList<SuggestedFriendModel>>() {
                @Override
                public ArrayList<SuggestedFriendModel> execute(Transaction transaction) {

                    String operator1 = "WHERE NOT (n)-[:FRIEND_OF]-(u) AND u.username=$username AND n.cap<=u.cap AND n.username<>$username\n";
                    String operator2 = "WHERE NOT (n)-[:FRIEND_OF]-(u) AND u.username=$username AND n.cap>=u.cap AND n.username<>$username\n";
                    boolean decision = new Random().nextBoolean();

                    ArrayList<SuggestedFriendModel> suggestedFriends = new ArrayList<>();

                    String query = "MATCH (u:User {username:$username})-[r]-(c:Campaign)\n" +
                            "WITH collect(DISTINCT c.category) as favoriteCategoryOfCurrentUser\n" +
                            "MATCH (n:User),(u:User)\n" +
                            ((decision==true)?(operator1):(operator2))+
                            "WITH n.username as candidateFriend, n.cap as candidateCap, favoriteCategoryOfCurrentUser, rand() as randomValue\n" +
                            ((decision==true)?("ORDER BY candidateCap DESC\n"):("ORDER BY candidateCap ASC\n")) +
                            "LIMIT 15\n" +
                            "MATCH (u:User)-[r]-(c:Campaign) WHERE u.username = candidateFriend\n" +
                            "WITH u, collect(c.category) as favoriteCategoryOfCandidate, favoriteCategoryOfCurrentUser, size([y IN collect(DISTINCT c.category) WHERE y IN favoriteCategoryOfCurrentUser]) as inCommon, randomValue\n" +
                            "RETURN u.username as username, u.urlProfileImage as urlProfileImage, u.region as region, u.province as province, u.cap as cap, inCommon, randomValue\n" +
                            "ORDER BY randomValue DESC, inCommon DESC\n" +
                            "LIMIT 3";


                    Result result = transaction.run(query, parameters("username", username));

                    while (result.hasNext()) {
                        Map<String, Object> row = result.next().asMap();
                        String usernameNewSuggestedFriend  = (String)row.get("username");
                        String urlImageNewSuggestedFriend  = (String)row.get("urlProfileImage");
                        String regionNewSuggestedFriend    = (String)row.get("region");
                        String provinceNewSuggestedFriend  = (String)row.get("province");
                        long capNewSuggestedFriend         = (long)row.get("cap");
                        int inCommonCategories             = (int)(long)row.get("inCommon");

                        suggestedFriends.add(new SuggestedFriendModel(usernameNewSuggestedFriend,
                                urlImageNewSuggestedFriend,
                                regionNewSuggestedFriend,
                                provinceNewSuggestedFriend,
                                capNewSuggestedFriend,
                                inCommonCategories));
                    }

                    return suggestedFriends;
                }
            });
        }
    }


    //QUERY MONGODB----------------------------------------------------------------------------------------

    //get n
    public static ArrayList<CampaignMongoDB> getLastCampaigns(int n, int s, String category) throws MongoException {

        ArrayList<CampaignMongoDB> lastCampaigns = new ArrayList<>();
        //TODO: non avendo lo "startDate" prelevo solo n campagne e basta
        Bson filter = Filters.eq("category",category);

            MongoCursor<Document> cursor;
            if (category != "null")
                cursor = connection.getMongoDBCollection("campaigns").withReadConcern(ReadConcern.MAJORITY).find(filter).sort(new BasicDBObject("startDate",-1)).skip(s).limit(n).iterator();
            else
                cursor = connection.getMongoDBCollection("campaigns").withReadConcern(ReadConcern.MAJORITY).find().sort(new BasicDBObject("startDate",-1)).skip(s).limit(n).iterator();

            try {
                while (cursor.hasNext()) {
                    Document campaignDocument = cursor.next();
                    String str = campaignDocument.toJson();

                    CampaignMongoDB campaignMongoDB = gsonBuilder.fromJson(str, CampaignMongoDB.class);
                    campaignMongoDB.set_id(campaignDocument.getObjectId("_id"));
                    lastCampaigns.add(campaignMongoDB);
                }
            }finally {
                cursor.close();
            }

        return lastCampaigns;
    }

    //get the details of a specific campaign
    public static CampaignMongoDB getDetailsCampaign(ObjectId id) throws MongoException {

        CampaignMongoDB campaign = null;
        try (MongoCursor<Document> cursor = connection.getMongoDBCollection("campaigns").withReadConcern(ReadConcern.MAJORITY).find(Filters.eq("_id",id)).iterator())
        {
            if(cursor.hasNext()) {
                campaign = gsonBuilder.fromJson(cursor.next().toJson(), CampaignMongoDB.class);
                campaign.set_id(id);
            }

        }
        return campaign;
    }

    //ANALYTICS: donations trend
    public static ArrayList<AnalyticalData> getDonationsTrend(ObjectId idCampaign) throws MongoException{

        ArrayList<AnalyticalData> analyticalData = new ArrayList<>();

        Bson match = match(Filters.eq("_id",idCampaign));
        Bson unwind = unwind("$donations");
        Bson group = group("$donations.timestamp", avg("totalDonations","$donations.amount"));
        Bson sort = sort(ascending("_id"));

            MongoCursor cursor = connection.getMongoDBCollection("campaigns").withReadConcern(ReadConcern.LOCAL).aggregate(Arrays.asList(match,unwind,group,sort)).iterator();
            while(cursor.hasNext()){
                Document data = (Document) cursor.next();
                String date = data.getString("_id");
                Double totalDonations = data.getDouble("totalDonations");

                analyticalData.add(new AnalyticalData(totalDonations,date));
            }

        return analyticalData;

    }

    /*
    the following aggregations all refer to collection "monthlyDonations" without implementing any month filter on the date as this collection
    is managed through an expired time TTL index equal to one month. This way we have speed and redundancy management for the next month automatically.
     */

    //ANALYTICS:top monthly categories (exploiting monthlyDonations's collection)
    public static ArrayList<AnalyticalData> getTopMonthlyCategories() throws MongoException{

        ArrayList<AnalyticalData> analyticalData = new ArrayList<>();

        Bson group = group("$category", sum("amountAchived","$amount"));
        Bson sort = sort(descending("amountAchived"));

            MongoCursor cursor = connection.getMongoDBCollection("monthlyDonations").withReadConcern(ReadConcern.LOCAL).aggregate(Arrays.asList(group,sort)).iterator();
            while(cursor.hasNext()){
                Document data = (Document) cursor.next();
                String date = data.getString("_id");
                Double totalDonations = data.getDouble("amountAchived");

                analyticalData.add(new AnalyticalData(totalDonations,date));
            }

        return analyticalData;

    }

    //ANALYTICS: get top donors of the current month (exploiting monthlyDonations's collection)
    public static ArrayList<DonorRankModel> getTopMonthlyDonors() throws MongoException{

        ArrayList<DonorRankModel> rank = new ArrayList<>();

        Bson group = group("$donor", sum("amountDonated","$amount"));
        Bson sort = sort(descending("amountDonated"));

            MongoCursor cursor = connection.getMongoDBCollection("monthlyDonations").withReadConcern(ReadConcern.LOCAL).aggregate(Arrays.asList(group,sort)).iterator(); //
            while(cursor.hasNext()){
                Document data = (Document) cursor.next();
                String donor = data.getString("_id");
                Double totalDonated = data.getDouble("amountDonated");

                rank.add(new DonorRankModel(donor,totalDonated));
            }

        return rank;

    }


}
    


