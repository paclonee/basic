package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.time.LocalDate;

/** Giao dịch thu nhập. */
public class Income extends Transaction {

  private String source;

  public Income() {
    // TODO
  }

  public Income(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String source) {
    // TODO
  }

  @Override
  public TransactionType getType() {
    // TODO: return TransactionType.INCOME
    return null;
  }

  @Override
  public double getSignedAmount() {
    // TODO: return +amount
    return 0;
  }

  @Override
  public void printInfo() {
    // TODO: in thêm nguồn thu
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    // TODO
  }
}
