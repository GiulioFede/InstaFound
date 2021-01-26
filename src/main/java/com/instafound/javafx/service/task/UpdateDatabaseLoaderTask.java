package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.SynchDatabaseManager;
import com.instafound.javafx.controller.HomeController;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class UpdateDatabaseLoaderTask extends Task<Parent> {

    private HomeController homeController;
    private String file_path;

    public UpdateDatabaseLoaderTask(HomeController homeController, String file_path) { //n: how many campaigns, s: skip

        this.homeController = homeController;
        this.file_path = file_path;

    }

    @Override
    protected Parent call() throws Exception {

        homeController.showUpdateWindow();

        //all the exception are handled internally
        SynchDatabaseManager synchDatabaseManager = new SynchDatabaseManager(homeController);
        synchDatabaseManager.setSourceFilePath(file_path);
        synchDatabaseManager.insertDataFromExcelToDatabases();

        return null;
    }

    @Override
    protected void failed() {
        super.failed();
        homeController.hideUpdateWindow();
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        homeController.hideUpdateWindow();
    }
}
