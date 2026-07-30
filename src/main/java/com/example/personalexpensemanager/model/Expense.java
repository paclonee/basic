package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.time.LocalDate;

/** Giao dịch chi tiêu. */
public class Expense extends Transaction {

  private String paymentMethod;

  public Expense(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet,
      String paymentMethod) {
    super(id, amount, date, note, category, wallet);
    this.paymentMethod = paymentMethod;
  }

  @Override
  public TransactionType getType() {
    return TransactionType.EXPENSE;
  }

  @Override
  public double getSignedAmount() {
    return -getAmount();
  }

  @Override
  public void printInfo() {
    super.printInfo();
    System.out.println("       Thanh toán: " + (paymentMethod == null ? "-" : paymentMethod));
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }
}
