package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.Period;

/** Ngân sách theo danh mục và chu kỳ. */
public class Budget {

  private Category category;
  private double limit;
  private Period period;

  public Budget() {
    // TODO
  }

  public Budget(Category category, double limit, Period period) {
    // TODO: validate limit >= 0
  }

  /** Kiểm tra đã vượt hạn mức khi đã chi {@code spent}. */
  public boolean isExceeded(double spent) {
    // TODO: return spent > limit
    return false;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    // TODO
  }

  public double getLimit() {
    return limit;
  }

  public void setLimit(double limit) {
    // TODO: chặn limit < 0
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    // TODO
  }
}
