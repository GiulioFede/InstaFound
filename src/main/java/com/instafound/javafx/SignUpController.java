package com.instafound.javafx;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.instafound.javafx.backend.queries.Insert;
import com.instafound.javafx.controller.HomeController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.DiscoveryException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

public class SignUpController implements Initializable {

    @FXML
    Pane signUpPane, explorationModePane;
    @FXML
    private TextField usernameTF,regionTF,capTF,provinceTF, urlProfile;
    @FXML
    private PasswordField passwordTF;
    @FXML
    private Label missingValuesLabel;
    
    private StringBuilder missingFields;
    private static final Effect frostEffect = new BoxBlur(100,100,3);


    @FXML
    public void register(){

        try {
            missingFields = new StringBuilder();
            String user = usernameTF.getText();
            String password = passwordTF.getText();
            String region = regionTF.getText();
            int cap = Integer.valueOf(capTF.getText());
            String province = provinceTF.getText();
            if (user.isEmpty() || password.isEmpty() || region.isEmpty() || province.isEmpty() || cap <= 0) {
                missingFields.append("Error: missing");
                if (user.isEmpty()) missingFields.append(" Username,");
                if (password.isEmpty()) missingFields.append(" Password,");
                if (region.isEmpty()) missingFields.append(" Region,");
                if (province.isEmpty()) missingFields.append(" Province,");
                if (cap <= 0) missingFields.append(" CAP,");
                missingFields.append(" please insert it");
                missingValuesLabel.setText(missingFields.toString());
            } else
                try {
                    registerNewUser(user.replace(" ", ""), password, urlProfile.getText(), region, province, cap);
                } catch (DiscoveryException | ServiceUnavailableException s) {
                   // openInstaFoundWindow(HomeController.EXPLORATION_MODE);
                    openExplorationPane();
                } catch (Exception n) {
                    missingValuesLabel.setText("An error occured. Please try later.");
                }
        }catch (Exception e){
            missingValuesLabel.setText(e.getMessage());
        }
    }

    private void registerNewUser(String user, String password, String url, String region, String province, int cap) {

        try {
            boolean outcome = Insert.registerNewUser(user, password, url, region, province, cap);
            if(outcome==true) {
                missingValuesLabel.setText("Registration was successful! Log in.");
                missingValuesLabel.setStyle("-fx-text-fill: green");
            }else
                missingValuesLabel.setText("Something went wrong. Try later please.");
        }catch (ClientException c){
            missingValuesLabel.setText("Username already used.");
        }
    }

    @FXML
    public void clearUsernameTextField(){
        usernameTF.clear();
    }
    @FXML
    public void clearPasswordTextField(){
        passwordTF.clear();
    }
    @FXML
    public void clearRegionTextField(){
        regionTF.clear();
    }
    @FXML
    public void clearProvinceTextField(){
        provinceTF.clear();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    public void frostStartUpPane(){

        signUpPane.setEffect(frostEffect);
    }

    public void unFrostStartUpPane(){

        signUpPane.setEffect(null);
    }

    public void openExplorationPane(){

        frostStartUpPane();
        explorationModePane.setVisible(true);
    }

    public void closeExplorationPane(MouseEvent mouseEvent) {

        unFrostStartUpPane();
        explorationModePane.setVisible(false);
    }

    public void startExplorationMode(MouseEvent mouseEvent) {

        openInstaFoundWindow(HomeController.EXPLORATION_MODE);
    }

    private void openInstaFoundWindow(String mode) {

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

        homeController.setMode(mode);
        homeController.loadHome();

        //close current window
        ( (Stage) missingValuesLabel.getScene().getWindow()).close();


    }

}
