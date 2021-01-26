package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Update;
import com.instafound.javafx.model.CampaignNeo4J;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class PreviewCampaignFollowedController {

    @FXML
    Label titleCampaignFollowedPreview,followLabel;
    @FXML
    ImageView imageViewCampaignFollowed;

    private ProfileController profileController;
    private CampaignNeo4J campaignInfo;

    public void setTitleCampaign(String title){

        titleCampaignFollowedPreview.setText(title);
    }

    public void setImageCampaign(String url){
        try {
            imageViewCampaignFollowed.setImage(new Image(url));
        }catch (NullPointerException | IllegalArgumentException e) {
            Image image = new Image("https://www.debonisarredo.it/site/images/joomlart/demo/default.jpg");
            imageViewCampaignFollowed.setImage(image);
        }
    }

    public void openCampaignDetails(MouseEvent mouseEvent) {

        profileController.openDetailsCampaign(campaignInfo.getId());
    }

    public void showUnfollowLabel(MouseEvent mouseEvent) {

        followLabel.setText("unfollow");
    }

    public void showFollowedLabel(MouseEvent mouseEvent) {

        followLabel.setText("followed");
    }

    public void setReference(ProfileController profileController) {

        this.profileController = profileController;
    }

    public void setInfo(CampaignNeo4J campaignNeo4J) {

        this.campaignInfo = campaignNeo4J;
    }

    public void unFollowCampaign(MouseEvent mouseEvent) {

        //unfollow current campaign
        try {
            boolean outcome = Update.unfollowCampaign(profileController.getUsernameOfCurrentUser(), campaignInfo.getId().toHexString());
            if (outcome == true)
                profileController.removeFollowedCampaignFromUI(this);
        }catch (DiscoveryException | ServiceUnavailableException s){
            profileController.destroy();
            profileController.showPopUpOfExplorationMode();

        }catch (Exception n) {
            profileController.showPopUpMessage("There was a problem. Impossible to unfollow a campaign.");
        }
    }
}
