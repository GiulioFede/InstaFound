package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;


public class SuggestedFriendController {

    @FXML
    private Circle profileImageSuggestedFriend;
    @FXML
    private Label usernameSuggestedFriend,locationSuggestedFriend,labelInCommon;
    @FXML
    private Label buttonAddSuggestedFriend;
    private ProfileController profileController;


    public void setProfileImage(String url){

        profileImageSuggestedFriend.setStroke(Color.TRANSPARENT);
        Image profileImage;
        try {
            profileImage = new Image(url);
        }catch (NullPointerException n){
            profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
        }catch(IllegalArgumentException e){
            profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
        }
        profileImageSuggestedFriend.setFill(new ImagePattern(profileImage));
        profileImageSuggestedFriend.setEffect(new DropShadow(+25d,0d,+2d,Color.LIGHTGRAY));
    }

    public void setUsernameSuggestedFriend(String username){

        this.usernameSuggestedFriend.setText(username);
    }

    public void setLocationSuggestedFriend(String location){

        this.locationSuggestedFriend.setText(location);
    }

    public void setFavoriteCategoryInCommon(int q){

        this.labelInCommon.setText(String.valueOf(q)+" favorite categories in common");
    }


    public void addSuggestedFriend(MouseEvent mouseEvent) {

        try {
            boolean outcome = Insert.addNewFriend(profileController.getUsernameOfCurrentUser(), usernameSuggestedFriend.getText());
            if (outcome == true)
                buttonAddSuggestedFriend.setVisible(false);
        }catch (DiscoveryException | ServiceUnavailableException s){
            profileController.destroy();
            profileController.showPopUpOfExplorationMode();

        }catch (Exception n) {
            profileController.showPopUpMessage("There was a problem. Impossible to add friend.");
        }
    }

    public void setReference(ProfileController profileController) {

        this.profileController = profileController;
    }
}
