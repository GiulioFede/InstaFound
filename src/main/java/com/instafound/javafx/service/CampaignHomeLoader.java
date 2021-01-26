package com.instafound.javafx.service;

import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.service.task.CampaignHomeLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class CampaignHomeLoader extends Service<Parent> {

    private HomeController homeController;
    private String category;
    private int n, s;

    public CampaignHomeLoader (HomeController homeController,String category, int n, int s) {

        this.homeController = homeController;
        this.category = category;
        this.n= n;
        this.s = s;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {

        return new CampaignHomeLoaderTask(homeController, category, n, s);
    }
}