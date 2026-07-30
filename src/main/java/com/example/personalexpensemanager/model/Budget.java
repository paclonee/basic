package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.Period;

/** Ngân sách theo danh mục và chu kỳ. */
public class Budget {

  private Category category;
  private double limit;
  private Period period;

  public Budget(Category category, double limit, Period period) {
    this.category = requireCategory(category);
    this.limit = requireLimit(limit);
    this.period = requirePeriod(period);
  }

  /** Kiểm tra đã vượt hạn mức khi đã chi {@code spent}. */
  public boolean isExceeded(double spent) {
    return spent > limit;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = requireCategory(category);
  }

  public double getLimit() {
    return limit;
  }

  public void setLimit(double limit) {
    this.limit = requireLimit(limit);
  }

  public Period getPeriod() {
    return period;
  }

  public void setPeriod(Period period) {
    this.period = requirePeriod(period);
  }

  @Override
  public String toString() {
    return String.format("%s: %,.0f VND / %s", category, limit, period);
  }

  private static Category requireCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Danh mục của ngân sách không được để trống");
    }
    return category;
  }

  private static double requireLimit(double limit) {
    if (limit < 0) {
      throw new IllegalArgumentException("Hạn mức không được âm");
    }
    return limit;
  }

  private static Period requirePeriod(Period period) {
    if (period == null) {
      throw new IllegalArgumentException("Chu kỳ không được để trống");
    }
    return period;
  }
}
