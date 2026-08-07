package com.example.personalexpensemanager.view;

import com.example.personalexpensemanager.controller.MainController;
import com.example.personalexpensemanager.manager.ExpenseManager;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Lớp cầu nối cho giao diện JavaFX: nạp FXML rồi tiêm {@link ExpenseManager} vào
 * controller. Nhờ vậy {@code PersonalExpenseManagerApp} không phải biết gì về FXML,
 * còn controller không phải tự dựng lấy dữ liệu nghiệp vụ.
 */
public class AppView {

  private static final String MAIN_VIEW = "/com/example/personalexpensemanager/main-view.fxml";

  private final ExpenseManager expenseManager;
  private Parent root;
  private MainController controller;

  public AppView(ExpenseManager expenseManager) {
    if (expenseManager == null) {
      throw new IllegalArgumentException("ExpenseManager không được để trống");
    }
    this.expenseManager = expenseManager;
  }

  /** Dựng cây giao diện màn hình chính và gắn dữ liệu vào đó. */
  public Parent load() throws IOException {
    URL location = AppView.class.getResource(MAIN_VIEW);
    if (location == null) {
      throw new IllegalStateException("Không tìm thấy file giao diện " + MAIN_VIEW);
    }
    FXMLLoader loader = new FXMLLoader(location);
    root = loader.load();
    controller = loader.getController();
    controller.setExpenseManager(expenseManager);
    return root;
  }

  public MainController getController() {
    if (controller == null) {
      throw new IllegalStateException("Gọi load() trước khi lấy controller");
    }
    return controller;
  }

  public ExpenseManager getExpenseManager() {
    return expenseManager;
  }
}
