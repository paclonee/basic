package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.time.LocalDate;

/** Giao dịch chi tiêu. */
public class Expense extends Transaction {

  private String paymentMethod;

  public Expense() {
    // TODO
  }

  public Expense(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String paymentMethod) {
    // TODO
  }

  @Override
  public TransactionType getType() {
    // TODO: return TransactionType.EXPENSE
    return null;
  }

  @Override
  public double getSignedAmount() {
    // TODO: return -amount
    return 0;
  }

  @Override
  public void printInfo() {
    // TODO: in thêm phương thức thanh toán
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    // TODO
  }
}
