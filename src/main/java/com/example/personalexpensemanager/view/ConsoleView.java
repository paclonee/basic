package com.example.personalexpensemanager.view;

import com.example.personalexpensemanager.manager.ExpenseManager;

/**
 * Giao diện dòng lệnh (CLI).
 * Tách hiển thị khỏi logic nghiệp vụ trong {@link ExpenseManager}.
 */
public class ConsoleView {

  private final ExpenseManager expenseManager;

  public ConsoleView(ExpenseManager expenseManager) {
    this.expenseManager = expenseManager;
  }

  /** Vòng lặp menu chính trên console. */
  public void start() {
    // TODO: hiển thị menu, đọc lựa chọn, gọi ExpenseManager
  }

  private void showMenu() {
    // TODO
  }
}
