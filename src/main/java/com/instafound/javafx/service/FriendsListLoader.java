package com.instafound.javafx.service;

import com.instafound.javafx.service.task.FriendsListLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;

public class FriendsListLoader extends Service<Parent> {

@FXML
private ProgressIndicator progressIndicatorFriendsList;
@FXML
private VBox containerFriendsList;
private String pathToFxmlModel;
private Object view;

public FriendsListLoader (Object view,ProgressIndicator progressIndicatorFriendsList, VBox containerFriendsList, String pathToFxmlModel) {

        this.progressIndicatorFriendsList = progressIndicatorFriendsList;
        this.containerFriendsList = containerFriendsList;
        this.pathToFxmlModel = pathToFxmlModel;
        this.view = view;
        }

//this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new FriendsListLoaderTask(view,containerFriendsList, progressIndicatorFriendsList, pathToFxmlModel);
    }
}