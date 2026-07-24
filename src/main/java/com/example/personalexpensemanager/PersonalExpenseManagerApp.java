package com.example.personalexpensemanager;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Entry point JavaFX của ứng dụng. */
public class PersonalExpenseManagerApp extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader loader =
        new FXMLLoader(
            PersonalExpenseManagerApp.class.getResource(
                "main-view.fxml"));
    Scene scene = new Scene(loader.load(), 640, 480);
    stage.setTitle("Personal Expense Manager");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
