package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.AnalyticalData;
import com.mongodb.MongoTimeoutException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;

import java.util.ArrayList;

public class TopCategoryLoaderTask extends Task<Parent> {

    private HomeController homeController;

    public TopCategoryLoaderTask(HomeController homeController) {

        this.homeController = homeController;

    }

    @Override
    protected Parent call() throws Exception {

        //get info user profile
        try {
            ArrayList<AnalyticalData> topCategories = Request.getTopMonthlyCategories();

            //update UI
            Platform.runLater(() -> {
                homeController.updateCakeUI(topCategories);
            });
        }catch (MongoTimeoutException m){
            //update UI
            Platform.runLater(() -> {
                homeController. showPopUpMessage("At the moment it is not possible to contact servers. Please try again later.");
            });
        }catch (Exception e){
            //update UI
            Platform.runLater(() -> {
                homeController.showPopUpMessage("At the moment it is not possible to see the statistics. Please try again later.");
            });
        }

        return null;
    }

}
