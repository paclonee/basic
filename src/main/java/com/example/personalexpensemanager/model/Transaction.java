package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.time.LocalDate;

/**
 * Lớp trừu tượng cho mọi giao dịch (thu/chi).
 * Các lớp con triển khai kiểu giao dịch và số tiền có dấu.
 */
public abstract class Transaction {

  private String id;
  private double amount;
  private LocalDate date;
  private String note;
  private Category category;
  private Wallet wallet;

  protected Transaction() {
    // TODO: khởi tạo mặc định
  }

  protected Transaction(
      String id,
      double amount,
      LocalDate date,
      String note,
      Category category,
      Wallet wallet) {
    // TODO: gán field + kiểm tra amount >= 0
  }

  /** Trả về loại giao dịch (INCOME / EXPENSE). */
  public abstract TransactionType getType();

  /**
   * Số tiền có dấu: dương nếu thu, âm nếu chi.
   * Phục vụ tính tổng số dư / báo cáo.
   */
  public abstract double getSignedAmount();

  /** In thông tin giao dịch ra console (đa hình theo lớp con). */
  public void printInfo() {
    // TODO: in thông tin chung; lớp con có thể override
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    // TODO
  }

  public double getAmount() {
    return amount;
  }

  /** Đặt số tiền; cần validate không âm. */
  public void setAmount(double amount) {
    // TODO: chặn amount < 0
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    // TODO
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    // TODO
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    // TODO
  }

  public Wallet getWallet() {
    return wallet;
  }

  public void setWallet(Wallet wallet) {
    // TODO
  }
}
