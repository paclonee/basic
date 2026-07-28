package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/**
 * Lớp trừu tượng cho ví / tài khoản.
 * Lớp con có thể override {@link #withdraw(double)} (ví dụ phí ngân hàng).
 */
public abstract class Wallet {

  private String name;
  private double balance;

  protected Wallet(String name, double balance) {
    if (balance < 0) {
      throw new IllegalArgumentException("Balance cannot be negative");
    }
    this.name = name;
    this.balance = balance;
  }

  public void deposit(double amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Deposit amount cannot be negative");
    }
    balance += amount;
  }

  /**
   * Rút tiền. Hành vi có thể khác nhau giữa CashWallet và BankAccount
   * (phí, hạn mức, ...).
   */
  public void withdraw(double amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Withdraw amount cannot be negative");
    }
    if (amount > balance) {
      throw new IllegalStateException("Insufficient balance in wallet: " + name);
    }
    balance -= amount;
  }

  /** Trả về loại ví (CASH / BANK / EWALLET). */
  public abstract WalletType getWalletType();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getBalance() {
    return balance;
  }

  /** Đặt số dư; cần validate không âm. */
  //public void setBalance(double balance) {
    // TODO: chặn balance < 0
  //}
}
