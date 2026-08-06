package com.example.personalexpensemanager.controller;

import com.example.personalexpensemanager.enums.TransactionType;
import com.example.personalexpensemanager.enums.WalletType;
import com.example.personalexpensemanager.manager.ExpenseManager;
import com.example.personalexpensemanager.model.BankAccount;
import com.example.personalexpensemanager.model.CashWallet;
import com.example.personalexpensemanager.model.Category;
import com.example.personalexpensemanager.model.EWallet;
import com.example.personalexpensemanager.model.Expense;
import com.example.personalexpensemanager.model.Income;
import com.example.personalexpensemanager.model.Transaction;
import com.example.personalexpensemanager.model.Wallet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Controller FXML cho màn hình chính. Chỉ lo phần hiển thị và đọc dữ liệu người
 * dùng nhập. Mọi quy tắc nghiệp vụ đều ủy quyền cho {@link ExpenseManager}, kể cả
 * việc kiểm tra hợp lệ — lỗi ném ra được hiển thị lại nguyên văn ở thanh trạng thái.
 * 
 * XML (Extensible Markup Language) là ngôn ngữ đánh dấu tổng quát dùng để lưu trữ 
 * và truyền tải dữ liệu. Trong khi đó, FXML (JavaFX Markup Language) là một dạng 
 * con chuyên biệt dựa trên cú pháp của XML, được thiết kế riêng để xây dựng 
 * giao diện người dùng (GUI) trong các ứng dụng JavaFX.
 */
public class MainController {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @FXML private Label summaryLabel;
  @FXML private Label totalBalanceLabel;
  @FXML private Label statusLabel;

  @FXML private ListView<Wallet> walletList;
  @FXML private TextField walletNameField;
  @FXML private ComboBox<WalletType> walletTypeBox;
  @FXML private TextField walletBalanceField;

  @FXML private TextField searchField;
  @FXML private TableView<Transaction> transactionTable;
  @FXML private TableColumn<Transaction, String> dateColumn;
  @FXML private TableColumn<Transaction, String> typeColumn;
  @FXML private TableColumn<Transaction, String> categoryColumn;
  @FXML private TableColumn<Transaction, String> amountColumn;
  @FXML private TableColumn<Transaction, String> walletColumn;
  @FXML private TableColumn<Transaction, String> noteColumn;

  @FXML private ComboBox<TransactionType> typeBox;
  @FXML private TextField amountField;
  @FXML private DatePicker datePicker;
  @FXML private ComboBox<Category> categoryBox;
  @FXML private ComboBox<Wallet> walletBox;
  @FXML private TextField detailField;
  @FXML private TextField noteField;

  private ExpenseManager manager;

  @FXML
  private void initialize() {
    dateColumn.setCellValueFactory(cell -> cellText(cell.getValue().getDate().format(DATE_FORMAT)));
    typeColumn.setCellValueFactory(cell -> cellText(typeLabel(cell.getValue().getType())));
    categoryColumn.setCellValueFactory(cell -> cellText(cell.getValue().getCategory().getName()));
    amountColumn.setCellValueFactory(
            cell -> cellText(String.format("%,.0f", cell.getValue().getSignedAmount())));
    walletColumn.setCellValueFactory(cell -> cellText(cell.getValue().getWallet().getName()));
    noteColumn.setCellValueFactory(cell -> cellText(cell.getValue().getNote()));

    typeBox.setConverter(converter(MainController::typeLabel));
    typeBox.getItems().setAll(TransactionType.values());
    typeBox.setValue(TransactionType.EXPENSE);
    typeBox.valueProperty().addListener((observable, old, value) -> onTypeChanged());

    walletTypeBox.setConverter(converter(MainController::walletTypeLabel));
    walletTypeBox.getItems().setAll(WalletType.values());
    walletTypeBox.setValue(WalletType.CASH);

    datePicker.setValue(LocalDate.now());
    searchField.textProperty().addListener((observable, old, value) -> refreshTransactionTable());
    onTypeChanged();
  }

  /** Gắn dữ liệu vào màn hình. Gọi một lần sau khi FXML đã nạp xong. */
  public void setExpenseManager(ExpenseManager expenseManager) {
    if (expenseManager == null) {
      throw new IllegalArgumentException("ExpenseManager không được để trống");
    }
    this.manager = expenseManager;
    seedDefaultCategories();
    refreshAll();
  }

  // --- Thao tác của người dùng ---

  @FXML
  private void handleAddTransaction() {
    try {
      String id = nextTransactionId();
      double amount = parseMoney(amountField.getText(), "số tiền");
      LocalDate date = required(datePicker.getValue(), "Hãy chọn ngày giao dịch");
      Category category = required(categoryBox.getValue(), "Hãy chọn danh mục");
      Wallet wallet = required(walletBox.getValue(), "Hãy chọn ví, hoặc thêm ví mới trước");
      String note = blankToNull(noteField.getText());
      String detail = blankToNull(detailField.getText());

      Transaction transaction = typeBox.getValue() == TransactionType.INCOME
              ? new Income(id, amount, date, note, category, wallet, detail)
              : new Expense(id, amount, date, note, category, wallet, detail);
      manager.addTransaction(transaction);

      amountField.clear();
      noteField.clear();
      detailField.clear();
      refreshAll();
      showInfo("Đã ghi giao dịch " + id);
    } catch (RuntimeException e) {
      showError(e.getMessage());
    }
  }

