package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Update;
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

public class FriendController {

    @FXML
    Label labelFriendUsername,labelLocationFriend;
    @FXML
    Circle circleImageFriend;

    private FriendsListController friendsListController;

    public void setUsername(String username){

       labelFriendUsername.setText(username);
    }

    public void setLocation(String location){
        labelLocationFriend.setText(location);
    }

    public void setReference(FriendsListController friendsListController){

        this.friendsListController = friendsListController;
    }

    public void setProfileImage(String url){ //TODO: MODIFICARE, per adesso è sempre di default (tranne per quelle settate da me stesso). Nel prossimo scraping si aggiusta tutto e bisogna riportare l'if else che fa un controllo del tipo "se l'url è nullo allora metti quella di default come immagine" (invece per adesso è strano)

        Image profileImage;
        if(url==null || url.compareTo("null")==0) {
            //set default image
            profileImage = new Image("https://icon-library.com/images/anonymous-avatar-icon/anonymous-avatar-icon-25.jpg");
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

        circleImageFriend.setEffect(new DropShadow(+25d,0d,+2d, Color.LIGHTGRAY));
    }

    public void deleteFriend(MouseEvent mouseEvent) {

        //block UI, no problem
        try {
            boolean successfullyDeleted = Update.removeFriend(friendsListController.getUsernameOfUser(), labelFriendUsername.getText());

            //update UI
            if (successfullyDeleted == true)
                friendsListController.removeFriendFromUI(this);
        }catch (DiscoveryException | ServiceUnavailableException s){
            friendsListController.destroy();
            friendsListController.showPopUpOfExplorationMode();

        }catch (Exception n) {
            friendsListController.showPopUpMessage("There was a problem. Impossible to remove friend");
        }
        
    }

}
