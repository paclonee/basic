package com.example.personalexpensemanager.manager;

import com.example.personalexpensemanager.model.Budget;
import com.example.personalexpensemanager.model.Category;
import com.example.personalexpensemanager.model.Transaction;
import com.example.personalexpensemanager.model.Wallet;
import com.example.personalexpensemanager.storage.Storage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp điều phối nghiệp vụ: quản lý giao dịch, ví, danh mục, ngân sách
 * và persistence qua {@link Storage}.
 */
public class ExpenseManager {

  private final List<Transaction> transactions = new ArrayList<>();
  private final List<Wallet> wallets = new ArrayList<>();
  private final List<Category> categories = new ArrayList<>();
  private final Map<Category, Budget> budgets = new HashMap<>();
  private Storage storage;

  public ExpenseManager() {
    // TODO
  }

  public ExpenseManager(Storage storage) {
    // TODO
  }

  // --- Transaction ---

  public void addTransaction(Transaction transaction) {
    // TODO
  }

  public void removeTransaction(String id) {
    // TODO
  }

  public void updateTransaction(Transaction transaction) {
    // TODO
  }

  public Transaction findTransaction(String id) {
    // TODO
    return null;
  }

  // --- Wallet ---

  public void addWallet(Wallet wallet) {
    // TODO
  }

  public void removeWallet(String name) {
    // TODO
  }

  public Wallet findWallet(String name) {
    // TODO
    return null;
  }

  // --- Category ---

  public void addCategory(Category category) {
    // TODO
  }

  public void removeCategory(String name) {
    // TODO
  }

  public Category findCategory(String name) {
    // TODO
    return null;
  }

  // --- Budget ---

  public void setBudget(Category category, Budget budget) {
    // TODO
  }

  public Budget getBudget(Category category) {
    // TODO
    return null;
  }

  // --- Báo cáo ---

  /** Tóm tắt thu/chi theo tháng hiện tại (hoặc tháng chỉ định — tùy triển khai). */
  public String monthlySummary() {
    // TODO: tổng thu, tổng chi, số dư theo tháng
    return null;
  }

  /** Thống kê tổng chi (hoặc thu) theo từng danh mục. */
  public Map<Category, Double> statisticsByCategory() {
    // TODO
    return Map.of();
  }

  // --- Persistence ---

  public void save(String path) {
    // TODO: ủy quyền storage.save
  }

  public void load(String path) {
    // TODO: ủy quyền storage.load rồi cập nhật danh sách
  }

  public Storage getStorage() {
    return storage;
  }

  public void setStorage(Storage storage) {
    // TODO
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public List<Wallet> getWallets() {
    return wallets;
  }

  public List<Category> getCategories() {
    return categories;
  }

  public Map<Category, Budget> getBudgets() {
    return budgets;
  }
}
