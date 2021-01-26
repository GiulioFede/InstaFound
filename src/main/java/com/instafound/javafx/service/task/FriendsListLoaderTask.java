package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.*;
import com.instafound.javafx.model.UserInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;

public class FriendsListLoaderTask extends Task<Parent> {

    @FXML
    private VBox containerFriendsList;
    @FXML
    private ProgressIndicator progressIndicatorFriendsList;
    private String pathToFxmlModel;
    private Object view;

    public FriendsListLoaderTask(Object view, VBox containerFriendsList, ProgressIndicator progressIndicatorFriendsList, String pathToFxmlModel) {

        this.containerFriendsList = containerFriendsList;
        this.progressIndicatorFriendsList = progressIndicatorFriendsList;
        this.pathToFxmlModel = pathToFxmlModel;
        this.view = view;

    }

    @Override
    protected Parent call() throws Exception {


        //IF I WANT THE LIST OF FRIEND TO SHOW IN THE TEAM VIEW..._________________________________
        if(pathToFxmlModel.compareTo("friendToAddModel.fxml")==0){

            TeamViewController teamViewController =(TeamViewController)view;
        try {
            //get all the user's friend
            ArrayList<UserInfo> friendsInfo = Request.getUserFriends(teamViewController.getUsernameOfCurrentUser());

            //get the list of members in the selected team
            ArrayList<String> membersOfSelectedTeam = teamViewController.getUsernameOfMembersOfSelectedTeam();

            //remove from friend's list the members of selected team
            for (int i = 0; i < membersOfSelectedTeam.size(); i++) {
                String usernameOfMember = membersOfSelectedTeam.get(i);
                int k = 0;
                boolean found = false;
                for (k = 0; k < friendsInfo.size(); k++) {
                    if (friendsInfo.get(k).getUsername().compareTo(usernameOfMember) == 0) {
                        found = true;
                        break;
                    }
                }

                if (found == true) {
                    friendsInfo.remove(k);
                }
            }

            progressIndicatorFriendsList.setVisible(true);

            try {
                for (int i = 0; i < friendsInfo.size(); i++) {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "../../" + pathToFxmlModel));
                    final Parent root = (Parent) loader.load();
                    FriendToAddController friendController = (FriendToAddController) loader.getController();
                    friendController.setUsername(friendsInfo.get(i).getUsername());
                    friendController.setProfileImage(friendsInfo.get(i).getUrlProfileImage());
                    friendController.setReference(teamViewController);
                    double progress = (i + 1) * 0.05;
                    teamViewController.addFriendToList(friendController);
                    Platform.runLater(() -> {
                        containerFriendsList.getChildren().add(root);
                    });
                    Thread.sleep(5);
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } catch (DiscoveryException | ServiceUnavailableException s){

            Platform.runLater(() -> {
                //pop up to warn of the possibility of browsing in exploration mode
                teamViewController.destroy();
                teamViewController.showPopUpOfExplorationMode();
            });
        }catch (Neo4jException n) {
            Platform.runLater(() -> {
                teamViewController.showPopUp("There was a problem. Impossible to load friends.");
            });
        }


        }else //___________________________________________________________________________________
            {
            //here i make the connection with DATABASE
            progressIndicatorFriendsList.setVisible(true);
            FriendsListController friendsListController = (FriendsListController)view;
            String usernameUser = friendsListController.getUsernameOfUser();
            try{
            ArrayList<UserInfo> friends = Request.getUserFriends(usernameUser);
            Thread.sleep(5);

                for (int i = 0; i < friends.size(); i++) {
                    //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "../../" + pathToFxmlModel));
                    final Parent root = (Parent) loader.load();
                    FriendController friendController = (FriendController) loader.getController();
                    friendController.setReference(friendsListController);
                    friendController.setUsername(friends.get(i).getUsername());
                    friendController.setProfileImage(friends.get(i).getUrlProfileImage());
                    friendController.setLocation(friends.get(i).getCap()+","+friends.get(i).getProvince()+","+friends.get(i).getRegion());
                    friendsListController.addFriendToList(friendController);

                    final int j= i;
                    Platform.runLater(() -> {
                        ((FriendsListController)view).addFriendToUI(root);
                        if(j==friends.size()-1)
                            ((FriendsListController)view).updateNumberOfFriendsLabel(friends.size());
                    });
                    Thread.sleep(5);
                }
            }
            catch (DiscoveryException | ServiceUnavailableException s){

            Platform.runLater(() -> {
                //pop up to warn of the possibility of browsing in exploration mode
                ((FriendsListController)view).destroy();
                ((FriendsListController)view).showPopUpOfExplorationMode();
            });
        }catch (Neo4jException n) {
            Platform.runLater(() -> {
                ((FriendsListController)view).showPopUp("There was a problem. Impossible to load friends.");
            });
        }catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        progressIndicatorFriendsList.setVisible(false);
    }
}
