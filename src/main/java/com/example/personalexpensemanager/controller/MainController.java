package com.example.personalexpensemanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller FXML tạm cho màn hình chính.
 * Sẽ mở rộng khi hoàn thiện giao diện thật.
 */
public class MainController {

  @FXML
  private Label welcomeLabel;

  @FXML
  private void initialize() {
    // TODO: gắn dữ liệu từ ExpenseManager khi có
    if (welcomeLabel != null) {
      welcomeLabel.setText("Personal Expense Manager");
    }
  }
}
