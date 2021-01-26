package com.instafound.javafx.controller;

import com.instafound.javafx.model.UserInfo;
import com.instafound.javafx.service.FriendsListLoader;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FriendsListController implements Initializable {

    @FXML
    Pane containerCentralHome,containerOtherInterfaces;
    @FXML
    HBox boxFriendsList;
    @FXML
    ProgressIndicator progressIndicatorFriendsList;
    @FXML
    Label labelNumberOfFriends;

    private UserInfo userInfo;
    private ArrayList<FriendController> friendsList;
    private HomeController homeController;

    public void setReference(HomeController homeController){
        this.homeController = homeController;
    }

    public void loadFriendsList(){

        FriendsListLoader friendsListLoader = new FriendsListLoader(this,progressIndicatorFriendsList, null, "friendListModel.fxml");
        friendsListLoader.start();
    }

    public void setUserInfo(UserInfo userInfo){

        this.userInfo = userInfo;
    }

    public void updateNumberOfFriendsLabel(int c){

        labelNumberOfFriends.setText(("friends ("+c+")"));
    }

    public String getUsernameOfUser(){

        return userInfo.getUsername();
    }

    public void addFriendToList(FriendController friend){

        friendsList.add(friend);
    }

    public void addFriendToUI(Parent fxmlFriend){

        boxFriendsList.getChildren().add(fxmlFriend);
    }

    public void removeFriendFromUI(FriendController friendController){

        int iChild = friendsList.indexOf(friendController);
        friendsList.remove(iChild);
        labelNumberOfFriends.setText(("friends ("+friendsList.size()+")"));
        boxFriendsList.getChildren().remove(iChild);
    }

    public void setHomePanel(Pane containerCentralHome, Pane containerProfile){
        this.containerCentralHome= containerCentralHome;
        this.containerOtherInterfaces = containerProfile;

    }

    public void backToHome(MouseEvent mouseEvent) {

        homeController.highlightsButton(0);
        containerCentralHome.setVisible(true);
        containerOtherInterfaces.setVisible(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        friendsList = new ArrayList<>();
    }

    public void destroy() {

        homeController.destroyPane();
    }

    public void showPopUpOfExplorationMode() {

        homeController.showPopUpOfExplorationMode();
    }

    public void showPopUp(String mex){

        homeController.showPopUpMessage(mex);
    }

    public void showPopUpMessage(String s) {

        homeController.showPopUpMessage(s);
    }
}
