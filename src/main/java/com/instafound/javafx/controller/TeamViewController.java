package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import com.instafound.javafx.model.Team;
import com.instafound.javafx.model.UserInfo;
import com.instafound.javafx.service.FriendsListLoader;
import com.instafound.javafx.service.TeamLoader;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TeamViewController implements Initializable {

    @FXML
    Pane frostPane,containerCentralHome,containerOtherInterfaces,memberDetailsPane,listOfFriendsToAdAsMemberPane, newTeamPane;
    @FXML
    HBox boxTeams,boxMembers;
    @FXML
    ProgressIndicator progressIndicatorTeams, progressIndicatorMembers, progressIndicatorListOfFriendsToAdd,progressIndicatorNewTeam;
    @FXML
    Label closeDetailsMemberLabel,addMemberButton,labelTeams,labelTitleNewTeam;
    @FXML
    ImageView closeDetailsMemberImage,addMemberImage;
    @FXML
    ListView listViewOtherTeams;
    @FXML
    VBox boxListOfFriendsToAdd;
    @FXML
    Button createNewTeamButton;
    @FXML
    TextField areaNameOfNewTeam;



    private ArrayList<TeamController> teamsController; //array with the controllers of all the teams
    private ArrayList<FriendToAddController> friendsList; //array of All the controller of friends of users
    private TeamController teamSelected; //this variable contains the position (in teamsController) of the selected team

    private UserInfo userInfo;
    private HomeController homeController;
    private static final Effect frostEffect = new BoxBlur(100,100,3);

    public void setUserInfo(UserInfo userInfo){

        this.userInfo = userInfo;
    }

    public void loadTeams(){
        TeamLoader teamLoader = new TeamLoader(this,progressIndicatorTeams,progressIndicatorMembers, boxTeams, boxMembers);
        teamLoader.start();

    }

    public String getUsernameOfCurrentUser(){

        return userInfo.getUsername();
    }

    public void addTeam(TeamController teamController){
        teamsController.add(teamController);
    }

    public void clearTeam(){
        teamsController.clear();
    }

    public void setHomePanel(Pane containerCentralHome, Pane containerProfile){
        this.containerCentralHome= containerCentralHome;
        this.containerOtherInterfaces = containerProfile;

    }

    public void removeMemberFromBox(TeamController teamController, int pos){

        if(teamController.equals(teamSelected))
            boxMembers.getChildren().remove(pos);
    }

    public void backToHome(MouseEvent mouseEvent) {

        homeController.highlightsButton(0);
        containerCentralHome.setVisible(true);
        containerOtherInterfaces.setVisible(false);
    }

    public void showPopUp(String mex){

        homeController.showPopUpMessage(mex);
    }

    public void enableIndicatorForListOfAddableFriends() {

        progressIndicatorListOfFriendsToAdd.setVisible(true);
    }

    public void disenableIndicatorForListOfAddableFriends() {

        progressIndicatorListOfFriendsToAdd.setVisible(false);
    }

    public Pane getFrostPane() {
        return frostPane;
    }

    public HBox getBoxMembers() {
        return boxMembers;
    }

    public ProgressIndicator getProgressIndicatorMembers() {
        return progressIndicatorMembers;
    }

    public Pane getMemberDetailsPane() {
        return memberDetailsPane;
    }

    public ListView getListViewOtherTeams() {
        return listViewOtherTeams;
    }

    public void setTeamSelected(TeamController teamController, boolean isRemoving){

        teamSelected = teamController;
        addMemberImage.setOpacity(1);
        addMemberButton.setVisible(true);

        //if the current user is not the organizer of the selected team so don't make available the + button to add new members
        if(teamSelected.getUsernameOfOrganizer().compareTo(getUsernameOfCurrentUser())!=0)
            hideAddFriendButton();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        progressIndicatorMembers.setVisible(false);
        teamsController = new ArrayList<>();
        teamSelected = null;
        addMemberImage.setOpacity(0.3);
        addMemberButton.setVisible(false);
        listOfFriendsToAdAsMemberPane.setVisible(false);
        friendsList = new ArrayList<>();
        newTeamPane.setVisible(false);
    }

    public void closeMemberDetailsPlane(MouseEvent mouseEvent) {

        memberDetailsPane.setVisible(false);
        listOfFriendsToAdAsMemberPane.setVisible(false);
        frostPane.setEffect(null);
    }

    public ArrayList<String> getUsernameOfMembersOfSelectedTeam(){

        return teamSelected.getUsernameOfMembers();
    }

    public long getIdOfSelectedTeam(){

        return teamSelected.getIdTeam();
    }

    public void updateLabelTeams(){

        labelTeams.setText("teams ("+teamsController.size()+")");
    }

    public void deleteTeamUI(TeamController teamController){

        //if team was selected, deselect it
        if(teamController==teamSelected) {
            //update UI membersBox
            clearBoxMembersUI();
            teamSelected = null;
        }

        //update UI boxTeams
        int indexOfTeam = teamsController.indexOf(teamController);
        teamsController.remove(indexOfTeam);
        boxTeams.getChildren().remove(indexOfTeam);
        labelTeams.setText("teams ("+teamsController.size()+")");
    }

    public void openFriendsList(MouseEvent mouseEvent) {

        //add member to the current selected team


        //open tab
        listOfFriendsToAdAsMemberPane.setVisible(true);
        clearFriendsList();
        frostPane.setEffect(frostEffect);
        //load friends into the tab
        FriendsListLoader friendsListLoader = new FriendsListLoader(this, progressIndicatorListOfFriendsToAdd, boxListOfFriendsToAdd, "friendToAddModel.fxml");
        friendsListLoader.start();
    }

    public void addFriendToTeam(String username, String urlImmagineProfilo){


        try {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXMEMBERS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../memberModel.fxml"));
                final Parent root = (Parent) loader.load();
                MemberController memberController = (MemberController) loader.getController();
                memberController.setTeamViewReferences(this, teamSelected);
                memberController.setUsername(username);
                memberController.setTypeOfMember("member");
                memberController.setProfileImage(urlImmagineProfilo);
                //add the user inside the array of a team
                teamSelected.addMemberController(memberController);
                boxMembers.getChildren().add(root);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void clearBoxMembersUI(){

        boxMembers.getChildren().removeAll();
        boxMembers.getChildren().clear();
    }

    public void addFriendToList(FriendToAddController friendToAddController){

        friendsList.add(friendToAddController);
    }

    public void clearFriendsList(){

        friendsList.clear();
        boxListOfFriendsToAdd.getChildren().clear();
    }

    public void hideAddFriendButton(){
        addMemberImage.setOpacity(0.3);
        addMemberButton.setVisible(false);
    }

    public void addNewFriendToListOfSelectedTeam(String username){

        teamSelected.addNewMemberToTeam(username);
    }

    public void openCreateTeamView(MouseEvent mouseEvent) {
        frostPane.setEffect(frostEffect);
        newTeamPane.setVisible(true);
        createNewTeamButton.setOpacity(0.5);
        areaNameOfNewTeam.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {

                //update title inside circle
                if(areaNameOfNewTeam.getText().length()==0)
                    labelTitleNewTeam.setText("New Team");
                else
                    labelTitleNewTeam.setText(areaNameOfNewTeam.getText());

                if(areaNameOfNewTeam.getText().length()>3)
                    createNewTeamButton.setOpacity(1);
                else
                    createNewTeamButton.setOpacity(0.5);
            }
        });
    }

    public void createNewTeam(MouseEvent mouseEvent) {

        if(areaNameOfNewTeam.getText().length()>3){
            //add into DB
            progressIndicatorNewTeam.setVisible(true);
            try {
                Team newTeam = Insert.createNewTeam(getUsernameOfCurrentUser(), areaNameOfNewTeam.getText());
                progressIndicatorNewTeam.setVisible(false);
                //update UI
                if (newTeam != null) {
                    frostPane.setEffect(null);
                    newTeamPane.setVisible(false);
                    addNewTeamToUI(newTeam);
                    areaNameOfNewTeam.setText("");
                    labelTitleNewTeam.setText("New Team");
                }
            }catch (DiscoveryException | ServiceUnavailableException s){
                destroy();
                showPopUpOfExplorationMode();

            }catch (Exception n) {
                showPopUpMessage("There was a problem. Impossible to add friend");
            }
        }else
            System.out.println("numero minimo di caratteri non rispettato.");
    }

    private void showPopUpMessage(String s) {

        homeController.showPopUpMessage(s);
    }

    private void addNewTeamToUI(Team newTeam) {

        try {
            //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCTEAMS
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "../teamModel.fxml"));
            final Parent root = (Parent) loader.load();
            TeamController teamController = (TeamController) loader.getController();
            teamController.setTeamViewReference(this);
            teamController.setNameOfTeamLabel(newTeam.getName());
            teamController.initializeTeam(newTeam);
            addTeam(teamController);
            teamController.loadInfoTeam();
            boxTeams.getChildren().add(root);
            labelTeams.setText("teams ("+teamsController.size()+")");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void hideFrostEffect(MouseEvent mouseEvent) {

        frostPane.setEffect(null);
        listOfFriendsToAdAsMemberPane.setVisible(false);
        newTeamPane.setVisible(false);
        labelTitleNewTeam.setText("New Team");
        areaNameOfNewTeam.setText("");

    }

    public void setReference(HomeController homeController) {

        this.homeController = homeController;
    }

    public void showPopUpOfExplorationMode() {

        homeController.showPopUpOfExplorationMode();
    }

    public void destroy() {

        homeController.destroyPane();
    }
}
