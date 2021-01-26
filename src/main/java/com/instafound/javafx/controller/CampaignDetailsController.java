package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import com.instafound.javafx.backend.queries.Update;
import com.instafound.javafx.model.CampaignMongoDB;
import com.instafound.javafx.model.Donor;
import com.instafound.javafx.model.UserInfo;
import com.instafound.javafx.service.DonorsCampaignLoader;
import com.mongodb.MongoTimeoutException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CampaignDetailsController implements Initializable {

    @FXML
    ProgressIndicator progressIndicatorCampaignDetails,progressIndicatorListOfDonors;
    @FXML
    Label titleCampaign, labelCategory, labelNumberOfDonations,labelTotalRequired,labelTotalAchived,labelBeneficiaryName, mexOfThanksByOrganizer, labelBeneficiary, labelSupporters;
    @FXML
    TextArea areaDescription;
    @FXML
    ImageView imageViewCampaign;
    @FXML
    ProgressBar progressBarCampaign;
    @FXML
    HBox boxOrganizer, boxMembers;
    @FXML
    VBox boxDonors;
    @FXML
    Label buttonFollowCampaign, labelCountDonors;
    @FXML
    TextField areaDonate;
    @FXML
    Pane paneInteractive;

    private HomeController homeController;
    private UserInfo userInfo;
    private ArrayList<UserInfo> friends;
    private ArrayList<String> idFollowedCampaigns;
    private CampaignMongoDB campaignInfo;
    private String mode;

    public void setHomeController(HomeController homeController){
        this.homeController = homeController;
    }
    public void setUserInfo(UserInfo userInfo, ArrayList<UserInfo> friends){

        this.friends = friends;
        this.userInfo = userInfo;
    }

    public void backToHome(MouseEvent mouseEvent) {
        try {
            homeController.getContainerCentralHome().setVisible(true);
            homeController.getContainerOtherInterfaces().setVisible(false);

        }catch (NullPointerException m){
            destroy();
        }
    }

    public void showUnfollowLabel(MouseEvent mouseEvent) {
    }

    public void showFollowedLabel(MouseEvent mouseEvent) {
    }

    public void loadCampaign() {

        if(mode.compareTo(HomeController.EXPLORATION_MODE)==0) {
            paneInteractive.setVisible(false);
            //update UI
            updateStaticUI();
        }else {
            updateStaticUI();
            updateUI();
        }

        //load donors
        loadDonors();
    }

    private void loadDonors(){

        DonorsCampaignLoader donorsCampaignLoader = new DonorsCampaignLoader(this);
        donorsCampaignLoader.start();
    }

    public List<Donor> getDonationsList(){

        return campaignInfo.getDonations();
    }

    public void updateLabelCountDonations(int n){

        labelCountDonors.setText("List of all donors ("+n+")");
    }

    public List<UserInfo> getFriendsOfCurrentUser(){

        return friends;
    }

    public String getUsernameOfCurrentUser(){

        return userInfo.getUsername();
    }

    private void updateStaticUI(){ //This part is in common between NORMAL_MODE and EXPLORATION_MODE

        titleCampaign.setText(campaignInfo.getTitle());
        labelCategory.setText(campaignInfo.getCategory());
        labelNumberOfDonations.setText(String.valueOf(campaignInfo.getCountDonations()));
        labelTotalRequired.setText("Total Required: "+campaignInfo.getAmountRequired()+"€");
        labelTotalAchived.setText("Total Achived: "+campaignInfo.getAmountAchived()+"€");
        updateProgressBar();
        labelBeneficiaryName.setText((campaignInfo.getBeneficiary().compareTo("null")==0)?(""):campaignInfo.getBeneficiary());
        areaDescription.setText(campaignInfo.getDescription());
        if (campaignInfo.getBeneficiary().compareTo("null") == 0) {
            labelBeneficiary.setVisible(false);
        } else {
            labelBeneficiary.setVisible(true);
        }

        //load organizer and members
        loadOrganizerAndMembers();

        try {
            imageViewCampaign.setImage(new Image(campaignInfo.getUrlImage()));
        }catch (NullPointerException | IllegalArgumentException e) {
            Image image = new Image("https://www.debonisarredo.it/site/images/joomlart/demo/default.jpg");
            imageViewCampaign.setImage(image);
        }
    }

    private void updateUI(){

        //if the campaign is already followed by the user...
        if(idFollowedCampaigns.contains(campaignInfo.get_id().toHexString())) {
            buttonFollowCampaign.setText("followed");
            buttonFollowCampaign.setOnMouseClicked(null);
        }
    }

    private void updateProgressBar(){
        BigDecimal achived = new BigDecimal(campaignInfo.getAmountAchived());
        BigDecimal required = new BigDecimal(campaignInfo.getAmountRequired());
        BigDecimal progress = new BigDecimal(achived.doubleValue()/required.doubleValue());
        double updateProgress = (progress.doubleValue()>1) ? 1 : progress.doubleValue();
        progressBarCampaign.setProgress(updateProgress);

    }

    private void loadOrganizerAndMembers() {


        //Load organizer...
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../usernameMemberModel.fxml"));
        try {
            Parent root = (Parent) loader.load();
            UsernameMemberController organizer = loader.getController();
            organizer.setUsername(campaignInfo.getOrganizer());
            organizer.setReference(this);

            if(mode.compareTo(HomeController.NORMAL_MODE)==0) {

                organizer.setCurrentUserUsername(userInfo.getUsername());
                //check if current user is already friend with the organizer
                boolean isFriend = checkFriendRelationship(campaignInfo.getOrganizer());
                if (isFriend == true)
                    organizer.hideAddFriendButton();
            }else
                organizer.hideAddFriendButton();

            boxOrganizer.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Load members...
        ArrayList<String> usernameMembers = campaignInfo.getMembersOfTeam();
        for(int i=0; i<usernameMembers.size(); i++) {
            try {
                FXMLLoader loader2 = new FXMLLoader(getClass().getResource(
                        "../usernameMemberModel.fxml"));
                Parent root = (Parent) loader2.load();
                UsernameMemberController member = loader2.getController();
                member.setUsername(usernameMembers.get(i));

                if(mode.compareTo(HomeController.NORMAL_MODE)==0) {
                    member.setCurrentUserUsername(userInfo.getUsername());
                    //check if user is already friend with the member
                    boolean isFriend = checkFriendRelationship(usernameMembers.get(i));
                    if (isFriend == true) {
                        member.hideAddFriendButton();
                    }
                }else
                    member.hideAddFriendButton();

                boxMembers.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(usernameMembers.size()==0)
            labelSupporters.setVisible(false);


    }

    private boolean checkFriendRelationship(String person) {

        if(person.compareTo(userInfo.getUsername())==0) //if the user is the same current user...
            return true;

        for(UserInfo friend : friends){

            if(friend.getUsername().compareTo(person)==0) //if the person is friend of user
                return true;
        }

        return false;
    }

    public void setCampaignInfo(CampaignMongoDB campaignInfo) {

        this.campaignInfo = campaignInfo;
    }

    public void followCampaign(MouseEvent mouseEvent) {

        boolean outcome = Insert.followCampaign(userInfo.getUsername(), campaignInfo.get_id().toHexString());
        if(outcome==true) {
            buttonFollowCampaign.setText("followed");
            buttonFollowCampaign.setOnMouseClicked(null);
        }
    }

    public void setFollowedCampaigns(ArrayList<String> idFollowedCampaigns) {

        this.idFollowedCampaigns = idFollowedCampaigns;
    }

    public void setProgressIndicatorVisibilityOfDonorList(boolean visibility) {

        progressIndicatorListOfDonors.setVisible(visibility);
    }

    public void addDonorToListUI(Parent root) {

        boxDonors.getChildren().add(root);
    }

    public void donateCampaign(MouseEvent mouseEvent) {

       boolean outcome = checkDonateArea();

       if(outcome==true) {

           try {
               boolean outcomeUpdating = Update.donateCampaign(campaignInfo.get_id().toHexString(), campaignInfo.getCategory(), userInfo.getUsername(), Double.valueOf(areaDonate.getText()));
               mexOfThanksByOrganizer.setVisible(true);
               if (outcomeUpdating == true) {//update UI
                   mexOfThanksByOrganizer.setStyle("-fx-text-fill: green");
                   mexOfThanksByOrganizer.setText(campaignInfo.getOrganizer() + " thanks you!");
                   labelNumberOfDonations.setText("" + (Integer.valueOf(labelNumberOfDonations.getText()) + 1));
                   BigDecimal tot = new BigDecimal(campaignInfo.getAmountAchived()).add(new BigDecimal(Double.valueOf(areaDonate.getText())));
                   campaignInfo.setAmountAchived(tot.doubleValue());
                   labelTotalAchived.setText("Total achived: " + campaignInfo.getAmountAchived() + "€");
                   updateProgressBar();

                   //add donor into the list
                   FXMLLoader loader = new FXMLLoader();
                   loader = new FXMLLoader(getClass().getResource(
                           "../donorModel.fxml"));
                   Parent root = null;

                   try {
                       root = (Parent) loader.load();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }

                   DonorController donorController = loader.getController();
                   donorController.setUsername(getUsernameOfCurrentUser());
                   donorController.setCurrentUserUsername(getUsernameOfCurrentUser());
                   donorController.setTimestampOfDonationLabel("now");
                   donorController.setAmountDonated(Double.valueOf(areaDonate.getText()));
                   donorController.setReference(this);
                   boxDonors.getChildren().add(0, root); //top of the list
                   areaDonate.setText("");

               } else {
                   mexOfThanksByOrganizer.setStyle("-fx-text-fill: red");
                   mexOfThanksByOrganizer.setText("An error has occurred. Try again later");
               }
           }catch (MongoTimeoutException m){
               homeController.showPopUpMessage("At the moment it is not possible to make any donations. We apologize for the inconvenience. Please try again later.");
           }

       }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private boolean checkDonateArea() {

        String numberInserted = areaDonate.getText();
        if(numberInserted.length()==0) {
            areaDonate.setStyle("-fx-border-color: red");
            return false;
        }
        if (numberInserted.matches("[a-zA-Z_]+")) {
            areaDonate.setStyle("-fx-border-color: red");
            return false;
        }
        else
            areaDonate.setStyle("");

        return true;
    }


    public void setMode(String explorationMode) {

        this.mode = explorationMode;
    }

    public String getMode(){

        return this.mode;
    }

    public void destroy() {

        homeController.destroyPane();
    }

    public void showPopUpOfExplorationMode() {

        homeController.showPopUpOfExplorationMode();
    }

    public void showPopUpMessage(String s) {

        homeController.showPopUpMessage(s);
    }
}
