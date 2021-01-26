package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class FriendToAddController {

    @FXML
    Label labelFriendUsername;
    @FXML
    Circle circleImageFriend;
    @FXML
    Button addFriendToTeamButton;

    private TeamViewController teamViewController;
    private String urlProfileImage;

    public void setUsername(String username){

        labelFriendUsername.setText(username);
    }

    public String getUsername(){

        return labelFriendUsername.getText();
    }

    public String getUrlProfileImage(){

        return urlProfileImage;
    }

    public void setReference(TeamViewController teamViewController){
        this.teamViewController = teamViewController;
    }

    public void setProfileImage(String url){

        urlProfileImage = url;

        Image profileImage;
        if(url==null || url.compareTo("null")==0) {
            //set default image
            profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg"); //TODO: get directly to directory images to perform
            circleImageFriend.setFill(new ImagePattern(profileImage));
            circleImageFriend.setStroke(Color.LIGHTGRAY);
        }
        else {
            Exception e = new Image(url).getException(); //default

            if(e==null)
                profileImage=new Image(url);
            else
                profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
            circleImageFriend.setFill(new ImagePattern(profileImage));
            circleImageFriend.setStroke(javafx.scene.paint.Color.TRANSPARENT);
        }

    }

    public void addFriendAsMemberOfTeam(MouseEvent mouseEvent) {

        //add to database (blocking UI ok)
        teamViewController.enableIndicatorForListOfAddableFriends();
        try {
            boolean outcome = Insert.insertNewMemberToTheTeam(getUsername(), teamViewController.getIdOfSelectedTeam());
            teamViewController.disenableIndicatorForListOfAddableFriends();
            //add UI
            if (outcome == true) {
                teamViewController.addFriendToTeam(getUsername(), getUrlProfileImage());
                //add to team
                teamViewController.addNewFriendToListOfSelectedTeam(getUsername());
                //show as added
                addFriendToTeamButton.setText("added");
                addFriendToTeamButton.setOnMouseClicked(null);
                addFriendToTeamButton.setStyle("-fx-background-color: #04E800;\n" +
                        "    -fx-background-radius: 22;\n" +
                        "    -fx-text-fill: white;\n" +
                        "    -fx-font-size: 18;");
                addFriendToTeamButton.setBackground(new Background(new BackgroundFill(
                        Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }catch (DiscoveryException | ServiceUnavailableException s){

            //pop up to warn of the possibility of browsing in exploration mode
            teamViewController.destroy();
            teamViewController.showPopUpOfExplorationMode();

        }catch (Exception n){
            teamViewController.showPopUp("There was a problem. The new member could not be added to the team. Please try later.");
        }


    }

}
