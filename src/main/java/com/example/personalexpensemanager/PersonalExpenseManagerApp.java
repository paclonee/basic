package com.example.personalexpensemanager;

import com.example.personalexpensemanager.controller.MainController;
import com.example.personalexpensemanager.manager.ExpenseManager;
import com.example.personalexpensemanager.storage.JsonStorage;
import com.example.personalexpensemanager.util.FxBackground;
import com.example.personalexpensemanager.view.AppView;
import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Entry point JavaFX của ứng dụng và cũng là nơi quyết định vòng đời dữ liệu:
 * đọc file lúc mở app, ghi file lúc đóng cửa sổ — cả hai chạy trên luồng nền
 * để JavaFX Application Thread không bị chặn.
 */
public class PersonalExpenseManagerApp extends Application {

  private static final String TRANSACTION_FILE = "data/transactions.json";
  private static final String WALLET_FILE = "data/wallets.json";

  private final ExpenseManager manager =
          new ExpenseManager(JsonStorage.forTransactions(), JsonStorage.forWallets());

  private boolean saving;

  @Override
  public void start(Stage stage) throws IOException {
    AppView appView = new AppView(manager);
    Scene scene = new Scene(appView.load());
    MainController controller = appView.getController();

    stage.setTitle("Personal Expense Manager");
    stage.setScene(scene);
    stage.setOnCloseRequest(event -> onCloseRequest(event, stage));
    stage.show();

    // Hiện cửa sổ trước, rồi mới đọc file trên luồng nền (tương tự splash/loading trên mobile).
    loadSavedDataAsync(controller);
  }

  /**
   * Lần chạy đầu chưa có file thì Storage trả về danh sách rỗng, không phải lỗi.
   * Còn file hỏng thì báo cho người dùng và mở app trắng, hơn là crash lúc khởi động.
   */
  private void loadSavedDataAsync(MainController controller) {
    controller.setBusy(true, "Đang tải dữ liệu...");
    FxBackground.run(
            "load-data",
            true,
            () -> {
              manager.loadData(TRANSACTION_FILE, WALLET_FILE);
              return null;
            },
            ignored -> controller.onDataReady("Đã tải dữ liệu từ file"),
            error -> {
              controller.onDataReady(null);
              showAlert(Alert.AlertType.WARNING,
                      "Không đọc được dữ liệu đã lưu, ứng dụng mở với dữ liệu trống.\n"
                              + error.getMessage());
            });
  }

  /**
   * Chặn đóng cửa sổ ngay lập tức, ghi file trên luồng nền, rồi mới đóng.
   * Nếu ghi lỗi thì giữ app mở để người dùng thử lại.
   */
  private void onCloseRequest(WindowEvent event, Stage stage) {
    event.consume();
    if (saving) {
      return;
    }
    saving = true;
    FxBackground.run(
            "save-data",
            false,
            () -> {
              manager.saveData(TRANSACTION_FILE, WALLET_FILE);
              return null;
            },
            ignored -> stage.close(),
            error -> {
              saving = false;
              showAlert(Alert.AlertType.ERROR,
                      "Không ghi được dữ liệu xuống file.\n" + error.getMessage());
            });
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
