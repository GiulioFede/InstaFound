package com.instafound.javafx.controller;


import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.backend.queries.Update;
import com.instafound.javafx.model.*;
import com.instafound.javafx.service.CampaignCreatedLoader;
import com.instafound.javafx.service.CampaignFollowedLoader;
import com.instafound.javafx.service.SuggestedFriendsLoader;
import com.mongodb.MongoTimeoutException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.bson.types.ObjectId;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProfileController implements Initializable {

    @FXML
    Label labelUsernameProfile,labelLocationProfile,titleStatisticLabel;
    @FXML
    Pane mainProfilePane,statisticsPane,containerCentralHome,containerProfile,newUrlPane;
    @FXML
    Circle circleImageView;
    @FXML
    HBox containerCampaignsCreated, containerCampaignsFollowed,containerSuggestedFriends;
    @FXML
    ProgressIndicator progressIndicatorCampaignsCreated,progressIndicatorCampaignsFollowed,progressIndicatorFriendsSuggested;
    @FXML
    BarChart barChartStatisticsCampaign;
    @FXML
    Button showDonationsTrendButton, showFollowersTrendButton;
    @FXML
    NumberAxis barCharYLabel;
    @FXML
    TextField textFieldNewUrl;

    UserInfo userInfo;
    private HomeController homeController;

    private ArrayList<PreviewCampaignCreatedController> campaignCreatedControllers;
    private ArrayList<PreviewCampaignFollowedController> campaignFollowedControllers;
    private PreviewCampaignCreatedController selectedCampaignCreated;

    private static final Effect frostEffect = new BoxBlur(100,100,3);
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
    private static Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Italy"));



    private void loadUserProfileImage() throws NullPointerException{

        labelUsernameProfile.setText(userInfo.getUsername());

        labelLocationProfile.setText(userInfo.getCap()+","+userInfo.getProvince()+","+userInfo.getRegion());

        circleImageView.setStroke(Color.TRANSPARENT);
        try {
            Image profileImage = new Image(userInfo.getUrlProfileImage());
            circleImageView.setFill(new ImagePattern(profileImage));
            circleImageView.setEffect(new DropShadow(+25d,0d,+2d, Color.LIGHTGRAY));
        }catch (NullPointerException | IllegalArgumentException e){
            Image profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
            circleImageView.setFill(new ImagePattern(profileImage));
            circleImageView.setEffect(new DropShadow(+25d,0d,+2d, Color.LIGHTGRAY));
        }

    }

    public void loadSuggestedFriends(){

        try {
            for(int i=0; i<3; i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "suggestedFriendModel.fxml"));
                Parent root = (Parent) loader.load();
                SuggestedFriendController suggestedFriendController = (SuggestedFriendController) loader.getController();
                suggestedFriendController.setProfileImage("");
                containerSuggestedFriends.getChildren().add(root);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setHomePanel(HomeController homeController, Pane containerCentralHome, Pane containerProfile){

        this.homeController = homeController;
        this.containerCentralHome= containerCentralHome;
        this.containerProfile = containerProfile;

    }

    public String getUsernameOfCurrentUser(){

        return userInfo.getUsername();
    }

    public ProgressIndicator getProgressIndicatorFriendsSuggested(){

        return progressIndicatorFriendsSuggested;
    }

    public HBox getContainerSuggestedFriends(){

        return containerSuggestedFriends;
    }

    public void initProfile(UserInfo userInfo){

        this.userInfo = userInfo;

        try {
            loadUserProfileImage();
        }catch(NullPointerException npe){

            //assign to the user a default image
            circleImageView.setStroke(Color.TRANSPARENT);
            Image profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
            circleImageView.setFill(new ImagePattern(profileImage));
            circleImageView.setEffect(new DropShadow(+25d,0d,+2d,Color.LIGHTGRAY));

        }

        final CampaignCreatedLoader campaignCreatedLoader = new CampaignCreatedLoader(this);
        campaignCreatedLoader.start();


        final CampaignFollowedLoader campaignFollowedLoader = new CampaignFollowedLoader(this);
        campaignFollowedLoader.start();

        final SuggestedFriendsLoader suggestedFriendsLoader = new SuggestedFriendsLoader(this);
        suggestedFriendsLoader.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        campaignCreatedControllers = new ArrayList<>();
        campaignFollowedControllers = new ArrayList<>();
        statisticsPane.setVisible(false);

    }

    public ProgressIndicator getProgressIndicatorCampaignsFollowed(){
        return progressIndicatorCampaignsFollowed;
    }

    public HBox getContainerCampaignsFollowed(){
        return containerCampaignsFollowed;
    }

    public void addCampaignCreated(PreviewCampaignCreatedController previewCampaignController){

        campaignCreatedControllers.add(previewCampaignController);
    }

    public void clearCampaignCreated(){

        campaignCreatedControllers.clear();
    }

    private void frostMainPane(){

        mainProfilePane.setEffect(frostEffect);
    }

    private void unFrostMainPane(){

        mainProfilePane.setEffect(null);
    }

    //-------------------------------------------------- STATISTICS CAMPAIGN --------------------------------------------------------------------------------

    public void openStaticsPane(PreviewCampaignCreatedController previewCampaignCreatedController){

        statisticsPane.setVisible(true);
        frostMainPane();
        //indicate the selected campaign
        selectedCampaignCreated = previewCampaignCreatedController;
        titleStatisticLabel.setText("statistics of: \""+selectedCampaignCreated.getTitle()+"\"");
    }

    public void closeStatisticsPane(){

        statisticsPane.setVisible(false);
        unFrostMainPane();
        selectedCampaignCreated = null;
    }

    public void backToHome(MouseEvent mouseEvent) {
        homeController.highlightsButton(0);
        containerCentralHome.setVisible(true);
        containerProfile.setVisible(false);
    }

    public void showDonationsTrend(MouseEvent mouseEvent) {

        try {
            ArrayList<AnalyticalData> analyticalData = Request.getDonationsTrend(selectedCampaignCreated.getObjectId());
            //load data into UI
            loadBarChart("donation's trend", "average of donations", analyticalData, sdf);
        }catch(MongoTimeoutException mte){
            showPopUpMessage("At the moment it is not possible to contact servers. Please try again later.");
        }catch (Exception e){
            showPopUpMessage("At the moment it is not possible to see the statistics. Please try again later.");
        }
    }

    public void showFollowersTrend(MouseEvent mouseEvent) {

        ArrayList<AnalyticalData> analyticalData = Request.getFollowersTrend(selectedCampaignCreated.getObjectId().toHexString());
        loadBarChart("follower's trend",  "total followed achived", analyticalData,sdf);
    }

    public ProgressIndicator getProgressIndicatorCampaignsCreated(){
        return progressIndicatorCampaignsCreated;
    }

    public HBox getContainerCampaignsCreated(){
        return containerCampaignsCreated;
    }

    private void loadBarChart(String title,
                              String yLabel,
                              ArrayList<AnalyticalData> averageOfData,
                              SimpleDateFormat parser){

        //refresh
        barChartStatisticsCampaign.getData().clear();

        barCharYLabel.setLabel(yLabel);

        barChartStatisticsCampaign.setTitle(title);
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);
        XYChart.Series newData = new XYChart.Series();
        //add data
        for(AnalyticalData data: averageOfData){
            try {
                cal.setTime(parser.parse(data.getKey()));
                newData.getData().add(new XYChart.Data(cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+(cal.get(Calendar.DAY_OF_MONTH)+1),data.getValue()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //add data to a barChart graph
        barChartStatisticsCampaign.getData().add(newData);
    }

    //------------------------------------------------ DETAILS CAMPAIGN ------------------------------------------------

    public void openDetailsCampaign(ObjectId idCampaign){

        homeController.openCampaignDetailsView(idCampaign);

    }

    public void removeFollowedCampaignFromUI(PreviewCampaignFollowedController previewCampaignFollowedController) {

        int index = campaignFollowedControllers.indexOf(previewCampaignFollowedController);
        containerCampaignsFollowed.getChildren().remove(index);
    }

    public void addFollowedCampaign(PreviewCampaignFollowedController campaignFollowedController) {

        campaignFollowedControllers.add(campaignFollowedController);
    }

    public void closeUrlPane(MouseEvent mouseEvent) {

        newUrlPane.setVisible(false);
        unFrostMainPane();
    }

    public void openUrlPanel(MouseEvent mouseEvent) {

        newUrlPane.setVisible(true);
        frostMainPane();
        textFieldNewUrl.setStyle("");
    }

    public void saveNewUrl(MouseEvent mouseEvent) {

        String newUrlInserted = textFieldNewUrl.getText();
        if(!newUrlInserted.isEmpty()) {

            try {
                boolean outcome = Update.updateUrlProfileImageUser(userInfo.getUsername(), newUrlInserted);
                if (outcome) {
                    //update profile image with new url
                    userInfo.setUrlProfileImage(newUrlInserted);
                    newUrlPane.setVisible(false);
                    unFrostMainPane();
                    loadUserProfileImage();
                    //also update left side image
                    homeController.setUserInfo(userInfo);
                    homeController.updateUserUI();
                }
            }catch (DiscoveryException | ServiceUnavailableException s){

                destroy();
                showPopUpOfExplorationMode();

            }catch (Exception n) {
                showPopUpMessage("There was a problem. Impossible to load new profile image.");
            }
        }else
            textFieldNewUrl.setStyle("-fx-border-color: red");


    }

    public void showPopUpMessage(String s) {

        homeController.showPopUpMessage(s);
    }

    //------------------------------------------ EXPLORATION MODE -----------------------------------

    public void showPopUpOfExplorationMode() {

        homeController.showPopUpOfExplorationMode();
    }

    public void destroy() {

        homeController.destroyPane();
    }

}

