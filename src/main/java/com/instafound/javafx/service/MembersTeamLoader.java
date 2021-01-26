package com.instafound.javafx.service;

import com.instafound.javafx.controller.TeamController;
import com.instafound.javafx.controller.TeamViewController;
import com.instafound.javafx.service.task.MembersTeamLoaderTask;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;

public class MembersTeamLoader extends Service<Parent> {

    private TeamViewController teamViewController;
    private TeamController teamController;

    public MembersTeamLoader(TeamViewController teamViewController, TeamController teamController) {

        this.teamViewController = teamViewController;
        this.teamController = teamController;
    }

    //this service is used to get data about the campaign that the user created

    /*
    This method is executed on a background thread. The rest of the other codes in this application is executed on the main (no background) thread.
     */
    @Override
    protected Task<Parent> createTask() {
        return new MembersTeamLoaderTask(teamViewController, teamController);
    }
}