  @FXML
  private void handleDeleteTransaction() {
    Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showError("Hãy chọn một giao dịch trong bảng trước khi xoá");
      return;
    }
    try {
      manager.removeTransaction(selected.getId());
      refreshAll();
      showInfo("Đã xoá giao dịch " + selected.getId() + " và hoàn lại tiền vào ví");
    } catch (RuntimeException e) {
      showError(e.getMessage());
    }
  }

  @FXML
  private void handleAddWallet() {
    try {
      String name = walletNameField.getText();
      double balance = parseOptionalMoney(walletBalanceField.getText());
      manager.addWallet(createWallet(walletTypeBox.getValue(), name, balance));

      walletNameField.clear();
      walletBalanceField.clear();
      refreshAll();
      showInfo("Đã thêm ví " + name);
    } catch (RuntimeException e) {
      showError(e.getMessage());
    }
  }

  private static Wallet createWallet(WalletType type, String name, double balance) {
    return switch (type) {
      case CASH -> new CashWallet(name, balance);
      case BANK -> new BankAccount(name, balance, null, null);
      case EWALLET -> new EWallet(name, balance, null);
    };
  }

  // --- Làm mới hiển thị ---

  private void refreshAll() {
    refreshWalletViews();
    refreshCategoryChoices();
    refreshTransactionTable();
    refreshSummary();
  }

  private void refreshWalletViews() {
    List<Wallet> currentWallets = manager.getWallets();
    Wallet selected = walletBox.getValue();
    walletList.getItems().setAll(currentWallets);
    walletBox.getItems().setAll(currentWallets);
    if (selected != null && currentWallets.contains(selected)) {
      walletBox.setValue(selected);
    } else if (!currentWallets.isEmpty()) {
      walletBox.setValue(currentWallets.get(0));
    }
  }

  /** Chỉ hiện những danh mục cùng loại với giao dịch đang soạn. */
  private void refreshCategoryChoices() {
    TransactionType type = typeBox.getValue();
    List<Category> matching = new ArrayList<>();
    for (Category category : manager.getCategories()) {
      if (category.getType() == type) {
        matching.add(category);
      }
    }
    Category selected = categoryBox.getValue();
    categoryBox.getItems().setAll(matching);
    if (selected != null && matching.contains(selected)) {
      categoryBox.setValue(selected);
    } else if (!matching.isEmpty()) {
      categoryBox.setValue(matching.get(0));
    }
  }

  private void refreshTransactionTable() {
    if (manager == null) {
      return;
    }
    transactionTable.getItems().setAll(manager.searchTransactions(searchField.getText()));
  }

  private void refreshSummary() {
    summaryLabel.setText(manager.monthlySummary());
    totalBalanceLabel.setText(String.format("Tổng số dư: %,.0f VND", manager.totalWalletBalance()));
  }

  private void onTypeChanged() {
    boolean income = typeBox.getValue() == TransactionType.INCOME;
    detailField.setPromptText(income ? "Nguồn thu" : "Phương thức thanh toán");
    if (manager != null) {
      refreshCategoryChoices();
    }
  }

  /** Lần chạy đầu chưa có danh mục nào thì tạo sẵn bộ cơ bản cho người dùng. */
  private void seedDefaultCategories() {
    if (!manager.getCategories().isEmpty()) {
      return;
    }
    manager.addCategory(new Category("Ăn uống", TransactionType.EXPENSE));
    manager.addCategory(new Category("Đi lại", TransactionType.EXPENSE));
    manager.addCategory(new Category("Mua sắm", TransactionType.EXPENSE));
    manager.addCategory(new Category("Hoá đơn", TransactionType.EXPENSE));
    manager.addCategory(new Category("Lương", TransactionType.INCOME));
    manager.addCategory(new Category("Thưởng", TransactionType.INCOME));
  }

  /** Sinh mã chưa dùng, dạng GD1, GD2, ... */
  private String nextTransactionId() {
    int index = manager.getTransactions().size() + 1;
    while (manager.findTransaction("GD" + index) != null) {
      index++;
    }
    return "GD" + index;
  }

  // --- Tiện ích nhỏ ---

  private void showInfo(String message) {
    statusLabel.setStyle("-fx-text-fill: #147d64;");
    statusLabel.setText(message);
  }

  private void showError(String message) {
    statusLabel.setStyle("-fx-text-fill: #cf1124;");
    statusLabel.setText(message == null ? "Có lỗi xảy ra" : message);
  }

  private static ObservableValue<String> cellText(String value) {
    return new ReadOnlyStringWrapper(value == null ? "" : value);
  }

  private static String typeLabel(TransactionType type) {
    return type == TransactionType.INCOME ? "Thu" : "Chi";
  }

  private static String walletTypeLabel(WalletType type) {
    return switch (type) {
      case CASH -> "Tiền mặt";
      case BANK -> "Ngân hàng";
      case EWALLET -> "Ví điện tử";
    };
  }

  /** ComboBox chỉ cần hiển thị, không cần chuyển ngược từ chuỗi về enum. */
  private static <T> StringConverter<T> converter(java.util.function.Function<T, String> toLabel) {
    return new StringConverter<>() {
      @Override
      public String toString(T value) {
        return value == null ? "" : toLabel.apply(value);
      }

      @Override
      public T fromString(String text) {
        throw new UnsupportedOperationException("Không hỗ trợ nhập tay");
      }
    };
  }

  private static <T> T required(T value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private static String blankToNull(String text) {
    return text == null || text.isBlank() ? null : text.trim();
  }

  /** Chấp nhận người dùng gõ dấu phân cách kiểu 1 500 000 hoặc 1,500,000. */
  private static double parseMoney(String text, String fieldName) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("Hãy nhập " + fieldName);
    }
    String cleaned = text.trim().replace(" ", "").replace(",", "");
    try {
      return Double.parseDouble(cleaned);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Không đọc được " + fieldName + ": " + text);
    }
  }

  private static double parseOptionalMoney(String text) {
    return text == null || text.isBlank() ? 0 : parseMoney(text, "số dư");
  }
}
