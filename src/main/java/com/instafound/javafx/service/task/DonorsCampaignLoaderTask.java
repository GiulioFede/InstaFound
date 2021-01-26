package com.instafound.javafx.service.task;

import com.instafound.javafx.controller.CampaignDetailsController;
import com.instafound.javafx.controller.DonorController;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.Donor;
import com.instafound.javafx.model.UserInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonorsCampaignLoaderTask extends Task<Parent> {

    private CampaignDetailsController campaignDetailsController;
    private List<Donor> donors;

    public DonorsCampaignLoaderTask(CampaignDetailsController campaignDetailsController) {

        this.campaignDetailsController = campaignDetailsController;
        this.donors = new ArrayList<>();

    }

    @Override
    protected Parent call() throws Exception {

        //for the first time here i don't use a connection with db, i have already all the data available inside di donarions array of campaign.
        //but i use also here a background task due to the fact that donors are really to much, so their loading will block the UI for a few
        // seconds (an average of 3 seconds for 21,000 donations)

        campaignDetailsController.setProgressIndicatorVisibilityOfDonorList(true);

        donors = campaignDetailsController.getDonationsList();

        for(Donor donor: donors){
            FXMLLoader loader = new FXMLLoader();
            loader = new FXMLLoader(getClass().getResource(
                    "../../donorModel.fxml"));
            Parent root = null;

            try {
                root = (Parent) loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            DonorController donorController= (DonorController) loader.getController();
            donorController.setUsername(donor.getDonor());
            donorController.setAmountDonated(donor.getAmount());
            donorController.setTimestampOfDonationLabel(donor.getTimestamp());
            //if the donor is already friend of current user...

            if(campaignDetailsController.getMode().compareTo(HomeController.NORMAL_MODE)==0) {
                donorController.setCurrentUserUsername(campaignDetailsController.getUsernameOfCurrentUser());

                for (UserInfo friendUser : campaignDetailsController.getFriendsOfCurrentUser()) {
                    if (friendUser.getUsername().compareTo(donor.getDonor()) == 0) {
                        donorController.hideAddFriendButton();
                        break;
                    }
                }
            }else
                donorController.hideAddFriendButton();

            //update UI
            Parent finalRoot = root;
            Platform.runLater(() -> {
                campaignDetailsController.addDonorToListUI(finalRoot);
            });
        }

        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        campaignDetailsController.setProgressIndicatorVisibilityOfDonorList(false);
        campaignDetailsController.updateLabelCountDonations(donors.size());
    }
}
