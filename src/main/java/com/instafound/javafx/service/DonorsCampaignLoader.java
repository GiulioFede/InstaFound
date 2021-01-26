package com.instafound.javafx.service;

import com.instafound.javafx.controller.CampaignDetailsController;
import com.instafound.javafx.service.task.DonorsCampaignLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class DonorsCampaignLoader extends Service<Parent> {

    CampaignDetailsController campaignDetailsController;

    public DonorsCampaignLoader(CampaignDetailsController campaignDetailsController) {

        this.campaignDetailsController = campaignDetailsController;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new DonorsCampaignLoaderTask(campaignDetailsController);
    }
}
