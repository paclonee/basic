package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.time.LocalDate;

/** Giao dịch thu nhập. */
public class Income extends Transaction {

  private String source;

  public Income(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String source) {
    super(id, amount, date, note, category, wallet);
    this.source = source;
  }

  @Override
  public TransactionType getType() {
    return TransactionType.INCOME;
  }

  @Override
  public double getSignedAmount() {
    return getAmount();
  }

  @Override
  public void printInfo() {
    super.printInfo();
    System.out.println("       Nguồn thu: " + (source == null ? "-" : source));
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }
}
