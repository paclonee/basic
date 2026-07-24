package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.Period;
import java.time.LocalDate;

/** Chi tiêu định kỳ (hàng ngày / tuần / tháng / năm). */
public class RecurringExpense extends Expense {

  private Period period;

  public RecurringExpense() {
    // TODO
  }

  public RecurringExpense(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String paymentMethod,
      Period period) {
    // TODO
  }

  /** Tính ngày đến hạn tiếp theo dựa trên {@link #period}. */
  public LocalDate nextDueDate() {
    // TODO: cộng chu kỳ vào date hiện tại
    return null;
  }

  @Override
  public void printInfo() {
    // TODO: in thêm chu kỳ
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    // TODO
  }
}
