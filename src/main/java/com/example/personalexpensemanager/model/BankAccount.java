package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/**
 * Tài khoản ngân hàng.
 * Có thể override withdraw để áp dụng phí / hạn mức.
 */
public class BankAccount extends Wallet {

  private String bankName;
  private String accountNumber;
  private double transactionFee;

  public BankAccount() {
    super("Tài khoản ngân hàng", 0);
  }

  public BankAccount(String name, double balance, String bankName, String accountNumber) {
    super(name, balance);
    this.bankName = bankName;
    this.accountNumber = accountNumber;
  }

  @Override
  public WalletType getWalletType() {
    return WalletType.BANK;
  }

  /**
   * Rút tiền có kèm phí giao dịch: số bị trừ vào số dư là {@code amount + phí}.
   * Phí mặc định bằng 0 nên hành vi giống ví thường cho tới khi được cấu hình
   * qua {@link #setTransactionFee(double)}.
   */
  @Override
  public void withdraw(double amount) {
    super.withdraw(amount + transactionFee);
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    this.bankName = bankName;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public double getTransactionFee() {
    return transactionFee;
  }

  public void setTransactionFee(double transactionFee) {
    if (transactionFee < 0) {
      throw new IllegalArgumentException("Phí giao dịch không được âm");
    }
    this.transactionFee = transactionFee;
  }
}
