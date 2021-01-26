package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.MemberController;
import com.instafound.javafx.controller.TeamController;
import com.instafound.javafx.controller.TeamViewController;
import com.instafound.javafx.model.UserInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;

public class MembersTeamLoaderTask extends Task<Parent> {

    private TeamViewController teamViewController;
    private TeamController teamController;

    public MembersTeamLoaderTask(TeamViewController teamViewController, TeamController teamController) {

        this.teamViewController = teamViewController;
        this.teamController = teamController;

    }

    @Override
    protected Parent call() throws Exception {

        HBox containerMembersTeam = teamViewController.getBoxMembers();
        ProgressIndicator progressIndicatorMembersTeam = teamViewController.getProgressIndicatorMembers();

        //clear team list
        teamController.clearMembers();

        progressIndicatorMembersTeam.setVisible(true);

        //here i make the connection with DATABASE
        //retrieve all the information about the members
        ArrayList<String> membersUsername = new ArrayList<>(teamController.getUsernameOfMembers());
        //add organizer
        membersUsername.add(0,teamController.getUsernameOfOrganizer());
        ArrayList<UserInfo> membersInfo = new ArrayList<>();
        for(int i=0; i<membersUsername.size(); i++){
            UserInfo memberInfo = null;
            try {
                memberInfo = Request.getUserInfo(membersUsername.get(i));
            }catch (DiscoveryException | ServiceUnavailableException s){

                Platform.runLater(() -> {
                    //pop up to warn of the possibility of browsing in exploration mode
                    teamViewController.destroy();
                    teamViewController.showPopUpOfExplorationMode();
                });
            }catch (Exception n) {
                Platform.runLater(() -> {
                    teamViewController.showPopUp("There was a problem. Please try later.");
                });
            }
            membersInfo.add(memberInfo);
            Thread.sleep(5);
        }

        try {
            for(int i=0; i<membersInfo.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../memberModel.fxml"));
                final Parent root = (Parent) loader.load();
                MemberController memberController = (MemberController) loader.getController();
                memberController.setTeamViewReferences(teamViewController, teamController);
                memberController.setUsername(membersInfo.get(i).getUsername());
                memberController.setProfileImage(membersInfo.get(i).getUrlProfileImage());
                if(i==0)
                    memberController.setTypeOfMember("organizer");
                else
                    memberController.setTypeOfMember("member");

                double progress = (i+1)*0.05;
                //add the user inside the array of a team
                teamController.addMemberController(memberController);
                final int j = i;
                Platform.runLater(() -> {
                    /*
                    Platform.runLater() is execute "after" in a unknown time. So if the users click two different team shape the task is closed BUT
                    this doesn't mean that also this statement will no longer run in the future, becouse when it starts it's completely separated from
                    this thread. So can appen that the old thread is closed but his last Platform.runLater is waiting scheduling to be processed even if
                    the thread that has generated it is closed, so with a new thread two Platoform.runLater can be overlapped causing two insertion of
                    same nodes. The statement below is necessary to avoid this.
                     */
                    if(j == containerMembersTeam.getChildren().size())
                         containerMembersTeam.getChildren().add(root);
                });
                Thread.sleep(5);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        teamViewController.getProgressIndicatorMembers().setVisible(false);
    }
}
