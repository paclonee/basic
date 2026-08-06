package com.example.personalexpensemanager.manager;

import java.time.YearMonth;

/**
 * Kết quả thống kê thu / chi của một tháng.
 * Giữ số liệu thô để giao diện tự quyết định cách hiển thị; {@link #toString()}
 * chỉ dành cho việc in nhanh ra console.
 */
public record MonthlyReport(YearMonth month, double income, double expense) {

  public MonthlyReport {
    if (month == null) {
      throw new IllegalArgumentException("Tháng không được để trống");
    }
    if (income < 0 || expense < 0) {
      throw new IllegalArgumentException("Tổng thu và tổng chi không được âm");
    }
  }

  /** Chênh lệch thu - chi trong tháng; âm nghĩa là tiêu nhiều hơn kiếm. */
  public double balance() {
    return income - expense;
  }

  @Override
  public String toString() {
    return String.format("Tháng %s | Thu: %,.0f VND | Chi: %,.0f VND | Còn lại: %,.0f VND",
            month, income, expense, balance());
  }
}
