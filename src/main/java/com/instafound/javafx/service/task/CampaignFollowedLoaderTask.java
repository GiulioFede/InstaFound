package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.PreviewCampaignFollowedController;
import com.instafound.javafx.controller.ProfileController;
import com.instafound.javafx.model.CampaignNeo4J;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import java.util.ArrayList;

//the relative task
public class CampaignFollowedLoaderTask extends Task<Parent> {

    private ProfileController profileController;

    public CampaignFollowedLoaderTask(ProfileController profileController) {

        this.profileController = profileController;
    }


    @Override
    protected Parent call() throws Exception {

        //here i make the connection with DATABASE
        try{
        profileController.getProgressIndicatorCampaignsFollowed().setVisible(true);
        ArrayList<CampaignNeo4J> previewCampaignFollowed = Request.getPreviewInfoOfUserFollowedCampaigns(profileController.getUsernameOfCurrentUser());

            for(int i=0; i<previewCampaignFollowed.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../campaignFollowedModelPreview.fxml"));
                final Parent root = (Parent) loader.load();
                PreviewCampaignFollowedController campaignFollowedController = (PreviewCampaignFollowedController) loader.getController();
                campaignFollowedController.setTitleCampaign(previewCampaignFollowed.get(i).getTitle());
                campaignFollowedController.setImageCampaign(previewCampaignFollowed.get(i).getUrlImage());
                campaignFollowedController.setReference(profileController);
                campaignFollowedController.setInfo(previewCampaignFollowed.get(i));
                profileController.addFollowedCampaign(campaignFollowedController);

                Platform.runLater(() -> {
                    profileController.getContainerCampaignsFollowed().getChildren().add(root);
                });
                Thread.sleep(5);
            }
        }catch (DiscoveryException | ServiceUnavailableException s){

            Platform.runLater(() -> {
                //pop up to warn of the possibility of browsing in exploration mode
                profileController.destroy();
                profileController.showPopUpOfExplorationMode();
            });
        }catch (Exception n) {
            Platform.runLater(() -> {
                profileController.showPopUpMessage("There was a problem. Please try later.");
            });
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        profileController.getProgressIndicatorCampaignsFollowed().setVisible(false);
    }
}

