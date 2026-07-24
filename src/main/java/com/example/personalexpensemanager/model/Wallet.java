package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/**
 * Lớp trừu tượng cho ví / tài khoản.
 * Lớp con có thể override {@link #withdraw(double)} (ví dụ phí ngân hàng).
 */
public abstract class Wallet {

  private String name;
  private double balance;

  protected Wallet() {
    // TODO
  }

  protected Wallet(String name, double balance) {
    // TODO: validate balance >= 0
  }

  public void deposit(double amount) {
    // TODO: cộng số dư nếu amount > 0
  }

  /**
   * Rút tiền. Hành vi có thể khác nhau giữa CashWallet và BankAccount
   * (phí, hạn mức, ...).
   */
  public void withdraw(double amount) {
    // TODO: trừ số dư nếu đủ tiền
  }

  /** Trả về loại ví (CASH / BANK / EWALLET). */
  public abstract WalletType getWalletType();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    // TODO
  }

  public double getBalance() {
    return balance;
  }

  /** Đặt số dư; cần validate không âm. */
  public void setBalance(double balance) {
    // TODO: chặn balance < 0
  }
}
