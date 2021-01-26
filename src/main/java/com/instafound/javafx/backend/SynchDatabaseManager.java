package com.instafound.javafx.backend;

import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.Campaign;
import com.instafound.javafx.model.GeoData;
import com.instafound.javafx.model.User;
import com.instafound.javafx.utilities.GeoGenerator;
import com.instafound.javafx.utilities.RandomGenerator;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertOneResult;
import javafx.application.Platform;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class SynchDatabaseManager {

    private String sourceFilePath;
    private Workbook workbook;
    private GeoGenerator geoGenerator;
    private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat formatTimestamp = new SimpleDateFormat("yyyy-MM-dd");

    private HomeController homeController;

    public SynchDatabaseManager(HomeController homeController){

        this.homeController = homeController;
        sourceFilePath = "null";
        geoGenerator = new GeoGenerator();

    }

    //define the path to the source file to parse
    public void setSourceFilePath(String path){

        sourceFilePath = path;
    }


    public void insertDataFromExcelToDatabases(){


        if(sourceFilePath.compareTo("null")==0) {
            System.err.println("You must set the path of a file before extracting rows.");
            return;
        }

        //access the file system
        try {
           FileInputStream file = new FileInputStream(new File(sourceFilePath));
            workbook = new XSSFWorkbook(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //work with first sheet
        Sheet sheet = workbook.getSheetAt(0);


        try {
            int i = 0;
            for (Row row : sheet) {
                parseRow(row);
                final int j = i + 1;
                Platform.runLater(() -> {
                    //update progress
                    homeController.setUpdateProgressBar((double) j / sheet.getPhysicalNumberOfRows());
                });

                i++;
            }
        }catch (DiscoveryException | ServiceUnavailableException e){
            Platform.runLater(() -> {
                //update progress
                homeController.showPopUpOfExplorationMode();

            });
        }catch (MongoTimeoutException e){
            Platform.runLater(() -> {
                //update progress
                homeController.unFrostHomePane();
                homeController.showPopUpMessage("An error occured. Servers are not responding, you can still browse your profile, friends list and teams.");
            });
        }catch (Exception e){
            Platform.runLater(() -> {
                //update progress
                homeController.unFrostHomePane();
                homeController.showPopUpMessage("An error occured.");
            });
        }
    }

    public void parseRow(Row row) throws Exception {

        DataFormatter formatter = new DataFormatter();

        String category="", title= "", urlCampaign="", startDate="", description="",
                team="", beneficiary="", organizer="";
        double totalRequired = 0, totalAchived = 0;
        ArrayList<String> membersOfTeam = new ArrayList<String>();
        ArrayList<String> listOfDonors = new ArrayList<String>();
        ArrayList<Double> amountsDonated = new ArrayList<Double>();
        ArrayList<String> timestampOfDonations = new ArrayList<String>();

        //this array contains all the index related to a anonymous (need to be not included)
        ArrayList<Integer> anonymousIndexes = new ArrayList<Integer>();

        int maxLenght = -1;

        Iterator<Cell> iterator = row.cellIterator();
        int numberOfcell= -1;
        //for each cell'row
        while(iterator.hasNext()) {
            numberOfcell++;
            Cell cell = iterator.next();

            switch (numberOfcell) {
                case 0: //if category
                    category = formatter.formatCellValue(cell);
                    break;

                case 1: //if title
                    title = formatter.formatCellValue(cell);
                    break;

                case 2: //if urlCampaign
                    urlCampaign = formatter.formatCellValue(cell);
                    break;

                case 3: //if startDate
                    startDate = formatDate.format(cell.getDateCellValue());
                    break;

                case 5: //if totalRequired
                    totalRequired = Double.valueOf(formatter.formatCellValue(cell));
                    break;

                case 8: //if description
                    description = formatter.formatCellValue(cell);
                    break;

                case 9: { //if organizer and members of team
                    String cellValue = formatter.formatCellValue(cell);
                    //split each ',' character
                    membersOfTeam = new ArrayList<String>(Arrays.asList(cellValue.split(",")));
                    for(int i=0; i<membersOfTeam.size();i++) {
                        String username = membersOfTeam.remove(i).replace(" ", ""); //standard username doesn't allow space
                        membersOfTeam.add(i, username);
                    }
                    organizer = membersOfTeam.remove(0);
                    break;
                }

                case 10: //if team
                    team = formatter.formatCellValue(cell);
                    break;


                case 11: //if beneficiary
                    beneficiary = formatter.formatCellValue(cell);
                    break;


                case 12: { //if list of donors (delete all the anonymous)
                    String[] cellValue = formatter.formatCellValue(cell).split(",");

                    int i = 0;
                    for (String donor : cellValue) {

                        if (donor.toLowerCase().contains("ymous") != true && donor.toLowerCase().contains("anonim") != true)
                            listOfDonors.add(donor.replace(" ",""));
                        else
                            anonymousIndexes.add(i);

                        i++;
                    }
                    maxLenght = listOfDonors.size();
                    break;
                }

                case 13: { //if list of amount donated and totalAchived (delete those related to anonymous)
                    String[] cellValue = formatter.formatCellValue(cell).split(",");
                    int i = 0;
                    for (String strAmount : cellValue) {
                        if (anonymousIndexes.contains(i++) == false) {
                            double amount = Double.valueOf(strAmount);
                            amountsDonated.add(amount);
                            totalAchived += amount;
                        }
                    }
                    if(maxLenght>amountsDonated.size())
                        maxLenght = amountsDonated.size();
                    break;
                }

                case 14: //if timestamp
                    int i=0;
                    for(String time: formatter.formatCellValue(cell).split(",")){

                        if(anonymousIndexes.contains(i++)==false){
                            try {
                                timestampOfDonations.add(formatTimestamp.format(formatTimestamp.parse(time)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(maxLenght>timestampOfDonations.size())
                        maxLenght = timestampOfDonations.size();
                    break;


            }

        }


        int diff = amountsDonated.size()-maxLenght;
        if(diff>0 && maxLenght!=-1){
            for(int i=0; i<diff; i++){
                totalAchived-=amountsDonated.get(maxLenght+1);
            }
        }


        //create object campaign
        Campaign campaign = new Campaign(title,
                urlCampaign,
                startDate,
                totalRequired,
                totalAchived,
                listOfDonors.size(),
                organizer,
                membersOfTeam,
                description,
                beneficiary,
                category,
                listOfDonors,
                timestampOfDonations,
                amountsDonated
        );


        //campaign.printCampaign();
        try {
            insertCampaingIntoDB(campaign,team, maxLenght);
        }catch (Exception m){ //use general exception. Here we don't need to get the specific cause.
            throw m;
        }




    }

    //------------------------------------ MAIN METHODS --------------------------------------------------------------
    //here there are the two methods called to insert a new campaign into mongodb and neo4j

    private void insertCampaingIntoDB(Campaign campaign, String team, int maxLenght) throws Exception {

        Document document = new Document("title", campaign.getTitle())
                .append("urlImage",campaign.getCampaignImage())
                .append("startDate", campaign.getStartDate())
                .append("amountRequired",campaign.getCountRequired())
                .append("amountAchived", campaign.getCountAchived())
                .append("countDonations", campaign.getCountDonations())
                .append("description", campaign.getDescription())
                .append("category", campaign.getCategory())
                .append("organizer",campaign.getOrganizer())
                .append("membersOfTeam",campaign.getMembersOfTeam())
                .append("beneficiary", campaign.getBeneficiary());



        final ClientSession clientSession = Connection.getInstance().getMongoClient().startSession();


        TransactionOptions txnOptions = TransactionOptions.builder()
                .writeConcern(WriteConcern.W3)
                .build();

        //define body transaction...
        TransactionBody transaction = new TransactionBody<Void>() {


            @Override
            public Void execute() {

                //append list of donors each of one with their donation and timestamp of it
                ArrayList<Document> donations = new ArrayList<Document>();
                for (
                        int i = 0;
                        i < maxLenght; i++) {

                    try {
                        Date convertedIntoDate = formatTimestamp.parse(campaign.getTimestampDonations().get(i));

                    Document donation = new Document("donor", campaign.getListOfDonors().get(i))
                            .append("category", campaign.getCategory())
                            .append("amount", campaign.getAmountsDonated().get(i))
                            .append("timestamp",convertedIntoDate);

                    donations.add(new Document("donor", campaign.getListOfDonors().get(i))
                            .append("category", campaign.getCategory())
                            .append("amount", campaign.getAmountsDonated().get(i))
                            .append("timestamp", campaign.getTimestampDonations().get(i)));

                    //insert into log of donations
                    Connection.getInstance().getMongoDBCollection("monthlyDonations").insertOne(clientSession,donation);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }



                document.append("donations", donations);

                InsertOneResult result = Connection.getInstance().getMongoDBCollection("campaigns").insertOne(clientSession,document);
                ObjectId objectId = result.getInsertedId().asObjectId().getValue();

                //INSERT INTO NEO4J_______________________________

                insertIntoNeo4J(objectId.toHexString(),
                        campaign.getTitle(),
                        campaign.getCampaignImage(),
                        campaign.getOrganizer(),
                        campaign.getMembersOfTeam(),
                        campaign.getListOfDonors(),
                        campaign.getTimestampDonations(),
                        team,
                        maxLenght);




                return null;

            }
        };

        //start transaction...
        try {
            clientSession.withTransaction(transaction,txnOptions);
        }catch (Exception e) {
            System.out.println("ERROR:"+e.getMessage());
            throw e;
        }


    }

    private void insertIntoNeo4J(final String idCampaign,
                                 final String titleCampaign,
                                 final String urlCampaign,
                                 final String organizer,
                                 final ArrayList<String> membersOfTeam,
                                 final ArrayList<String> listOfDonors,
                                 final ArrayList<String> timestampDonations,
                                 final String team,
                                 final int maxLenght ) throws Neo4jException {


        try (Session session = Connection.getInstance().getDriver().session()){
            session.writeTransaction(new TransactionWork<Void>() {

                @Override
                public Void execute(Transaction transaction) throws Neo4jException {
                    //WRITE A CAMPAIGN NODE
                    insertCampaignIntoNeo4J(transaction, idCampaign, titleCampaign,urlCampaign);

                    //WRITE ALL THE DONORS AS USER NODE___________________________________________________________
                    for(int i=0; i<maxLenght; i++) {

                        GeoData location = geoGenerator.getRandomLocation();
                        final User user = new User(listOfDonors.get(i).replace(" ",""),
                                RandomGenerator.generateRandomPassword(10),
                                "null",
                                location.getRegion(),
                                location.getProvince(),
                                location.getCap());

                        insertUserIntoNeo4J(transaction,user.getUsername(),
                                user.getPassword(),
                                user.getUrlImage(),
                                user.getRegion(),
                                user.getProvince(),
                                user.getCap());

                        createFollowRelationshipWithCampaign(transaction, user.getUsername(), idCampaign,timestampDonations.get(i));
                    }

                    //WRITE A ORGANIZER NODE_____________________________________________________________________

                    GeoData location = geoGenerator.getRandomLocation();
                    final User organizerUser = new User(organizer.replace(" ",""),
                            RandomGenerator.generateRandomPassword(10),
                            "null",
                            location.getRegion(),
                            location.getProvince(),
                            location.getCap());

                    insertUserIntoNeo4J(transaction,organizerUser.getUsername(),
                            organizerUser.getPassword(),
                            organizerUser.getUrlImage(),
                            organizerUser.getRegion(),
                            organizerUser.getProvince(),
                            organizerUser.getCap());

                    //WRITE RELATIONSHIP :ORGANIZES
                    createOrganizeRelationshipWithCampaign(transaction, organizerUser.getUsername(), idCampaign);

                    //WRITE TEAM
                    int idOfTeam = insertTeamIntoNeo4J(transaction,team);

                    //WRITE RELATIONSHIP :CREATES
                    createCreateRelationshipWithTeam(transaction,organizerUser.getUsername(), idOfTeam);

                    //create each member as a user and relationship :belongs for each member of team with the team itself
                    for(int i=0; i<membersOfTeam.size(); i++){
                        GeoData location2 = geoGenerator.getRandomLocation();
                        final User member = new User(membersOfTeam.get(i).replace(" ",""),
                                RandomGenerator.generateRandomPassword(10),
                                "null",
                                location2.getRegion(),
                                location2.getProvince(),
                                location2.getCap());
                        insertUserIntoNeo4J(transaction, member.getUsername(), member.getPassword(), member.getUrlImage(), member.getRegion(), member.getProvince(), member.getCap());
                        createBelongRelationshipWithTeam(transaction, member.getUsername(), idOfTeam);
                    }

                    //create friend relationship between organizer and members of team
                    for(String member: membersOfTeam){

                        createFriendRelationship(transaction,organizer,member);
                    }

                    //create relationship :support
                    createSupportRelationshipWithCampaign(transaction,idOfTeam,idCampaign);

                    return null;
                }
            }); //_______________________________________________________________________________________

        }catch (Neo4jException d){
            throw d;
        }

    }

    //--------------------------------------- SUPPORT'S METHODS for NEO4J ----------------------------------------------------

    private static Void insertCampaignIntoNeo4J(Transaction tx, String idCampaign, String titleCampaign, String urlImage)throws Neo4jException{

        tx.run("CREATE (c:Campaign {idMongoDB: $id, title: $title, urlImage: $urlImage})", parameters("id", idCampaign, "title", titleCampaign, "urlImage", urlImage));
        return null;
    }

    private static Void insertUserIntoNeo4J(Transaction tx,
                                            String username,
                                            String password,
                                            String urlProfileImage,
                                            String region,
                                            String province,
                                            int cap) {

        tx.run("MERGE (n:User { username: $username }) " +
                        "ON CREATE SET " +
                        "n.password = $password," +
                        "n.urlProfileImage = $urlProfileImage," +
                        "n.region = $region," +
                        "n.province = $province," +
                        "n.cap = $cap ",
                parameters("username",username,
                        "password",password,
                        "urlProfileImage",urlProfileImage,
                        "region",region,
                        "province",province,
                        "cap", cap) );
        return null;
    }

    public static Void createFollowRelationshipWithCampaign(Transaction tx, String username, String idCampaign, String timestamp){


        //CREATE RELATIONSHIP WITH CAMPAIGN
        tx.run("MATCH (u:User),(c:Campaign) WHERE u.username = $username AND c.idMongoDB = $idCampaign " +
                        "MERGE (u)-[:FOLLOWS{timestamp:$timestamp}]->(c)",
                parameters("username", username, "idCampaign",idCampaign,"timestamp",timestamp));

        return null;
    }

    public static Void createOrganizeRelationshipWithCampaign(Transaction tx, String username, String idCampaign){

        //CREATE RELATIONSHIP WITH CAMPAIGN
        tx.run("MATCH (u:User),(c:Campaign) WHERE u.username = $username AND c.idMongoDB = $idCampaign " +
                        "MERGE (u)-[:ORGANIZES]->(c)",
                parameters("username", username, "idCampaign",idCampaign));

        return null;
    }

    public static Void createCreateRelationshipWithTeam(Transaction tx, String username, int idTeam){

        //CREATE RELATIONSHIP WITH CAMPAIGN
        tx.run("MATCH (u:User),(t:Team) WHERE u.username = $username AND ID(t) = $idTeam " +
                        "MERGE (u)-[:CREATES]->(t)",
                parameters("username", username, "idTeam",idTeam));

        return null;
    }

    public static Void createBelongRelationshipWithTeam(Transaction tx, String username, int idTeam){

        //CREATE RELATIONSHIP WITH CAMPAIGN
        tx.run("MATCH (u:User),(t:Team) WHERE u.username = $username AND ID(t) = $idTeam " +
                        "MERGE (u)-[:BELONGS]->(t)",
                parameters("username", username, "idTeam",idTeam));

        return null;
    }


    public static Void createSupportRelationshipWithCampaign(Transaction tx, int idTeam, String idCampaign){

        //CREATE RELATIONSHIP WITH CAMPAIGN
        tx.run("MATCH (t:Team),(c:Campaign) WHERE ID(t) = $idTeam AND c.idMongoDB = $idCampaign " +
                        "MERGE (t)-[:SUPPORTS]->(c)",
                parameters("idTeam", idTeam, "idCampaign",idCampaign));

        return null;
    }

    public static Void createFriendRelationship(Transaction tx, String username1, String username2){

        //CREATE FRIEND RELATIONSHIP
        tx.run("MATCH (u:User), (f:User) WHERE u.username=$user1 AND f.username=$user2 \n" +
                        "MERGE (u)-[r:FRIEND_OF]->(f) RETURN u,r,f",
                parameters("user1",username1,"user2",username2));

        return null;
    }

    public static int insertTeamIntoNeo4J(Transaction tx, String nameOfTeam){

        Result result = tx.run("MERGE (t:Team {name: $nameOfTeam }) RETURN ID(t)",parameters("nameOfTeam",nameOfTeam));
        int idTeam = result.single().get(0).asInt();
        return idTeam;

    }

}
