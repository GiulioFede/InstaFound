package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.controller.LoadMoreButton;
import com.instafound.javafx.controller.PreviewCampaignController;
import com.instafound.javafx.model.CampaignMongoDB;
import com.mongodb.MongoTimeoutException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;
import java.util.ArrayList;

public class CampaignHomeLoaderTask extends Task<Parent> {

    private HomeController homeController;
    private String category;
    private int n,s;

    public CampaignHomeLoaderTask(HomeController homeController, String category, int n, int s) { //n: how many campaigns, s: skip

        this.homeController = homeController;
        this.category = category;
        this.n =n;
        this.s =s;

    }

    @Override
    protected Parent call() throws Exception {

        homeController.setVisibilityProgressIndicator(true);
        ArrayList<CampaignMongoDB> lastCampaigns = new ArrayList<>();

        //here i make the connection with DATABASE
        try {
            switch (category) {
                case "Medical Expenses":
                    lastCampaigns = Request.getLastCampaigns(n,s, "Medical Expenses");
                    break;
                case "Environment":
                    lastCampaigns = Request.getLastCampaigns(n,s, "Environment");
                    break;
                case "No-Profit":
                    lastCampaigns = Request.getLastCampaigns(n,s, "No Profit");
                    break;
                case "Creativity":
                    lastCampaigns = Request.getLastCampaigns(n,s, "Creativity");
                    break;
                case "Animals":
                    lastCampaigns = Request.getLastCampaigns(n,s, "Animals");
                    break;
                default:
                    lastCampaigns = Request.getLastCampaigns(n,s, "null");
                    break;
            }
        }catch(MongoTimeoutException me){
            Platform.runLater(() -> {
                homeController.showPopUpMessage("the server takes too long to respond.");
            });
        }catch (Exception e){
            Platform.runLater(() -> {
                homeController.showPopUpMessage("An error occured. Please try later.");
            });
        }

        try {
            for(int i=0; i<lastCampaigns.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../campaignModel.fxml"));
                final Parent root = (Parent) loader.load();

                PreviewCampaignController previewCampaignController = (PreviewCampaignController) loader.getController();
                previewCampaignController.setReference(homeController);
                previewCampaignController.setCampaignInfo(lastCampaigns.get(i));
                previewCampaignController.loadPreview();
                Platform.runLater(() -> {
                    homeController.addCampaignToHomeUI(root);
                    //update Number of cammpaign
                    homeController.incrementNumberOfCampaignDisplayed();
                });
                Thread.sleep(5);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        homeController.setVisibilityProgressIndicator(false);

        //load button
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "../../loadMoreButton.fxml"));
        try {
            final Parent root = (Parent) loader.load();
            LoadMoreButton loadMoreButton = loader.getController();
            loadMoreButton.setHomeController(homeController);
            homeController.addCampaignToHomeUI(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
