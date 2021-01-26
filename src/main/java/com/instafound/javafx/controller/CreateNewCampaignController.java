package com.instafound.javafx.controller;

import com.instafound.javafx.backend.queries.Insert;
import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.model.CampaignMongoDB;
import com.instafound.javafx.model.Donor;
import com.instafound.javafx.model.Team;
import com.instafound.javafx.model.UserInfo;
import com.mongodb.MongoTimeoutException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CreateNewCampaignController implements Initializable {

    @FXML
    Pane containerCentralHome,containerOtherInterfaces;
    @FXML
    Button medicalExpensesButton,environmentButton, noProfitButton,creativityButton,animalsButton;
    @FXML
    TextField titleNewCampaign,urlImageNewCampaign,amountRequiredNewCampaign,beneficiaryNewCampaign;
    @FXML
    TextArea descriptionNewCampaign;
    @FXML
    CheckBox teamNewCampaignOption;
    @FXML
    ListView listViewOfTeams;
    @FXML
    ProgressIndicator progressIndicatorListOfTeam;
    @FXML
    Label mexCreationCampaign, categoryLabel;

    private ArrayList<Team> teams;
    private HomeController homeController;

    private String categorySelected;
    private UserInfo userInfo;
    Button selectedCategoryButton;
    private SimpleDateFormat formatTimestamp = new SimpleDateFormat("yyyy/MM/dd");


    public void setHomePanel(Pane containerCentralHome, Pane containerOtherInterfaces){
        this.containerCentralHome= containerCentralHome;
        this.containerOtherInterfaces = containerOtherInterfaces;

    }

    public void setUserInfo(UserInfo userInfo){
        this.userInfo = userInfo;
    }

    public void backToHome(MouseEvent mouseEvent) {

        homeController.highlightsButton(0);
        containerCentralHome.setVisible(true);
        containerOtherInterfaces.setVisible(false);
    }

    private void loadTeam(){

        listViewOfTeams.setVisible(true);
        //if teams were previously loaded, avoid it
        if(teams.size()!=0)
            return;
        else{
            //load teams from DB
            //TODO
            progressIndicatorListOfTeam.setVisible(true);
            ArrayList<Team> tmpTeams = Request.getUserTeamsDetails(userInfo.getUsername());
            ObservableList<String> nameOfTeams = FXCollections.observableArrayList();
            //get name of teams and remove teams in which the current user is not the organizer
            for(Team team: tmpTeams){
                if(team.getUsernameOrganizer().compareTo(userInfo.getUsername())==0) {
                    nameOfTeams.add("- "+team.getName()+" - "+team.getUsernameMembers().toString().replace("[","").replace("]","")+" has supported "+team.getIdCampaignsSupported().size()+ " campaigns");
                    teams.add(team);
                }
            }
            progressIndicatorListOfTeam.setVisible(false);

            //add to a listView
            listViewOfTeams.setItems(nameOfTeams);
        }

    }

    public void createNewCampaign(){

        mexCreationCampaign.setVisible(true);

        if(checkFields()==false) {
            mexCreationCampaign.setText("fill in all fields before proceeding.");
            mexCreationCampaign.getStyleClass().remove(1);
            mexCreationCampaign.getStyleClass().add("errorLabel");

        }
        else {
           ArrayList<String> membersOfTeam = new ArrayList<>();
           String team = null;
           if(listViewOfTeams.getSelectionModel().getSelectedIndex()!=-1) {
               team = teams.get(listViewOfTeams.getSelectionModel().getSelectedIndex()).getName();
               membersOfTeam = teams.get(listViewOfTeams.getSelectionModel().getSelectedIndex()).getUsernameMembers();
           }

    try {
        CampaignMongoDB newCampaign = new CampaignMongoDB(null,
                titleNewCampaign.getText(),
                urlImageNewCampaign.getText(),
                formatTimestamp.format(new Timestamp(System.currentTimeMillis())),
                Integer.valueOf(amountRequiredNewCampaign.getText()),
                0,
                0,
                0,
                userInfo.getUsername(),
                membersOfTeam,
                descriptionNewCampaign.getText(),
                beneficiaryNewCampaign.getText(),
                selectedCategoryButton.getText(),
                new ArrayList<Donor>(),
                0);

        boolean outcome = Insert.insertNewCampaign(newCampaign, team);


        if (outcome == true) {
            mexCreationCampaign.setText("new campaign created!");
            mexCreationCampaign.getStyleClass().remove(1);
            mexCreationCampaign.getStyleClass().add("correctLabel");
        } else {
            mexCreationCampaign.setText("An error occured..Please try later.");
            mexCreationCampaign.getStyleClass().remove(1);
            mexCreationCampaign.getStyleClass().add("errorLabel");
        }

    }catch (NumberFormatException nfe){
        mexCreationCampaign.setText("error!");
        mexCreationCampaign.getStyleClass().remove(1);
        mexCreationCampaign.getStyleClass().add("errorLabel");
    }
    catch (MongoTimeoutException m){
        homeController.showPopUpMessage("At the moment it is not possible to create the campaign. Come back later. We apologize for the event.");
    }
    finally {
        reset();
    }

    }

    }

    private boolean checkFields(){

        boolean outcome = true;

        if(titleNewCampaign.getText().length()==0) {
            outcome = false;
            titleNewCampaign.getStyleClass().remove(2);
            titleNewCampaign.getStyleClass().add("errorField");
        }else {
            titleNewCampaign.getStyleClass().remove(2);
            titleNewCampaign.getStyleClass().add("correctField");
        }
        if(urlImageNewCampaign.getText().length()==0){
            outcome = false;
            urlImageNewCampaign.getStyleClass().remove(2);
            urlImageNewCampaign.getStyleClass().add("errorField");
        }else {
            urlImageNewCampaign.getStyleClass().remove(2);
            urlImageNewCampaign.getStyleClass().add("correctField");
        }
        if(amountRequiredNewCampaign.getText().length()==0){
            outcome = false;
            amountRequiredNewCampaign.getStyleClass().remove(2);
            amountRequiredNewCampaign.getStyleClass().add("errorField");
        }else {
            amountRequiredNewCampaign.getStyleClass().remove(2);
            amountRequiredNewCampaign.getStyleClass().add("correctField");
        }
        if(descriptionNewCampaign.getText().length()==0){
            outcome = false;
            descriptionNewCampaign.getStyleClass().remove(2);
            descriptionNewCampaign.getStyleClass().add("errorField");
        }else {
            descriptionNewCampaign.getStyleClass().remove(2);
            descriptionNewCampaign.getStyleClass().add("correctField");
        }

        if(medicalExpensesButton.getOpacity()!=1 &&
           environmentButton.getOpacity()!=1 &&
           noProfitButton.getOpacity()!=1 &&
           creativityButton.getOpacity()!=1 &&
           animalsButton.getOpacity()!=1){

            categoryLabel.setStyle("-fx-text-fill: red;");
        }else {
            categoryLabel.setStyle("-fx-text-fill: black;");
            if(medicalExpensesButton.getOpacity()==1)
                selectedCategoryButton = medicalExpensesButton;
            else if(environmentButton.getOpacity()==1)
                selectedCategoryButton = environmentButton;
            else if(noProfitButton.getOpacity()==1)
                selectedCategoryButton = noProfitButton;
            else if(creativityButton.getOpacity()==1)
                selectedCategoryButton = creativityButton;
            else
                selectedCategoryButton = animalsButton;
        }



        return outcome;
    }

    //given i highlights the i° button
    public void highlightsButton(MouseEvent mouseEvent){

        String textButton = ((Button)mouseEvent.getSource()).getText();

        categorySelected = textButton;

        switch (textButton){
            case "Medical Expenses":
            {
                medicalExpensesButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                medicalExpensesButton.getStyleClass().add("selectedButtonOnCreateCampaign"); //ad new class name of belonging

                environmentButton.getStyleClass().remove(1);
                environmentButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                noProfitButton.getStyleClass().remove(1);
                noProfitButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                creativityButton.getStyleClass().remove(1);
                creativityButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                animalsButton.getStyleClass().remove(1);
                animalsButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
                break;
            }
            case "Environment":
            {
                medicalExpensesButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                medicalExpensesButton.getStyleClass().add("unselectedButtonsOnCreateCampaign"); //ad new class name of belonging

                environmentButton.getStyleClass().remove(1);
                environmentButton.getStyleClass().add("selectedButtonOnCreateCampaign");

                noProfitButton.getStyleClass().remove(1);
                noProfitButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                creativityButton.getStyleClass().remove(1);
                creativityButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                animalsButton.getStyleClass().remove(1);
                animalsButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
                break;
            }

            case "No-Profit":
            {
                medicalExpensesButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                medicalExpensesButton.getStyleClass().add("unselectedButtonsOnCreateCampaign"); //ad new class name of belonging

                environmentButton.getStyleClass().remove(1);
                environmentButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                noProfitButton.getStyleClass().remove(1);
                noProfitButton.getStyleClass().add("selectedButtonOnCreateCampaign");

                creativityButton.getStyleClass().remove(1);
                creativityButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                animalsButton.getStyleClass().remove(1);
                animalsButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
                break;
            }

            case "Creativity":
            {
                medicalExpensesButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                medicalExpensesButton.getStyleClass().add("unselectedButtonsOnCreateCampaign"); //ad new class name of belonging

                environmentButton.getStyleClass().remove(1);
                environmentButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                noProfitButton.getStyleClass().remove(1);
                noProfitButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                creativityButton.getStyleClass().remove(1);
                creativityButton.getStyleClass().add("selectedButtonOnCreateCampaign");

                animalsButton.getStyleClass().remove(1);
                animalsButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
                break;
            }

            case "Animals": {
                medicalExpensesButton.getStyleClass().remove(1); //remove last class name of belonging (css)
                medicalExpensesButton.getStyleClass().add("unselectedButtonsOnCreateCampaign"); //ad new class name of belonging

                environmentButton.getStyleClass().remove(1);
                environmentButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                noProfitButton.getStyleClass().remove(1);
                noProfitButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                creativityButton.getStyleClass().remove(1);
                creativityButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");

                animalsButton.getStyleClass().remove(1);
                animalsButton.getStyleClass().add("selectedButtonOnCreateCampaign");
            }
        }
    }

    private void reset(){

        listViewOfTeams.setVisible(false);

        titleNewCampaign.setText("");
        urlImageNewCampaign.setText("");
        amountRequiredNewCampaign.setText("");
        descriptionNewCampaign.setText("");
        beneficiaryNewCampaign.setText("");

        teamNewCampaignOption.setSelected(false);

        progressIndicatorListOfTeam.setVisible(false);


        //hide all buttons
        medicalExpensesButton.getStyleClass().remove(1);
        medicalExpensesButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
        noProfitButton.getStyleClass().remove(1);
        noProfitButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
        creativityButton.getStyleClass().remove(1);
        creativityButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
        environmentButton.getStyleClass().remove(1);
        environmentButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
        animalsButton.getStyleClass().remove(1);
        animalsButton.getStyleClass().add("unselectedButtonsOnCreateCampaign");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reset();

        listViewOfTeams.setVisible(false);

        teams = new ArrayList<>();

        //add listener on checkbox
        teamNewCampaignOption.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if(newValue == true){
                    loadTeam();
                }else
                    listViewOfTeams.setVisible(false);
            }
        });
    }

    public void setReference(HomeController homeController) {

        this.homeController = homeController;
    }
}
