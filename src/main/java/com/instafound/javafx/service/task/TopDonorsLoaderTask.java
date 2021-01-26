package com.instafound.javafx.service.task;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.DonorRankModel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;

import java.util.ArrayList;

public class TopDonorsLoaderTask extends Task<Parent> {

    private HomeController homeController;

    public TopDonorsLoaderTask(HomeController homeController) {

        this.homeController = homeController;

    }

    @Override
    protected Parent call() throws Exception {

        //get
        ArrayList<DonorRankModel> topDonors = Request.getTopMonthlyDonors();

        //update UI
        Platform.runLater(() -> {
            homeController.updateDonorsRankingUI(topDonors);
        });

        return null;
    }

}
