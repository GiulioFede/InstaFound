package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.backend.queries.Update;
import com.instafound.javafx.model.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.util.ArrayList;

public class MemberController {

    @FXML
    Circle circleImageMember;
    @FXML
    Label memberUsernameLabel,typeOfMember,deleteMemberButton;

    TeamViewController teamViewController;
    TeamController teamController;


    private static final Effect frostEffect = new BoxBlur(100,100,3);

    public void setUsername(String username){

        memberUsernameLabel.setText(username);
    }

    public String getUsername(){

        return memberUsernameLabel.getText();
    }

    public void setTypeOfMember(String type){

        typeOfMember.setText(type);

        //if the member is not the organizer allow him to be removed
        if(teamController.getUsernameOfCurrentUser().compareTo(memberUsernameLabel.getText())==0) //if is the current user
            deleteMemberButton.setVisible(false);
        if(teamController.getUsernameOfCurrentUser().compareTo(teamController.getUsernameOfOrganizer())!=0) //if the current user is not the organizer
            deleteMemberButton.setVisible(false);
    }


    public void setProfileImage(String url){


        Image profileImage;
        if(url==null || url.compareTo("null")==0) {
            //set default image
            profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
            circleImageMember.setFill(new ImagePattern(profileImage));
            circleImageMember.setStroke(Color.LIGHTGRAY);
        }
        else {
            Exception e = new Image(url).getException(); //default

            if(e==null)
                profileImage=new Image(url);
            else
                profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");

            circleImageMember.setFill(new ImagePattern(profileImage));
            circleImageMember.setStroke(javafx.scene.paint.Color.TRANSPARENT);

        }

        circleImageMember.setEffect(new DropShadow(+25d,0d,+2d, Color.LIGHTGRAY));
    }

    public void setTeamViewReferences(TeamViewController teamViewController, TeamController teamController){

        this.teamViewController = teamViewController;
        this.teamController = teamController;
    }


    public void openInfoMember(MouseEvent mouseEvent) { //TODO: DA ELIMINARE E FARLO SENSATO (QUI E' SOLO A SCOPO DI DEBUGGING)

        teamViewController.getMemberDetailsPane().setVisible(true);
        teamViewController.getFrostPane().setEffect(frostEffect);
        teamViewController.getListViewOtherTeams().getItems().clear();

        //get team's details of the user
        ArrayList<Team> teamsOfUser = Request.getUserTeamsDetails(getUsername());

        //extract all the teams and campaigns he belongs/supports
        ObservableList<String> nameOfTeams = FXCollections.observableArrayList();

        for(Team team: teamsOfUser){
            nameOfTeams.add("- "+team.getName()+" -  has supported "+team.getIdCampaignsSupported().size()+" campaigns");
        }

        //fill listViews of details
        teamViewController.getListViewOtherTeams().setItems(nameOfTeams);

    }


    public void removeMemberFromTeam(MouseEvent mouseEvent) {

        //remove from database (blocking UI)
        try {
            boolean outcome = Update.removeMemberFromTeam(getUsername(), teamController.getIdTeam());

            //since the username is unique we can scroll all the boxMembers until we find a child whose username is the same of this
            if (outcome == true) {
                teamController.removeMemberController(this);
                teamController.removeMemberFromTeam(getUsername());
            }
        }catch (DiscoveryException | ServiceUnavailableException s){
            teamViewController.destroy();
            teamViewController.showPopUpOfExplorationMode();

        }catch (Exception n) {
            teamViewController.showPopUp("There was a problem. Impossible to remove member.");
        }

    }

}
