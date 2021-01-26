package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.PreviewCampaignCreatedController;
import com.instafound.javafx.controller.ProfileController;
import com.instafound.javafx.model.CampaignNeo4J;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.util.ArrayList;

public class CampaignCreatedLoaderTask extends Task<Parent> {

    private ProfileController profileController;

    public CampaignCreatedLoaderTask(ProfileController profileController) {
        this.profileController = profileController;

    }


    @Override
    protected Parent call() throws Exception {

        //here i make the connection with DATABASE
        try{
        profileController.getProgressIndicatorCampaignsCreated().setVisible(true);
        ArrayList<CampaignNeo4J> previewCampaignsCreated = Request.getPreviewInfoOfUserCreatedCampaigns(profileController.getUsernameOfCurrentUser());

        profileController.clearCampaignCreated();
            for(int i=0; i<previewCampaignsCreated.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../campaignCreatedModelPreview.fxml"));
                final Parent root = (Parent) loader.load();
                PreviewCampaignCreatedController campaignCreatedController = (PreviewCampaignCreatedController) loader.getController();
                campaignCreatedController.setTitleCampaign(previewCampaignsCreated.get(i).getTitle());
                campaignCreatedController.setImageCampaign(previewCampaignsCreated.get(i).getUrlImage());
                campaignCreatedController.setReference(profileController);
                campaignCreatedController.setInfo(previewCampaignsCreated.get(i));
                profileController.addCampaignCreated(campaignCreatedController);
                double progress = (i+1)*0.1;
                /*
                We are in another thread, not the main thread. To update UI we need to come back to the main thread.
                Platform.runLater() do this.
                 */
                Platform.runLater(() -> {
                    profileController.getContainerCampaignsCreated().getChildren().add(root);
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
        profileController.getProgressIndicatorCampaignsCreated().setVisible(false);
    }
}