package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Update;
import com.instafound.javafx.model.Team;
import com.instafound.javafx.service.MembersTeamLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

public class TeamController implements Initializable {

    @FXML
    Circle circleImageTeam;
    @FXML
    Label nameOfTeamLabel,labelCountMembersOfTeam,labelNumberOfCampaignsSupported;
    @FXML
    Button buttonDeleteOrLeaveTeam;

    private TeamViewController teamViewController; //the controller of tha main view
    private ArrayList<MemberController> membersController;
    private Team team;


    public static MembersTeamLoader membersTeamLoader;
    public static boolean isLoading = false;

    public void loadInfoTeam(){

        circleImageTeam.setStroke(Color.TRANSPARENT);
        circleImageTeam.setFill(Color.rgb(new Random().nextInt(255),new Random().nextInt(255),new Random().nextInt(255)));
        circleImageTeam.setEffect(new DropShadow(+35d,0d,+6d,Color.LIGHTGRAY));

        nameOfTeamLabel.setText(team.getName());
        labelCountMembersOfTeam.setText("Number of members: "+(team.getUsernameMembers().size()+1));
        labelNumberOfCampaignsSupported.setText("Number of campaigns supported: "+(team.getIdCampaignsSupported().size()));

        //if the current user is the organizer...
        if(getUsernameOfCurrentUser().compareTo(getUsernameOfOrganizer())==0)
            buttonDeleteOrLeaveTeam.setText("remove team");

    }

    public long getIdTeam(){

        return team.getId();
    }

    public void initializeTeam(String nameOfTeam){


    }

    public ArrayList<MemberController> getMembersController() {
        return membersController;
    }

    public String getUsernameOfCurrentUser(){

        return teamViewController.getUsernameOfCurrentUser();
    }

    public ArrayList<String> getUsernameOfMembers(){

        return team.getUsernameMembers();
    }

    public String getUsernameOfOrganizer(){

        return team.getUsernameOrganizer();
    }

    public void clearBoxMembersUI(){

        teamViewController.clearBoxMembersUI();
    }

    public void setNameOfTeamLabel(String name){

        nameOfTeamLabel.setText(name);
    }

    public void addMemberController(MemberController member){

        membersController.add(member);
        //add to UI

    }

    public void addNewMemberToTeam(String username){

        team.addNewMember(username);
        //update UI
        labelCountMembersOfTeam.setText("Number of members: "+(team.getUsernameMembers().size()+1));
    }

    public void removeMemberFromTeam(String username){

        team.removeMember(username);
        //update UI
        labelCountMembersOfTeam.setText("Number of members: "+(team.getUsernameMembers().size()+1));
    }

    public void initializeTeam(Team team){
        this.team = team;
    }

    public void clearMembers(){

        membersController.clear();

    }

    public void removeMemberController(MemberController member){

        int pos = membersController.indexOf(member);
        membersController.remove(member);

        //remove from UI
        teamViewController.removeMemberFromBox(this, pos); //given team (this) and the position of member to check if the team is the one selected
    }

    public void setTeamViewReference(TeamViewController teamViewController){

        this.teamViewController = teamViewController;

    }

    public void showMembersOfTeam(MouseEvent mouseEvent) {

        //if i showMembersOfTeam then it means I clicked on the team, so alert teamViewController
        teamViewController.setTeamSelected(this,false);

        HBox boxMembers = teamViewController.getBoxMembers();
        ProgressIndicator progressIndicatorMembers = teamViewController.getProgressIndicatorMembers();

        teamViewController.clearBoxMembersUI();
        if(membersTeamLoader!=null && membersTeamLoader.isRunning()) {
            membersTeamLoader.cancel();
        }

        membersTeamLoader = new MembersTeamLoader(teamViewController, this);
        if(membersTeamLoader.isRunning()==false && boxMembers.getChildren().size()==0 && isLoading==false) {
            membersTeamLoader.start();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        membersController = new ArrayList<>();
        team = new Team();
    }

    public void deleteOrLeaveTeam(MouseEvent mouseEvent) {


        //if i'm the organizer, so delete teams and all realtionships
        if(getUsernameOfOrganizer().compareTo(teamViewController.getUsernameOfCurrentUser())==0){

            try {
                boolean outcome = Update.removeTeam(team.getId());
                if (outcome == true)
                    teamViewController.deleteTeamUI(this);
            }catch (DiscoveryException | ServiceUnavailableException s){
                teamViewController.destroy();
                teamViewController.showPopUpOfExplorationMode();

            }catch (Exception n) {
                teamViewController.showPopUp("There was a problem. Impossible to remove team");
            }

        }else { //if i'm not the organizer just leave team

                MemberController me = null;
                for(int i=0; i<membersController.size(); i++) {
                    if (membersController.get(i).getUsername().compareTo(getUsernameOfCurrentUser()) == 0) {
                        me = membersController.get(i);
                    }

            }
            //remove from database (blocking UI)
            try {
                boolean outcome = Update.removeMemberFromTeam(getUsernameOfCurrentUser(), getIdTeam());

                //since the username is unique we can scroll all the boxMembers until we find a child whose username is the same of this
                if (outcome == true) {
                    removeMemberController(me);
                    removeMemberFromTeam(getUsernameOfCurrentUser());
                    //update UI
                    teamViewController.deleteTeamUI(this);
                }
            }catch (DiscoveryException | ServiceUnavailableException s){
                teamViewController.destroy();
                teamViewController.showPopUpOfExplorationMode();

            }catch (Exception n) {
                teamViewController.showPopUp("There was a problem. Impossible to leave team.");
            }

        }
    }
}
