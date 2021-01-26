package com.instafound.javafx.controller;

import com.instafound.javafx.model.CampaignMongoDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class PreviewCampaignController implements Initializable {

    @FXML
    Label labelTitleCampaignPreview,labelOrganizerPreview,labelCategoryPreview,labelNumberOfDonationsPreview,labelTotalRequiredPreview,labelTotalAchivedPreview;
    @FXML
    ImageView imageViewCampaignPreview;
    @FXML
    ProgressBar progressBarAchivedPreview;

    private HomeController homeController;
    private CampaignMongoDB campaignInfo;


    public void setReference(HomeController homeController){

        this.homeController = homeController;
    }

    public void loadPreview(){

        labelTitleCampaignPreview.setText(campaignInfo.getTitle());

        labelOrganizerPreview.setText(campaignInfo.getOrganizer());

        labelCategoryPreview.setText(campaignInfo.getCategory());

        labelNumberOfDonationsPreview.setText(" "+String.valueOf(campaignInfo.getCountDonations())+" DONATIONS");

        labelTotalRequiredPreview.setText("Total required: "+String.valueOf(campaignInfo.getAmountRequired()));

        labelTotalAchivedPreview.setText("Total achived: "+String.valueOf(campaignInfo.getAmountAchived()));

        double progress = (campaignInfo.getAmountAchived())/(campaignInfo.getAmountRequired());
        progress = (progress>1) ? 1 : progress;
        progressBarAchivedPreview.setProgress(progress);

        try {
            Image image = new Image(campaignInfo.getUrlImage());
            imageViewCampaignPreview.setImage(image);
        }catch (NullPointerException | IllegalArgumentException e){
            Image image = new Image("https://www.debonisarredo.it/site/images/joomlart/demo/default.jpg");
            imageViewCampaignPreview.setImage(image);

        }
    }

    public void setCampaignInfo(CampaignMongoDB campaignInfo){

        this.campaignInfo = campaignInfo;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void openCampaignDetails(MouseEvent mouseEvent) {

        homeController.openCampaignDetailsView(campaignInfo);
    }
}
