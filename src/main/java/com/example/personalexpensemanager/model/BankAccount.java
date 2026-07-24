package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.WalletType;

/**
 * Tài khoản ngân hàng.
 * Có thể override withdraw để áp dụng phí / hạn mức.
 */
public class BankAccount extends Wallet {

  private String bankName;
  private String accountNumber;

  public BankAccount() {
    // TODO
  }

  public BankAccount(String name, double balance, String bankName, String accountNumber) {
    // TODO
  }

  @Override
  public WalletType getWalletType() {
    // TODO: return WalletType.BANK
    return null;
  }

  @Override
  public void withdraw(double amount) {
    // TODO: có thể trừ thêm phí giao dịch / kiểm tra hạn mức
  }

  public String getBankName() {
    return bankName;
  }

  public void setBankName(String bankName) {
    // TODO
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    // TODO
  }
}
