package com.instafound.javafx.controller;

import com.instafound.javafx.model.CampaignNeo4J;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.bson.types.ObjectId;

public class PreviewCampaignCreatedController {

    @FXML
    Label titleCampaignPreview;
    @FXML
    ImageView imageViewCampaignCreatedPreview;

    private CampaignNeo4J campaignInfo; //contains all the info of the entire campaign

    private ProfileController profileController;

    public void setTitleCampaign(String title){

        titleCampaignPreview.setText(title);
    }

    public ObjectId getObjectId(){

        return campaignInfo.getId();
    }

    public String getTitle(){

        return campaignInfo.getTitle();
    }

    public void setInfo(CampaignNeo4J campaignMongoDB){

        this.campaignInfo = campaignMongoDB;
    }

    public void setImageCampaign(String url){

        try {
            Image image = new Image(url);
            imageViewCampaignCreatedPreview.setImage(image);
        }catch (NullPointerException | IllegalArgumentException e){
            Image image = new Image("https://www.debonisarredo.it/site/images/joomlart/demo/default.jpg");
            imageViewCampaignCreatedPreview.setImage(image);

        }

    }

    public void setReference(ProfileController profileController){

        this.profileController = profileController;
    }

    public void openStatisticsCampaign(MouseEvent mouseEvent) {

        //frost background
        profileController.openStaticsPane(this);
    }

    public void openCampaignDetails(MouseEvent mouseEvent) {

        profileController.openDetailsCampaign(campaignInfo.getId());

    }
}
