package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.UserInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class UserInfoLoaderTask extends Task<Parent> {

    private HomeController homeController;
    private String username;

    public UserInfoLoaderTask(HomeController homeController, String username) {

        this.homeController = homeController;
        this.username = username;

    }

    @Override
    protected Parent call() throws Exception {

        //get info user profile
        try {
            UserInfo userInfo = Request.getUserInfo(username);

            homeController.setUserInfo(userInfo);

            //update UI
            Platform.runLater(() -> {
                homeController.updateUserUI();
            });
        }catch (DiscoveryException | ServiceUnavailableException s){
            homeController.showPopUpOfExplorationMode();
        }catch (Exception e){
            Platform.runLater(() -> {
                homeController.showPopUpMessage("An error occured.");
            });
        }

         return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }
}
