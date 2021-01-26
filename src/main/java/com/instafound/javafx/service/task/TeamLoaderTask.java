package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.TeamController;
import com.instafound.javafx.controller.TeamViewController;
import com.instafound.javafx.model.Team;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;

public class TeamLoaderTask extends Task<Parent> {

    @FXML
    private HBox containerTeam;
    @FXML
    private HBox boxMembers;
    @FXML
    private ProgressIndicator progressIndicatorTeam;
    @FXML
    private ProgressIndicator progressIndicatorMembers;
    @FXML
    TeamViewController teamViewController;

    public TeamLoaderTask(TeamViewController teamViewController, HBox containerTeams, ProgressIndicator progressIndicatorTeams, ProgressIndicator progressIndicatorMembers, HBox boxMembers) {

        this.containerTeam = containerTeams;
        this.boxMembers = boxMembers;
        this.progressIndicatorTeam = progressIndicatorTeams;
        this.progressIndicatorMembers = progressIndicatorMembers;
        this.teamViewController = teamViewController;

    }

    @Override
    protected Parent call() throws Exception {

        //here i make the connection with DATABASE
        try{
        ArrayList<Team> teams = Request.getUserTeamsDetails(teamViewController.getUsernameOfCurrentUser());
        Thread.sleep(5);
        //...(simulate)
        progressIndicatorTeam.setVisible(true);
        teamViewController.clearTeam();

            for(int i=0; i<teams.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXTEAMS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../teamModel.fxml"));
                final Parent root = (Parent) loader.load();
                TeamController teamController = (TeamController) loader.getController();
                teamController.setTeamViewReference(teamViewController);
                teamController.initializeTeam(teams.get(i));
                teamController.loadInfoTeam();
                teamViewController.addTeam(teamController);
                Platform.runLater(() -> {
                    containerTeam.getChildren().add(root);
                });
                Thread.sleep(5);
            }
        }catch (DiscoveryException | ServiceUnavailableException s){

            Platform.runLater(() -> {
                //pop up to warn of the possibility of browsing in exploration mode
                teamViewController.destroy();
                teamViewController.showPopUpOfExplorationMode();
            });
        }catch (Neo4jException n) {
            Platform.runLater(() -> {
                teamViewController.showPopUp("There was a problem. Please try later.");
            });
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        progressIndicatorTeam.setVisible(false);
        teamViewController.updateLabelTeams();
    }
}
