package com.example.personalexpensemanager.manager;

import com.example.personalexpensemanager.enums.TransactionType;
import com.example.personalexpensemanager.model.Budget;
import com.example.personalexpensemanager.model.Category;
import com.example.personalexpensemanager.model.Transaction;
import com.example.personalexpensemanager.model.Wallet;
import com.example.personalexpensemanager.storage.Storage;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
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
  private Storage<Transaction> transactionStorage;
  private Storage<Wallet> walletStorage;

  /** Tạo manager chưa gắn Storage; phải set storage trước khi save/load. */
  public ExpenseManager() {
  }

  public ExpenseManager(Storage<Transaction> transactionStorage, Storage<Wallet> walletStorage) {
    this.transactionStorage = transactionStorage;
    this.walletStorage = walletStorage;
  }

  // --- Transaction ---

  public void addTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Giao dịch không được để trống");
    }
    if (findTransaction(transaction.getId()) != null) {
      throw new IllegalArgumentException("Đã tồn tại giao dịch có mã " + transaction.getId());
    }
    transactions.add(transaction);
  }

  public void removeTransaction(String id) {
    transactions.removeIf(tx -> tx.getId().equals(id));
  }

  /** Thay giao dịch cũ bằng bản mới có cùng mã. */
  public void updateTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Giao dịch không được để trống");
    }
    for (int i = 0; i < transactions.size(); i++) {
      if (transactions.get(i).getId().equals(transaction.getId())) {
        transactions.set(i, transaction);
        return;
      }
    }
    throw new IllegalArgumentException("Không tìm thấy giao dịch có mã " + transaction.getId());
  }

  public Transaction findTransaction(String id) {
    if (id == null) {
      return null;
    }
    for (Transaction tx : transactions) {
      if (tx.getId().equals(id)) {
        return tx;
      }
    }
    return null;
  }

  // --- Wallet ---

  public void addWallet(Wallet wallet) {
    if (wallet == null) {
      throw new IllegalArgumentException("Ví không được để trống");
    }
    if (findWallet(wallet.getName()) != null) {
      throw new IllegalArgumentException("Đã tồn tại ví tên " + wallet.getName());
    }
    wallets.add(wallet);
  }

  /** Không cho xoá ví đang được giao dịch tham chiếu, tránh để lại tham chiếu treo. */
  public void removeWallet(String name) {
    Wallet wallet = findWallet(name);
    if (wallet == null) {
      return;
    }
    for (Transaction tx : transactions) {
      if (tx.getWallet() == wallet) {
        throw new IllegalStateException("Ví " + wallet.getName() + " đang được dùng bởi giao dịch "
                + tx.getId());
      }
    }
    wallets.remove(wallet);
  }

  public Wallet findWallet(String name) {
    if (name == null) {
      return null;
    }
    for (Wallet wallet : wallets) {
      if (wallet.getName().equalsIgnoreCase(name.trim())) {
        return wallet;
      }
    }
    return null;
  }

  // --- Category ---

  public void addCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Danh mục không được để trống");
    }
    if (categories.contains(category)) {
      throw new IllegalArgumentException("Đã tồn tại danh mục " + category);
    }
    categories.add(category);
  }

  /** Không cho xoá danh mục đang được giao dịch hoặc ngân sách tham chiếu. */
  public void removeCategory(String name) {
    Category category = findCategory(name);
    if (category == null) {
      return;
    }
    for (Transaction tx : transactions) {
      if (tx.getCategory().equals(category)) {
        throw new IllegalStateException("Danh mục " + category + " đang được dùng bởi giao dịch "
                + tx.getId());
      }
    }
    if (budgets.containsKey(category)) {
      throw new IllegalStateException("Danh mục " + category + " đang có ngân sách, hãy xoá ngân sách trước");
    }
    categories.remove(category);
  }

  public Category findCategory(String name) {
    if (name == null) {
      return null;
    }
    for (Category category : categories) {
      if (category.getName().equalsIgnoreCase(name.trim())) {
        return category;
      }
    }
    return null;
  }

  // --- Budget ---

  public void setBudget(Category category, Budget budget) {
    if (category == null || budget == null) {
      throw new IllegalArgumentException("Danh mục và ngân sách không được để trống");
    }
    if (!category.equals(budget.getCategory())) {
      throw new IllegalArgumentException("Ngân sách này thuộc danh mục " + budget.getCategory()
              + ", không phải " + category);
    }
    budgets.put(category, budget);
  }

  public Budget getBudget(Category category) {
    return budgets.get(category);
  }

  // --- Báo cáo ---

  /** Tóm tắt thu/chi của tháng hiện tại. */
  public String monthlySummary() {
    return monthlySummary(YearMonth.now());
  }

  /** Tóm tắt thu/chi của tháng chỉ định. */
  public String monthlySummary(YearMonth month) {
    if (month == null) {
      throw new IllegalArgumentException("Tháng không được để trống");
    }
    double income = 0;
    double expense = 0;
    for (Transaction tx : transactions) {
      if (!YearMonth.from(tx.getDate()).equals(month)) {
        continue;
      }
      if (tx.getType() == TransactionType.INCOME) {
        income += tx.getAmount();
      } else {
        expense += tx.getAmount();
      }
    }
    return String.format("Tháng %s | Thu: %,.0f VND | Chi: %,.0f VND | Còn lại: %,.0f VND",
            month, income, expense, income - expense);
  }

  /**
   * Thống kê tổng số tiền theo từng danh mục. Giá trị luôn dương (tổng chi với
   * danh mục chi, tổng thu với danh mục thu) nên dùng trực tiếp được cho
   * {@link Budget#isExceeded(double)}.
   */
  public Map<Category, Double> statisticsByCategory() {
    Map<Category, Double> result = new HashMap<>();
    for (Transaction tx : transactions) {
      result.merge(tx.getCategory(), tx.getAmount(), Double::sum);
    }
    return result;
  }

  // --- Persistence ---

  /** Ghi cả ví lẫn giao dịch, dùng lúc thoát app. */
  public void saveData(String transactionPath, String walletPath) throws IOException {
    saveWallets(walletPath);
    saveTransactions(transactionPath);
  }

  /** Gọi lúc khởi động app: nạp ví trước rồi tới giao dịch. */
  public void loadData(String transactionPath, String walletPath) throws IOException {
    loadWallets(walletPath);
    loadTransactions(transactionPath);
  }

  public void saveTransactions(String path) throws IOException {
    requireTransactionStorage().save(new ArrayList<>(transactions), path);
  }

  /**
   * Nạp giao dịch từ file, thay thế danh sách hiện tại. Đi qua addTransaction để
   * file hỏng (trùng mã giao dịch) bị phát hiện ngay thay vì lọt vào bộ nhớ.
   */
  public void loadTransactions(String path) throws IOException {
    List<Transaction> loaded = requireTransactionStorage().load(path);
    transactions.clear();
    for (Transaction tx : loaded) {
      addTransaction(tx);
    }
    resolveWallets();
    registerLoadedCategories();
  }

  public void saveWallets(String path) throws IOException {
    requireWalletStorage().save(new ArrayList<>(wallets), path);
  }

  /**
   * Nạp ví từ file, thay thế danh sách hiện tại. Gọi resolveWallets ở cuối để
   * các giao dịch đã nạp trước đó cũng trỏ sang đúng đối tượng ví mới.
   */
  public void loadWallets(String path) throws IOException {
    List<Wallet> loaded = requireWalletStorage().load(path);
    wallets.clear();
    for (Wallet wallet : loaded) {
      addWallet(wallet);
    }
    resolveWallets();
  }

  /** Gán lại đúng đối tượng Wallet thật cho từng Transaction, thay vì
   *  wallet tạm (placeholder) mà TransactionJsonAdapter tạo lúc deserialize. */
  private void resolveWallets() {
    for (Transaction tx : transactions) {
      Wallet realWallet = findWallet(tx.getWallet().getName());
      if (realWallet != null) {
        tx.setWallet(realWallet);
      }
    }
  }

  /** File giao dịch mang theo danh mục của nó, nạp luôn vào danh sách danh mục. */
  private void registerLoadedCategories() {
    for (Transaction tx : transactions) {
      if (!categories.contains(tx.getCategory())) {
        categories.add(tx.getCategory());
      }
    }
  }

  private Storage<Transaction> requireTransactionStorage() {
    if (transactionStorage == null) {
      throw new IllegalStateException("Chưa cấu hình Storage cho giao dịch");
    }
    return transactionStorage;
  }

  private Storage<Wallet> requireWalletStorage() {
    if (walletStorage == null) {
      throw new IllegalStateException("Chưa cấu hình Storage cho ví");
    }
    return walletStorage;
  }

  public Storage<Transaction> getTransactionStorage() {
    return transactionStorage;
  }

  public void setTransactionStorage(Storage<Transaction> transactionStorage) {
    if (transactionStorage == null) {
      throw new IllegalArgumentException("Storage cho giao dịch không được để trống");
    }
    this.transactionStorage = transactionStorage;
  }

  public Storage<Wallet> getWalletStorage() {
    return walletStorage;
  }

  public void setWalletStorage(Storage<Wallet> walletStorage) {
    if (walletStorage == null) {
      throw new IllegalArgumentException("Storage cho ví không được để trống");
    }
    this.walletStorage = walletStorage;
  }

  // Các getter dưới đây trả về view chỉ đọc: muốn thêm/xoá thì phải đi qua
  // addTransaction/addWallet/... để không bỏ qua bước kiểm tra.

  public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }

  public List<Wallet> getWallets() {
    return Collections.unmodifiableList(wallets);
  }

  public List<Category> getCategories() {
    return Collections.unmodifiableList(categories);
  }

  public Map<Category, Budget> getBudgets() {
    return Collections.unmodifiableMap(budgets);
  }
}
