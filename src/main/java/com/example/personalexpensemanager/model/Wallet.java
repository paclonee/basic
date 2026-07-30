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
    this.name = requireValidName(name);
    this.balance = requireNonNegative(balance, "Số dư");
  }

  public void deposit(double amount) {
    balance += requirePositive(amount, "Số tiền nạp");
  }

  /**
   * Rút tiền. Hành vi có thể khác nhau giữa CashWallet và BankAccount
   * (phí, hạn mức, ...).
   */
  public void withdraw(double amount) {
    double value = requirePositive(amount, "Số tiền rút");
    if (value > balance) {
      throw new IllegalStateException("Số dư không đủ trong ví: " + name);
    }
    balance -= value;
  }

  /** Trả về loại ví (CASH / BANK / EWALLET). */
  public abstract WalletType getWalletType();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireValidName(name);
  }

  public double getBalance() {
    return balance;
  }

  // Cố tình không mở setBalance ra ngoài: số dư chỉ được đổi qua deposit/withdraw
  // để mọi thay đổi đều đi qua validate. Lớp con muốn số dư ban đầu thì
  // truyền qua constructor.

  @Override
  public String toString() {
    return String.format("%s: %,.0f VND", name, balance);
  }

  /** Tên ví là khoá tra cứu ví trong ExpenseManager nên không được rỗng. */
  private static String requireValidName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Tên ví không được để trống");
    }
    return name.trim();
  }

  private static double requireNonNegative(double value, String field) {
    if (value < 0) {
      throw new IllegalArgumentException(field + " không được âm");
    }
    return value;
  }

  private static double requirePositive(double value, String field) {
    if (value <= 0) {
      throw new IllegalArgumentException(field + " phải lớn hơn 0");
    }
    return value;
  }
}
