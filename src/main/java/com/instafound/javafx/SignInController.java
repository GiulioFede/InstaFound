package com.instafound.javafx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.instafound.javafx.backend.queries.Request;
import com.instafound.javafx.controller.HomeController;
import com.instafound.javafx.model.UserInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class SignInController implements Initializable{
    
    
    @FXML
    TextField usernameLoginTF;
    @FXML
    PasswordField passwordLoginTF;
    @FXML
    Label missingValuesLoginLabel;
    @FXML
    ProgressIndicator progressIndicatorLogin;
    @FXML
    Pane signInPane,explorationModePane;
    private static final Effect frostEffect = new BoxBlur(100,100,3);
    
    private String user, password;
    
    private StringBuilder missingFields;

    public void login(){
        progressIndicatorLogin.setVisible(true);

        missingFields = new StringBuilder();
        user = usernameLoginTF.getText();
        password = passwordLoginTF.getText();

        if (user.isEmpty() || password.isEmpty()) {
           missingFields.append("Error: missing");
           if (user.isEmpty()) missingFields.append(" Username,");
           if (password.isEmpty()) missingFields.append(" Password,");

           missingFields.append(" please insert it");
           missingValuesLoginLabel.setText(missingFields.toString());
           progressIndicatorLogin.setVisible(false);
        }else {
                try {
                    UserInfo userInfo = Request.login(user, password);

                    if(userInfo==null) {
                        missingValuesLoginLabel.setText("Wrong username or password.");
                    }
                    else {
                        missingValuesLoginLabel.setText("ok");

                        //open instafound
                        openInstaFoundWindow(userInfo, HomeController.NORMAL_MODE);
                    }
                }catch (DiscoveryException | ServiceUnavailableException e) {
                    openExplorationPane();
                }catch (Exception n){
                    missingValuesLoginLabel.setText("An error occured. Please try later.");
                }finally {
                    progressIndicatorLogin.setVisible(false);
                }


        }

    }

    private void openInstaFoundWindow(UserInfo userInfo, String mode) {

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "home.fxml"));
        Parent root = null;
        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage primaryStage = new Stage();
        primaryStage.initStyle(StageStyle.TRANSPARENT);;
        Scene scene = new Scene(root);
        scene.getStylesheets().add((getClass().getResource("settingsOfCSS.css").toString()));
        primaryStage.setScene(scene);
        primaryStage.show();

        HomeController homeController = (HomeController)loader.getController();
        homeController.setUserInfo(userInfo);

        homeController.setMode(mode);
        homeController.loadHome();

        //close current window
        progressIndicatorLogin.setVisible(false);
        ( (Stage) missingValuesLoginLabel.getScene().getWindow()).close();


    }

    @FXML
    public void clearUsernameTextField(){
        usernameLoginTF.clear();
    }
    @FXML
    public void clearPasswordTextField(){
        passwordLoginTF.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }


    public void frostStartUpPane(){

        signInPane.setEffect(frostEffect);
    }

    public void unFrostStartUpPane(){

        signInPane.setEffect(null);
    }

    public void openExplorationPane(){

        explorationModePane.setVisible(true);
        frostStartUpPane();
    }


    public void closeExplorationPane(MouseEvent mouseEvent) {

        unFrostStartUpPane();
        explorationModePane.setVisible(false);
    }

    public void startExplorationMode(MouseEvent mouseEvent) {

        openInstaFoundWindow(null, HomeController.EXPLORATION_MODE);
    }
}
