package com.example.personalexpensemanager.view;

import com.example.personalexpensemanager.manager.ExpenseManager;

/**
 * Lớp cầu nối / wrapper cho giao diện JavaFX.
 * Logic UI chi tiết nằm ở FXML + controller.
 */
public class AppView {

  private final ExpenseManager expenseManager;

  public AppView(ExpenseManager expenseManager) {
    this.expenseManager = expenseManager;
  }

  /** Khởi tạo / gắn dữ liệu cho scene JavaFX (gọi từ Application). */
  public void bind() {
    // TODO: gắn ExpenseManager với controller / scene
  }

  public ExpenseManager getExpenseManager() {
    return expenseManager;
  }
}
