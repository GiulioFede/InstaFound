package com.instafound.javafx.controller;

import com.instafound.javafx.service.CampaignHomeLoader;
import javafx.scene.input.MouseEvent;

public class LoadMoreButton {

    private HomeController homeController;

    public void setHomeController(HomeController homeController){
        this.homeController = homeController;
    }

    public void loadMoreCampaigns(MouseEvent mouseEvent) {

        //remove button 'loadMore'
        homeController.removeLoadMoreButton();

        //load other n campaigns skipping the current
        CampaignHomeLoader campaignHomeLoader = new CampaignHomeLoader(homeController,homeController.getCurrentCategoryDisplayed(),10, homeController.getNumberOfCampaignDisplayed());
        campaignHomeLoader.start();
    }
}
