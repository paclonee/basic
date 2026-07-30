package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.Period;
import java.time.LocalDate;

/** Chi tiêu định kỳ (hàng ngày / tuần / tháng / năm). */
public class RecurringExpense extends Expense {

  private Period period;

  public RecurringExpense(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String paymentMethod,
      Period period) {
    super(id, amount, date, note, category, wallet, paymentMethod);
    this.period = requirePeriod(period);
  }

  /** Tính ngày đến hạn tiếp theo dựa trên {@link #period}. */
  public LocalDate nextDueDate() {
    LocalDate from = getDate();
    return switch (period) {
      case DAILY -> from.plusDays(1);
      case WEEKLY -> from.plusWeeks(1);
      case MONTHLY -> from.plusMonths(1);
      case YEARLY -> from.plusYears(1);
    };
  }

  @Override
  public void printInfo() {
    super.printInfo();
    System.out.println("       Định kỳ: " + period + " | đến hạn tiếp: " + nextDueDate());
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = requirePeriod(period);
  }

  private static Period requirePeriod(Period period) {
    if (period == null) {
      throw new IllegalArgumentException("Chu kỳ không được để trống");
    }
    return period;
  }
}
