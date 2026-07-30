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

  protected Transaction(String id, double amount, LocalDate date, String note,
                        Category category, Wallet wallet) {
    this.id = requireId(id);
    this.amount = requireAmount(amount);
    this.date = requireDate(date);
    this.note = note;
    this.category = requireCategory(category);
    this.wallet = requireWallet(wallet);
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
    System.out.printf("[%s] %s | %,.0f VND | %s | %s%n",
            getType(), date, getSignedAmount(), category, note == null ? "" : note);
  }

  public String getId() { return id; }

  public double getAmount() { return amount; }

  /** Đặt số tiền; số tiền giao dịch phải lớn hơn 0. */
  public void setAmount(double amount) {
    this.amount = requireAmount(amount);
  }

  public LocalDate getDate() { return date; }

  public void setDate(LocalDate date) {
    this.date = requireDate(date);
  }

  public String getNote() { return note; }

  public void setNote(String note) { this.note = note; }

  public Category getCategory() { return category; }

  public void setCategory(Category category) {
    this.category = requireCategory(category);
  }

  public Wallet getWallet() { return wallet; }

  public void setWallet(Wallet wallet) {
    this.wallet = requireWallet(wallet);
  }

  /** Id là khoá để tìm / sửa / xoá giao dịch nên không được rỗng. */
  private static String requireId(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Mã giao dịch không được để trống");
    }
    return id.trim();
  }

  private static double requireAmount(double amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0");
    }
    return amount;
  }

  private static LocalDate requireDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Ngày giao dịch không được để trống");
    }
    return date;
  }

  private static Category requireCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Danh mục không được để trống");
    }
    return category;
  }

  private static Wallet requireWallet(Wallet wallet) {
    if (wallet == null) {
      throw new IllegalArgumentException("Ví không được để trống");
    }
    return wallet;
  }
}
