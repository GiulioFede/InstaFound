package com.instafound.javafx.service;

import com.instafound.javafx.controller.TeamViewController;
import com.instafound.javafx.service.task.TeamLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;

public class TeamLoader extends Service<Parent> {

    @FXML
    private ProgressIndicator progressIndicatorTeam;
    @FXML
    private ProgressIndicator progressIndicatorMembers;
    @FXML
    private HBox containerTeam;
    @FXML
    private HBox boxMembers;
    @FXML
    TeamViewController teamViewController;

    public TeamLoader(TeamViewController teamViewController, ProgressIndicator progressIndicatorTeam, ProgressIndicator progressIndicatorMembers, HBox containerTeam, HBox boxMembers) {

        this.progressIndicatorTeam = progressIndicatorTeam;
        this.progressIndicatorMembers = progressIndicatorMembers;
        this.containerTeam = containerTeam;
        this.boxMembers = boxMembers;
        this.teamViewController = teamViewController;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new TeamLoaderTask(teamViewController,containerTeam, progressIndicatorTeam, progressIndicatorMembers, boxMembers);
    }
}
