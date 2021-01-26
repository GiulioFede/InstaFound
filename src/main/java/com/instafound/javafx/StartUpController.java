package com.instafound.javafx;

import com.instafound.javafx.backend.Connection;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class StartUpController implements Initializable {

    @FXML
    private VBox vbox;
    @FXML
    Pane startUpPane;
    private Parent fxml;
    @FXML
    private Button buttonLog;
    @FXML
    private Button buttonReg;
    
    private FadeTransition fadeFrom, fadeTo;



    @Override
    public void initialize(URL url, ResourceBundle rb) {

                /*
        the first and also the last creation and initialization of the connection will be done
         */

        Connection connection = Connection.getInstance();

        showSignIn();
    }

    public void showSignIn(){

        TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
        t.setToX(800);
        FXMLLoader loader = new FXMLLoader();
        try {
            fxml = loader.load(getClass().getResource("SignIn.fxml"));
            vbox.getChildren().setAll(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        t.play();



    }
    @FXML
    private void open_signing(ActionEvent event){

        buttonLog.setDisable(true);
        createLogInTransition(vbox.getChildren().get(0),vbox);

    }
    @FXML
    private void open_signup(ActionEvent event){

        buttonReg.setDisable(true);
        createRegisterTransition(vbox.getChildren().get(0),vbox);

    }

    private void createRegisterTransition(Node to, VBox vbox){

        TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
        t.setToX(400);
        Node from = vbox.getChildren().get(0);
        fadeFrom = new FadeTransition(Duration.millis(330),from);
        fadeFrom.setFromValue(1.0);
        fadeFrom.setToValue(0.5);
        fadeFrom.setAutoReverse(false);
        fadeFrom.setOnFinished((e) ->{
            try{

                fxml = FXMLLoader.load(getClass().getResource("SignUp.fxml"));
                fxml.setOpacity(0.2);
                fadeTo = new FadeTransition(Duration.millis(330),fxml);
                fadeTo.setToValue(1.0);
                fadeFrom.setAutoReverse(false);
                vbox.getChildren().removeAll();
                vbox.getChildren().setAll(fxml);
                fadeTo.play();
                buttonReg.setDisable(false);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        });
        ParallelTransition p = new ParallelTransition(t, fadeFrom);
        p.play();

    }

    private void createLogInTransition(Node to, VBox vbox){


        TranslateTransition t = new TranslateTransition(Duration.seconds(1), vbox);
        t.setToX(800);
        Node from = vbox.getChildren().get(0);
        fadeFrom = new FadeTransition(Duration.millis(330),from);
        fadeFrom.setFromValue(1.0);
        fadeFrom.setToValue(0.5);
        fadeFrom.setAutoReverse(false);
        fadeFrom.setOnFinished((e) ->{
            try{
                FXMLLoader loader = new FXMLLoader();
                fxml = loader.load(getClass().getResource("SignIn.fxml"));
                fxml.setOpacity(0.2);
                fadeTo = new FadeTransition(Duration.millis(330),fxml);
                fadeTo.setToValue(1.0);
                fadeFrom.setAutoReverse(false);
                vbox.getChildren().removeAll();
                vbox.getChildren().setAll(fxml);
                fadeTo.play();
                buttonLog.setDisable(false);
            }catch(IOException ex){
                ex.printStackTrace();
            }
        });
        ParallelTransition p = new ParallelTransition(t, fadeFrom);
        p.play();
    }

    public void min(javafx.scene.input.MouseEvent mouseEvent) {
        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.setIconified(true);
    }

    public void close(javafx.scene.input.MouseEvent mouseEvent) {
        //first close the connection
        try {
            Connection.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Stage s = (Stage) ((Node)mouseEvent.getSource()).getScene().getWindow();
        s.close();
    }


    //------------------------------------- EXPLORATION MODE ---------------------------------------------------

}


