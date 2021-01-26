package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.ProfileController;
import com.instafound.javafx.controller.SuggestedFriendController;
import com.instafound.javafx.model.SuggestedFriendModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.io.IOException;
import java.util.ArrayList;

public class SuggestedFriendsLoaderTask extends Task<Parent> {

    private ProfileController profileController;

    public SuggestedFriendsLoaderTask(ProfileController profileController) {

        this.profileController=profileController;

    }

    @Override
    protected Parent call() throws Exception {

        //here i make the connection with DATABASE

        //get from db 3 suggested friends
        profileController.getProgressIndicatorFriendsSuggested().setVisible(true);
        try{
        ArrayList<SuggestedFriendModel> suggestedFriendModels = Request.getSuggestedFriend(profileController.getUsernameOfCurrentUser());
            for(int i=0; i<suggestedFriendModels.size(); i++) {
                //PRENDO L'FXML (la sua radice) E LO AGGIUNGO COME FIGLIO DI BOXCAMPAIGNS
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "../../suggestedFriendModel.fxml"));
                final Parent root = (Parent) loader.load();
                SuggestedFriendController suggestedFriendController = (SuggestedFriendController) loader.getController();
                suggestedFriendController.setUsernameSuggestedFriend(suggestedFriendModels.get(i).getUsername());
                suggestedFriendController.setReference(profileController);
                suggestedFriendController.setProfileImage(suggestedFriendModels.get(i).getUrlProfileImage());
                suggestedFriendController.setLocationSuggestedFriend(suggestedFriendModels.get(i).getCap()+","+
                                                                     suggestedFriendModels.get(i).getProvince()+","+
                                                                     suggestedFriendModels.get(i).getRegion());
                suggestedFriendController.setFavoriteCategoryInCommon(suggestedFriendModels.get(i).getInCommon());
                Platform.runLater(() -> {
                   profileController.getContainerSuggestedFriends().getChildren().add(root);
                });
                Thread.sleep(5);
            }
        }catch (DiscoveryException | ServiceUnavailableException s){
            Platform.runLater(() -> {
                //pop up to warn of the possibility of browsing in exploration mode
                profileController.destroy();
                profileController.showPopUpOfExplorationMode();
            });
        }catch (Neo4jException n) {
            Platform.runLater(() -> {
                profileController.showPopUpMessage("There was a problem. Please try later.");
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
        profileController.getProgressIndicatorFriendsSuggested().setVisible(false);
    }
}
