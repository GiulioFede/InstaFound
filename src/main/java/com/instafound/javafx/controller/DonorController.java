package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class DonorController {

    @FXML
    Label labelUsername;
    @FXML
    Pane areaAddFriend;
    @FXML
    Label labelAmountDonated;
    @FXML
    Label timestampOfDonationLabel;

    private String usernameOfCurrentUser;
    private CampaignDetailsController campaignDetailsController;

    public void setUsername(String username){

        labelUsername.setText(username);
    }

    public void setAmountDonated(double amountDonated) {

        labelAmountDonated.setText("has donated "+String.valueOf(amountDonated)+"€");
    }

    public void setCurrentUserUsername(String username){

        this.usernameOfCurrentUser = username;
        if(labelUsername.getText().compareTo(username)==0)
            hideAddFriendButton();
    }

    public void hideAddFriendButton(){

        areaAddFriend.setVisible(false);
    }

    public void setTimestampOfDonationLabel(String timestamp){

        this.timestampOfDonationLabel.setText(timestamp);
    }


    public void addFriend(MouseEvent mouseEvent) {

        //create friend relationship

        try {
            boolean outcome = Insert.addNewFriend(usernameOfCurrentUser, labelUsername.getText().replace(" ", "")); //TODO: il replace è da togliere quando su MongoDB si inseriranno gli username senza spazio
            if (outcome == true)
                areaAddFriend.setVisible(false);
        }catch (DiscoveryException | ServiceUnavailableException s){
            campaignDetailsController.destroy();
            campaignDetailsController.showPopUpOfExplorationMode();

        }catch (Exception n) {
            campaignDetailsController.showPopUpMessage("There was a problem. Impossible to add friend");
        }


    }

    public void setReference(CampaignDetailsController campaignDetailsController) {

        this.campaignDetailsController = campaignDetailsController;

    }
}