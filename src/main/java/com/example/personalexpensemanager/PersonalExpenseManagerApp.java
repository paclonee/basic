package com.example.personalexpensemanager;

import com.example.personalexpensemanager.manager.ExpenseManager;
import com.example.personalexpensemanager.storage.JsonStorage;
import com.example.personalexpensemanager.view.AppView;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Entry point JavaFX của ứng dụng và cũng là nơi quyết định vòng đời dữ liệu:
 * đọc file lúc mở app, ghi file lúc đóng cửa sổ.
 */
public class PersonalExpenseManagerApp extends Application {

  private static final String TRANSACTION_FILE = "data/transactions.json";
  private static final String WALLET_FILE = "data/wallets.json";

  private final ExpenseManager manager =
          new ExpenseManager(JsonStorage.forTransactions(), JsonStorage.forWallets());

  @Override
  public void start(Stage stage) throws IOException {
    loadSavedData();

    Scene scene = new Scene(new AppView(manager).load());
    stage.setTitle("Personal Expense Manager");
    stage.setScene(scene);
    stage.setOnCloseRequest(event -> saveData());
    stage.show();
  }

  /**
   * Lần chạy đầu chưa có file thì Storage trả về danh sách rỗng, không phải lỗi.
   * Còn file hỏng thì báo cho người dùng và mở app trắng, hơn là crash lúc khởi động.
   */
  private void loadSavedData() {
    try {
      manager.loadData(TRANSACTION_FILE, WALLET_FILE);
    } catch (IOException | RuntimeException e) {
      showAlert(Alert.AlertType.WARNING,
              "Không đọc được dữ liệu đã lưu, ứng dụng mở với dữ liệu trống.\n" + e.getMessage());
    }
  }

  private void saveData() {
    try {
      manager.saveData(TRANSACTION_FILE, WALLET_FILE);
    } catch (IOException e) {
      showAlert(Alert.AlertType.ERROR, "Không ghi được dữ liệu xuống file.\n" + e.getMessage());
    }
  }

  private static void showAlert(Alert.AlertType type, String message) {
    Alert alert = new Alert(type, message);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
