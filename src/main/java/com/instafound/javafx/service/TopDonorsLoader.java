package com.instafound.javafx.service;

import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.service.task.TopDonorsLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class TopDonorsLoader extends Service<Parent> {

    HomeController homeController;

    public TopDonorsLoader(HomeController homeController) {

        this.homeController = homeController;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new TopDonorsLoaderTask(homeController);
    }
}
