package com.instafound.javafx.service;

import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.service.task.UpdateDatabaseLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class UpdateDatabaseLoader extends Service<Parent> {

    private HomeController homeController;
    private String file_path;

    public UpdateDatabaseLoader(HomeController homeController, String file_path) {

        this.homeController = homeController;
        this.file_path = file_path;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new UpdateDatabaseLoaderTask(homeController, file_path);
    }
}
