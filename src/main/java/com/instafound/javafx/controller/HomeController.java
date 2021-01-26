package com.instafound.javafx.controller;

import com.instafound.javafx.backend.Connection;
import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.model.*;
import com.instafound.javafx.service.*;
import com.instafound.javafx.utilities.DirectoryManager;
import com.mongodb.MongoTimeoutException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.types.ObjectId;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {
    @FXML
    Pane homePane,containerSideMenu,containerLogo,containerCentral,containerOtherInterfaces, synchPane,campaignDetailsViewPane,popUpWindow, mainHomePane;
    @FXML
    VBox boxCampaigns; //container of campaign posts
    @FXML
    Circle profileImageUserHome;
    @FXML
    Label labelUsernameHome,labelUserLocation,popUpMessage, exploreModeLabel,labelSync;
    @FXML
    PieChart cakeGraph;
    @FXML
    TableView rankTable;
    @FXML
    ProgressIndicator progressIndicatorHome,updateProgressBar;
    @FXML
    Button homeProfileButton, logOutButton,closePopUpButton,homeCreateCampaignButton, startExplorationModeButton,homeTeamsButton,homeFriendsButton;
    @FXML
    ImageView iconSync;


    public static final String NORMAL_MODE = "NORMAL_MODE";
    public static final String EXPLORATION_MODE = "EXPLORATION_MODE";
    private UserInfo userInfo;
    private String mode;
    private static final Effect frostEffect = new BoxBlur(100,100,3);
    private int numberOfCampaignDisplayed; //this variable is necessary for skipping (only the keys of index thanks to the index on startDate) the current campaigns
    private String currentCategoryDisplayed;

    //------------------------------------ Mode of use -----------------------------------------------------------

    public void setMode(String mode){

        this.mode = mode;
        if(mode.compareTo(EXPLORATION_MODE)==0){ //if we are in exploration mode only the home itself is allowed to browse

            homeProfileButton.setVisible(false);
            homeCreateCampaignButton.setVisible(false);
            homeFriendsButton.setVisible(false);
            homeTeamsButton.setVisible(false);
            logOutButton.setText("exit");
            exploreModeLabel.setVisible(true);
            labelUsernameHome.setVisible(false);
            profileImageUserHome.setVisible(false);
            labelUserLocation.setVisible(false);

        }

    }

    public String getMode(){

        return this.mode;
    }

    //--------------------------------------------------------------------------------------------------------------

    //is set by task UserInfoLoaderTask
    public void setUserInfo(UserInfo userInfo){

        this.userInfo = userInfo;
    }

    public void updateUserUI() {

        labelUsernameHome.setText(userInfo.getUsername());

        labelUserLocation.setText(userInfo.getCap()+","+userInfo.getProvince()+","+userInfo.getRegion());
        profileImageUserHome.setStroke(Color.TRANSPARENT);
        try {
            Image profileImage = new Image(userInfo.getUrlProfileImage());
            profileImageUserHome.setFill(new ImagePattern(profileImage));
            profileImageUserHome.setEffect(new DropShadow(+25d,0d,+2d,Color.valueOf("25305c")));
        }catch (NullPointerException | IllegalArgumentException e){
            Image profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
            profileImageUserHome.setFill(new ImagePattern(profileImage));
            profileImageUserHome.setEffect(new DropShadow(+25d,0d,+2d,Color.valueOf("25305c")));
        }
    }

    private void initCakeGraph(){

        //set title of graph
        cakeGraph.setTitle("Top Monthly Categories");

        //load in background due to the complexity
        TopCategoryLoader topCategoryLoader = new TopCategoryLoader(this);
        topCategoryLoader.start();

    }

    public void updateCakeUI(ArrayList<AnalyticalData> topCategories){

        //set data inside g
        List<PieChart.Data> listData = new ArrayList<>();
        for(int i=0; i<topCategories.size(); i++){
            listData.add(new PieChart.Data(topCategories.get(i).getKey(),topCategories.get(i).getValue()));
        }
        ObservableList<PieChart.Data> cakeGraphdata = FXCollections.observableArrayList(listData);

        cakeGraph.setData(cakeGraphdata);
    }

    private void initRankingOfDonors() {

        TopDonorsLoader topDonorsLoader = new TopDonorsLoader(this);
        topDonorsLoader.start();
    }

    public void updateDonorsRankingUI(ArrayList<DonorRankModel> topDonors){

        rankTable.refresh();
        rankTable.getColumns().clear();


        final ObservableList<DonorRankModel> donors = FXCollections.observableArrayList(topDonors);

        //columns
        TableColumn usernameColumn = new TableColumn("Username");
        TableColumn amountDonatedColumn = new TableColumn("AmountDonated");
        usernameColumn.setCellValueFactory(new PropertyValueFactory("username"));
        amountDonatedColumn.setCellValueFactory(new PropertyValueFactory("totalAmountDonated"));

        rankTable.getColumns().add(0,usernameColumn);
        rankTable.getColumns().add(1,amountDonatedColumn);
        rankTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //fix dimensions

        rankTable.setItems(donors);
    }

    //load last campaigns
    private void loadLastCampaigns(){

        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"null",10,0);
        campaignHomeLoader.start();
    }

    public void loadHome(){

        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "null";

        //if we are in exploration mode we allow user only to browse the home
        if(getMode().compareTo(NORMAL_MODE)==0) {

            labelUsernameHome.setVisible(true);
            profileImageUserHome.setVisible(true);
            labelUserLocation.setVisible(true);

            try {
                updateUserUI(); //fast thanks to the fact SignInController has just retrieved all the information before home started
            } catch (NullPointerException npe) {
                showPopUpMessage("url of your image is not valid. Please change it.");
                //assign to a user a default image...
                profileImageUserHome.setStroke(Color.TRANSPARENT);
                Image profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
                profileImageUserHome.setFill(new ImagePattern(profileImage));
                profileImageUserHome.setEffect(new DropShadow(+25d, 0d, +2d, Color.valueOf("25305c")));
            }
        }

        //load the latest campaigns (in background)
        loadLastCampaigns();

        //load top monthly categories (in background)
        initCakeGraph();

        //load top monthly donors (in background)
        initRankingOfDonors();
    }


    //------------------------------------ SECTIONS OF PLATFORM -------------------------------------------------------

    public void openUserProfile(MouseEvent mouseEvent) {

        //highlights the home button
        highlightsButton(1);

        //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../profile.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }


        containerCentral.setVisible(false);
        containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
        containerOtherInterfaces.getChildren().add(root);
        containerOtherInterfaces.setVisible(true);

        progressIndicatorHome.setVisible(false);


        ProfileController profileController = (ProfileController) loader.getController();
        profileController.setHomePanel(this, containerCentral, containerOtherInterfaces);
        profileController.initProfile(userInfo);



    }

    public void openFriendsList(MouseEvent mouseEvent) {

        //hightlights
        highlightsButton(3);

        //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI FRIENDS LIST
        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../friendsList.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        containerCentral.setVisible(false);
        containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
        containerOtherInterfaces.getChildren().add(root);
        containerOtherInterfaces.setVisible(true);

        progressIndicatorHome.setVisible(false);


        FriendsListController friendsListController = (FriendsListController) loader.getController();
        friendsListController.setReference(this);
        friendsListController.setHomePanel(containerCentral, containerOtherInterfaces);
        friendsListController.setUserInfo(userInfo);
        friendsListController.loadFriendsList();
    }

    public void openTeamView(MouseEvent mouseEvent) {

        //hightlights
        highlightsButton(4);

        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../teamView.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        containerCentral.setVisible(false);
        containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
        containerOtherInterfaces.getChildren().add(root);
        containerOtherInterfaces.setVisible(true);

        progressIndicatorHome.setVisible(false);


        TeamViewController teamViewController = (TeamViewController) loader.getController();
        teamViewController.setReference(this);
        teamViewController.setHomePanel(containerCentral, containerOtherInterfaces);
        teamViewController.setUserInfo(userInfo);
        teamViewController.loadTeams();

    }

    public void openCreateNewCampaignView(MouseEvent mouseEvent) {

        //hightlights button
        highlightsButton(2);

        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../createCampaignView.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        containerCentral.setVisible(false);
        containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
        containerOtherInterfaces.getChildren().add(root);
        containerOtherInterfaces.setVisible(true);

        progressIndicatorHome.setVisible(false);


        CreateNewCampaignController createNewCampaignController = (CreateNewCampaignController) loader.getController();
        createNewCampaignController.setReference(this);
        createNewCampaignController.setHomePanel(containerCentral, containerOtherInterfaces);
        createNewCampaignController.setUserInfo(userInfo);

    }

    //open details of preview campaign (preview is intedend as preview campaign of neo4j, so we need to retrieve all the info)
    public void openCampaignDetailsView(ObjectId idCampaign){

        highlightsButton(5);

        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../detailsCampaignView.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //block UI until finished
        try {
            CampaignMongoDB campaignInfo = Request.getDetailsCampaign(idCampaign);
            containerCentral.setVisible(false);
            containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
            containerOtherInterfaces.getChildren().add(root);
            containerOtherInterfaces.setVisible(true);

            progressIndicatorHome.setVisible(false);

            //block UI
            ArrayList<UserInfo> friends = new ArrayList<>();
            ArrayList<String> idFollowedCampaigns = new ArrayList<>();
            if (getMode().compareTo(NORMAL_MODE) == 0) {
                progressIndicatorHome.setVisible(true);
                friends = Request.getUserFriends(userInfo.getUsername()); //find all the current friends
                try {
                    idFollowedCampaigns = Request.getIdOfFollowedCampaigns(userInfo.getUsername());
                }catch (DiscoveryException | ServiceUnavailableException s){
                    showPopUpOfExplorationMode();
                }catch (Exception n) {
                    showPopUpMessage("There was a problem. Please try later.");
                }
                progressIndicatorHome.setVisible(false);
            }


            CampaignDetailsController campaignDetailsController = (CampaignDetailsController) loader.getController();
            campaignDetailsController.setHomeController(this);
            campaignDetailsController.setUserInfo(userInfo, friends);

            //if we are in exploration mode we show only the details of campaign avoiding to show all the interactive parts (such as "make a donation")
            if (getMode().compareTo(EXPLORATION_MODE) == 0)
                campaignDetailsController.setMode(EXPLORATION_MODE);
            else
                campaignDetailsController.setMode(NORMAL_MODE);

            campaignDetailsController.setCampaignInfo(campaignInfo);
            campaignDetailsController.setFollowedCampaigns(idFollowedCampaigns);
            campaignDetailsController.loadCampaign();
        }catch (MongoTimeoutException m){
            showPopUpMessage("At the moment it is not possible to contact servers to see the details of the campaign. Please try again later.");
        }catch (Exception e){
            showPopUpMessage("At the moment it is not possible to get details of the campaign. Please try again later.");
        }
    }

    //open details of preview campaign (preview is intedend as preview campaign of mongodb, so we have just all we need)
    public void openCampaignDetailsView(CampaignMongoDB campaignMongoDB){

        highlightsButton(5);

        FXMLLoader loader = new FXMLLoader();
        loader = new FXMLLoader(getClass().getResource(
                "../detailsCampaignView.fxml"));
        Parent root = null;

        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        containerCentral.setVisible(false);
        containerOtherInterfaces.getChildren().removeAll(containerOtherInterfaces.getChildren());
        containerOtherInterfaces.getChildren().add(root);
        containerOtherInterfaces.setVisible(true);

        progressIndicatorHome.setVisible(false);

        //block UI
        ArrayList<UserInfo> friends = new ArrayList<>();
        ArrayList<String> idFollowedCampaigns = new ArrayList<>();
        if(getMode().compareTo(NORMAL_MODE)==0) {
            progressIndicatorHome.setVisible(true);
            friends = Request.getUserFriends(userInfo.getUsername()); //find all the current friends
            try {
                idFollowedCampaigns = Request.getIdOfFollowedCampaigns(userInfo.getUsername());
            }catch (DiscoveryException | ServiceUnavailableException s){
                showPopUpOfExplorationMode();
            }catch (Exception n) {
                showPopUpMessage("There was a problem. Please try later.");
            }
            progressIndicatorHome.setVisible(false);
        }

        CampaignDetailsController campaignDetailsController = (CampaignDetailsController) loader.getController();
        campaignDetailsController.setHomeController(this);

        //if we are in exploration mode we show only the details of campaign avoiding to show all the interactive parts (such as "make a donation")
        if(getMode().compareTo(EXPLORATION_MODE)==0)
            campaignDetailsController.setMode(EXPLORATION_MODE);
        else
            campaignDetailsController.setMode(NORMAL_MODE);

        campaignDetailsController.setUserInfo(userInfo, friends);
        campaignDetailsController.setCampaignInfo(campaignMongoDB);
        campaignDetailsController.setFollowedCampaigns(idFollowedCampaigns);
        campaignDetailsController.loadCampaign();
    }

    public Pane getContainerCentralHome(){
        return containerCentral;
    }

    public Pane getContainerOtherInterfaces(){
        return containerOtherInterfaces;
    }

    //given i highlights the i° button of the home interface
    public void highlightsButton(int i){

        switch (i){
            case 1:
            {
                homeProfileButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                homeProfileButton.getStyleClass().add("buttonsHomePressed"); //ad new class name of belonging

                homeCreateCampaignButton.getStyleClass().remove(1);
                homeCreateCampaignButton.getStyleClass().add("buttonsHome");

                homeFriendsButton.getStyleClass().remove(1);
                homeFriendsButton.getStyleClass().add("buttonsHome");

                homeTeamsButton.getStyleClass().remove(1);
                homeTeamsButton.getStyleClass().add("buttonsHome");

                break;
            }
            case 2:
            {
                homeCreateCampaignButton.getStyleClass().remove(1);
                homeCreateCampaignButton.getStyleClass().add("buttonsHomePressed");

                homeProfileButton.getStyleClass().remove(1);
                homeProfileButton.getStyleClass().add("buttonsHome");

                homeFriendsButton.getStyleClass().remove(1);
                homeFriendsButton.getStyleClass().add("buttonsHome");

                homeTeamsButton.getStyleClass().remove(1);
                homeTeamsButton.getStyleClass().add("buttonsHome");
                break;
            }

            case 3:
            {
                homeFriendsButton.getStyleClass().remove(1);
                homeFriendsButton.getStyleClass().add("buttonsHomePressed");

                homeProfileButton.getStyleClass().remove(1);
                homeProfileButton.getStyleClass().add("buttonsHome");

                homeCreateCampaignButton.getStyleClass().remove(1);
                homeCreateCampaignButton.getStyleClass().add("buttonsHome");

                homeTeamsButton.getStyleClass().remove(1);
                homeTeamsButton.getStyleClass().add("buttonsHome");
                break;
            }

            case 4:
            {
                homeTeamsButton.getStyleClass().remove(1);
                homeTeamsButton.getStyleClass().add("buttonsHomePressed");

                homeProfileButton.getStyleClass().remove(1);
                homeProfileButton.getStyleClass().add("buttonsHome");

                homeCreateCampaignButton.getStyleClass().remove(1);
                homeCreateCampaignButton.getStyleClass().add("buttonsHome");

                homeFriendsButton.getStyleClass().remove(1);
                homeFriendsButton.getStyleClass().add("buttonsHome");
                break;
            }

            default:
            {
                homeTeamsButton.getStyleClass().remove(1);
                homeTeamsButton.getStyleClass().add("buttonsHome");

                homeProfileButton.getStyleClass().remove(1);
                homeProfileButton.getStyleClass().add("buttonsHome");

                homeCreateCampaignButton.getStyleClass().remove(1);
                homeCreateCampaignButton.getStyleClass().add("buttonsHome");

                homeFriendsButton.getStyleClass().remove(1);
                homeFriendsButton.getStyleClass().add("buttonsHome");
                break;
            }
        }
    }

    //------------------------------------- STATSYSTICS ----------------------------------------------------------------

    public void refreshCakeUI(MouseEvent mouseEvent) {

        initCakeGraph();
    }

    public void refreshDonorsRanking(MouseEvent mouseEvent) {

        initRankingOfDonors();
    }

    public void addCampaignToHomeUI(Parent root){
        boxCampaigns.getChildren().add(root);
    }

    public void removeCampaignsFromHomeUI(){

        boxCampaigns.getChildren().clear();
    }

    //------------------------ Loading Category Buttons---------------------------------------------------

    public void loadMedicalExpensesCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "Medical Expenses";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"Medical Expenses", 10,0);
        campaignHomeLoader.start();

    }

    public void loadEnvironmentCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "Environment";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"Environment",10,0);
        campaignHomeLoader.start();
    }

    public void loadNoProfitCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "No-Profit";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"No-Profit",10,0);
        campaignHomeLoader.start();
    }

    public void loadCreativityCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "Creativity";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"Creativity",10,0);
        campaignHomeLoader.start();
    }

    public void loadAnimalsCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "Animals";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"Animals",10,0);
        campaignHomeLoader.start();
    }

    public void loadAllCategory(MouseEvent mouseEvent) {

        removeCampaignsFromHomeUI();
        numberOfCampaignDisplayed = 0;
        currentCategoryDisplayed = "null";
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(this,"null",10,0);
        campaignHomeLoader.start();
    }

    public String getCurrentCategoryDisplayed(){
        return currentCategoryDisplayed;
    }

    public void removeLoadMoreButton(){

        boxCampaigns.getChildren().remove(boxCampaigns.getChildren().size()-1);
    }

    //--------------------------- Frost Home Pane ------------------------------------------------
    public void frostHomePane(){

        mainHomePane.setEffect(frostEffect);
    }

    public void unFrostHomePane(){

        mainHomePane.setEffect(null);
    }

    //--------------------------- POPUP WINDOW ---------------------------------------------------

    public void showPopUpMessage(String mex){

        frostHomePane();
        startExplorationModeButton.setVisible(false);
        popUpWindow.setVisible(true);
        popUpMessage.setText(mex);
    }

    public void closePopUpMessage(MouseEvent mouseEvent) {

        popUpWindow.setVisible(false);
        unFrostHomePane();
    }


    //--------------------------- Log Out and Close/Reduce window ------------------------------------------------------------

    public void logOut(MouseEvent mouseEvent) {

        Parent root = null;
        FXMLLoader loader = new FXMLLoader();
        try {
            root = loader.load(getClass().getResource("../StartUp.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();

        //close current
        ((Stage)homePane.getScene().getWindow()).close();
    }

    public void closeMainWindow(MouseEvent mouseEvent) {
        //first close the connection
        try {
            Connection.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.close();
    }

    public void reduceMainWindow(MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.setIconified(true);
    }

    //--------------------------------------------- OTHER METHODS--------------------------------------------------

    //this method restart the social into a exploration mode (it's called only when the are neo4j's problem)

    public void showPopUpOfExplorationMode(){

        frostHomePane();
        showPopUpMessage("A problem has occurred, you cannot continue. Alternatively, you can navigate in exploration mode.");
        startExplorationModeButton.setVisible(true);
        closePopUpButton.setText("exit");
        closePopUpButton.setOnMouseClicked(this::logOut);

    }

    public void restartAsExplorationMode(MouseEvent mouseEvent) {

        popUpWindow.setVisible(false);
        iconSync.setVisible(false);
        labelSync.setVisible(false);
        unFrostHomePane();
        setMode(EXPLORATION_MODE);
        closePopUpButton.setText("close");
        closePopUpButton.setOnMouseClicked(this::closePopUpMessage);
        boxCampaigns.getChildren().clear();

        loadHome();
    }

    public void setVisibilityProgressIndicator(boolean visibility){

        progressIndicatorHome.setVisible(visibility);
    }

    public int getNumberOfCampaignDisplayed() {
        return numberOfCampaignDisplayed;
    }

    public void incrementNumberOfCampaignDisplayed(){
        numberOfCampaignDisplayed += 1;
    }

    public void destroyPane(){

        containerOtherInterfaces.getChildren().clear();
        containerOtherInterfaces.setVisible(false);
        containerCentral.setVisible(true);
    }

    //---------------------------------------- SYNCH DATABASE --------------------------------

    public void synchDB(MouseEvent mouseEvent) {

        String file_path = DirectoryManager.chooseFile();

        if(file_path!=null && "xlsx".compareTo(file_path.substring(file_path.length()-4))==0) {
            UpdateDatabaseLoader updateDatabaseLoader = new UpdateDatabaseLoader(this, file_path);
            updateDatabaseLoader.start();
        }

    }

    public void showUpdateWindow(){

        frostHomePane();
        synchPane.setVisible(true);
        updateProgressBar.setProgress(0);
    }

    public void hideUpdateWindow(){

        unFrostHomePane();
        synchPane.setVisible(false);
    }

    public void setUpdateProgressBar(double val){

        updateProgressBar.setProgress(val);
    }
}


