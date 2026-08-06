package com.example.personalexpensemanager.manager;

import com.example.personalexpensemanager.enums.Period;
import com.example.personalexpensemanager.enums.TransactionType;
import com.example.personalexpensemanager.model.Budget;
import com.example.personalexpensemanager.model.Category;
import com.example.personalexpensemanager.model.Transaction;
import com.example.personalexpensemanager.model.Wallet;
import com.example.personalexpensemanager.storage.Storage;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

/**
 * Lớp điều phối nghiệp vụ: quản lý giao dịch, ví, danh mục, ngân sách
 * và persistence qua {@link Storage}.
 *
 * <p>Số dư ví luôn đi kèm danh sách giao dịch: thêm một khoản chi thì tiền bị
 * trừ khỏi ví ngay, xoá thì hoàn lại, sửa thì hoàn khoản cũ rồi áp khoản mới.
 * Nhờ vậy tổng số dư mọi ví luôn giải thích được bằng các giao dịch đã ghi.
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

  // --- Giao dịch: Thêm / Xoá / Sửa / Tìm ---

  /**
   * Thêm giao dịch và cập nhật số dư ví. Nếu ví không đủ tiền cho khoản chi thì
   * ném lỗi và danh sách giao dịch giữ nguyên như trước khi gọi.
   */
  public void addTransaction(Transaction transaction) {
    requireNewTransaction(transaction);
    applyToWallet(transaction);
    transactions.add(transaction);
  }

  /** Xoá giao dịch và hoàn lại phần tiền nó đã cộng / trừ vào ví. */
  public void removeTransaction(String id) {
    Transaction transaction = findTransaction(id);
    if (transaction == null) {
      return;
    }
    revertFromWallet(transaction);
    transactions.remove(transaction);
  }

  /**
   * Thay giao dịch cũ bằng bản mới cùng mã: hoàn tác ảnh hưởng của bản cũ lên ví
   * rồi áp bản mới. Nếu bản mới làm ví âm thì bản cũ được khôi phục nguyên trạng.
   */
  public void updateTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Giao dịch không được để trống");
    }
    int index = indexOfTransaction(transaction.getId());
    if (index < 0) {
      throw new IllegalArgumentException("Không tìm thấy giao dịch có mã " + transaction.getId());
    }
    Transaction current = transactions.get(index);
    revertFromWallet(current);
    try {
      applyToWallet(transaction);
    } catch (RuntimeException e) {
      applyToWallet(current);
      throw e;
    }
    transactions.set(index, transaction);
  }

  public Transaction findTransaction(String id) {
    int index = indexOfTransaction(id);
    return index < 0 ? null : transactions.get(index);
  }

  /**
   * Cộng / trừ tiền ví theo đúng số tiền của giao dịch. Dùng withdrawExact thay vì
   * withdraw để phí của {@code BankAccount} không làm số dư lệch khỏi tổng giao
   * dịch; muốn tính phí thì ghi phí thành một giao dịch chi riêng.
   */
  private void applyToWallet(Transaction transaction) {
    Wallet wallet = transaction.getWallet();
    if (transaction.getType() == TransactionType.INCOME) {
      wallet.deposit(transaction.getAmount());
    } else {
      wallet.withdrawExact(transaction.getAmount());
    }
  }

  /** Phép nghịch đảo chính xác của {@link #applyToWallet(Transaction)}. */
  private void revertFromWallet(Transaction transaction) {
    Wallet wallet = transaction.getWallet();
    if (transaction.getType() == TransactionType.INCOME) {
      wallet.withdrawExact(transaction.getAmount());
    } else {
      wallet.deposit(transaction.getAmount());
    }
  }

  private void requireNewTransaction(Transaction transaction) {
    if (transaction == null) {
      throw new IllegalArgumentException("Giao dịch không được để trống");
    }
    if (findTransaction(transaction.getId()) != null) {
      throw new IllegalArgumentException("Đã tồn tại giao dịch có mã " + transaction.getId());
    }
  }

  private int indexOfTransaction(String id) {
    if (id == null) {
      return -1;
    }
    for (int i = 0; i < transactions.size(); i++) {
      if (transactions.get(i).getId().equals(id)) {
        return i;
      }
    }
    return -1;
  }

  // --- Ví: Thêm / Xoá / Sửa / Tìm ---

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

  /** Đổi tên ví. Giao dịch giữ tham chiếu tới ví nên tự động theo tên mới. */
  public void renameWallet(String oldName, String newName) {
    Wallet wallet = findWallet(oldName);
    if (wallet == null) {
      throw new IllegalArgumentException("Không tìm thấy ví tên " + oldName);
    }
    Wallet duplicate = findWallet(newName);
    if (duplicate != null && duplicate != wallet) {
      throw new IllegalArgumentException("Đã tồn tại ví tên " + newName);
    }
    wallet.setName(newName);
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

  // --- Danh mục: Thêm / Xoá / Sửa / Tìm ---

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

  /**
   * Đổi tên danh mục. Phải gỡ ngân sách khỏi map trước khi đổi tên: tên nằm trong
   * hashCode của {@link Category} nên sửa tại chỗ sẽ làm entry cũ không tra lại được.
   */
  public void renameCategory(String oldName, String newName) {
    Category category = findCategory(oldName);
    if (category == null) {
      throw new IllegalArgumentException("Không tìm thấy danh mục " + oldName);
    }
    Category duplicate = findCategory(newName);
    if (duplicate != null && duplicate != category) {
      throw new IllegalArgumentException("Đã tồn tại danh mục " + newName);
    }
    Budget budget = budgets.remove(category);
    category.setName(newName);
    if (budget != null) {
      budgets.put(category, budget);
    }
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

  // --- Ngân sách ---

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

  public void removeBudget(Category category) {
    budgets.remove(category);
  }

  // --- Tìm kiếm ---

  /** Lọc giao dịch theo điều kiện bất kỳ; mọi hàm tìm kiếm bên dưới đều dùng lại hàm này. */
  public List<Transaction> filterTransactions(Predicate<Transaction> condition) {
    if (condition == null) {
      throw new IllegalArgumentException("Điều kiện lọc không được để trống");
    }
    List<Transaction> result = new ArrayList<>();
    for (Transaction tx : transactions) {
      if (condition.test(tx)) {
        result.add(tx);
      }
    }
    return result;
  }

  /**
   * Tìm theo từ khoá, khớp một phần và không phân biệt hoa thường, quét qua mã
   * giao dịch, ghi chú, tên danh mục và tên ví. Từ khoá rỗng trả về tất cả.
   */
  public List<Transaction> searchTransactions(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return new ArrayList<>(transactions);
    }
    String needle = keyword.trim().toLowerCase(Locale.ROOT);
    return filterTransactions(tx -> containsIgnoreCase(tx.getId(), needle)
            || containsIgnoreCase(tx.getNote(), needle)
            || containsIgnoreCase(tx.getCategory().getName(), needle)
            || containsIgnoreCase(tx.getWallet().getName(), needle));
  }

  public List<Transaction> findTransactionsByCategory(Category category) {
    if (category == null) {
      throw new IllegalArgumentException("Danh mục không được để trống");
    }
    return filterTransactions(tx -> tx.getCategory().equals(category));
  }

  /** Trả về danh sách rỗng nếu không có ví nào tên như vậy. */
  public List<Transaction> findTransactionsByWallet(String walletName) {
    if (walletName == null || walletName.isBlank()) {
      throw new IllegalArgumentException("Tên ví không được để trống");
    }
    String target = walletName.trim();
    return filterTransactions(tx -> tx.getWallet().getName().equalsIgnoreCase(target));
  }

  public List<Transaction> findTransactionsByType(TransactionType type) {
    if (type == null) {
      throw new IllegalArgumentException("Loại giao dịch không được để trống");
    }
    return filterTransactions(tx -> tx.getType() == type);
  }

  /** Lọc theo khoảng ngày, tính cả hai đầu mút; truyền null để bỏ giới hạn một phía. */
  public List<Transaction> findTransactionsByDateRange(LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
    }
    return filterTransactions(tx -> (from == null || !tx.getDate().isBefore(from))
            && (to == null || !tx.getDate().isAfter(to)));
  }

  public List<Transaction> findTransactionsByMonth(YearMonth month) {
    requireMonth(month);
    return filterTransactions(tx -> YearMonth.from(tx.getDate()).equals(month));
  }

  /** Lọc theo khoảng số tiền, tính cả hai đầu mút. */
  public List<Transaction> findTransactionsByAmountRange(double min, double max) {
    if (min < 0) {
      throw new IllegalArgumentException("Số tiền nhỏ nhất không được âm");
    }
    if (min > max) {
      throw new IllegalArgumentException("Số tiền nhỏ nhất phải không lớn hơn số tiền lớn nhất");
    }
    return filterTransactions(tx -> tx.getAmount() >= min && tx.getAmount() <= max);
  }

  private static boolean containsIgnoreCase(String value, String lowercaseNeedle) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(lowercaseNeedle);
  }

  // --- Thống kê thu chi ---

  public double totalIncome() {
    return sumAmount(TransactionType.INCOME, null);
  }

  public double totalIncome(YearMonth month) {
    return sumAmount(TransactionType.INCOME, requireMonth(month));
  }

  public double totalExpense() {
    return sumAmount(TransactionType.EXPENSE, null);
  }

  public double totalExpense(YearMonth month) {
    return sumAmount(TransactionType.EXPENSE, requireMonth(month));
  }

  /** Chênh lệch tổng thu - tổng chi trên toàn bộ dữ liệu. */
  public double netAmount() {
    return totalIncome() - totalExpense();
  }

  public double netAmount(YearMonth month) {
    requireMonth(month);
    return totalIncome(month) - totalExpense(month);
  }

  /** Tổng số dư đang có trong mọi ví. */
  public double totalWalletBalance() {
    double total = 0;
    for (Wallet wallet : wallets) {
      total += wallet.getBalance();
    }
    return total;
  }

  /** Báo cáo thu/chi của tháng hiện tại. */
  public MonthlyReport monthlyReport() {
    return monthlyReport(YearMonth.now());
  }

  public MonthlyReport monthlyReport(YearMonth month) {
    requireMonth(month);
    return new MonthlyReport(month, totalIncome(month), totalExpense(month));
  }

  /** Bản in nhanh của {@link #monthlyReport()} cho console. */
  public String monthlySummary() {
    return monthlyReport().toString();
  }

  public String monthlySummary(YearMonth month) {
    return monthlyReport(month).toString();
  }

  /**
   * Tổng số tiền theo từng danh mục trên toàn bộ dữ liệu. Giá trị luôn dương
   * (tổng chi với danh mục chi, tổng thu với danh mục thu) nên dùng trực tiếp
   * được cho {@link Budget#isExceeded(double)}.
   */
  public Map<Category, Double> statisticsByCategory() {
    return sumByCategory(null);
  }

  /** Như trên nhưng chỉ tính các giao dịch trong tháng chỉ định. */
  public Map<Category, Double> statisticsByCategory(YearMonth month) {
    return sumByCategory(requireMonth(month));
  }

  /** Chênh lệch thu - chi của từng ví; dương nghĩa là tiền vào nhiều hơn tiền ra. */
  public Map<Wallet, Double> statisticsByWallet() {
    Map<Wallet, Double> result = new LinkedHashMap<>();
    for (Wallet wallet : wallets) {
      result.put(wallet, 0.0);
    }
    for (Transaction tx : transactions) {
      result.merge(tx.getWallet(), tx.getSignedAmount(), Double::sum);
    }
    return result;
  }

  /** Chênh lệch thu - chi theo từng tháng, sắp xếp tăng dần theo thời gian. */
  public Map<YearMonth, Double> statisticsByMonth() {
    Map<YearMonth, Double> result = new TreeMap<>();
    for (Transaction tx : transactions) {
      result.merge(YearMonth.from(tx.getDate()), tx.getSignedAmount(), Double::sum);
    }
    return result;
  }

  /**
   * Số tiền đã dùng cho danh mục trong chu kỳ hiện tại của ngân sách, tính từ
   * đầu chu kỳ tới hôm nay. Giao dịch ghi ngày tương lai không được tính.
   */
  public double spentInCurrentPeriod(Category category) {
    Budget budget = budgets.get(category);
    if (budget == null) {
      throw new IllegalArgumentException("Danh mục " + category + " chưa có ngân sách");
    }
    LocalDate today = LocalDate.now();
    LocalDate start = periodStart(budget.getPeriod(), today);
    double total = 0;
    for (Transaction tx : transactions) {
      if (tx.getCategory().equals(category)
              && !tx.getDate().isBefore(start)
              && !tx.getDate().isAfter(today)) {
        total += tx.getAmount();
      }
    }
    return total;
  }

  /** Danh mục chưa đặt ngân sách thì coi như không vượt. */
  public boolean isOverBudget(Category category) {
    Budget budget = budgets.get(category);
    return budget != null && budget.isExceeded(spentInCurrentPeriod(category));
  }

  public List<Category> categoriesOverBudget() {
    List<Category> result = new ArrayList<>();
    for (Category category : budgets.keySet()) {
      if (isOverBudget(category)) {
        result.add(category);
      }
    }
    return result;
  }

  private double sumAmount(TransactionType type, YearMonth month) {
    double total = 0;
    for (Transaction tx : transactions) {
      if (tx.getType() != type) {
        continue;
      }
      if (month != null && !YearMonth.from(tx.getDate()).equals(month)) {
        continue;
      }
      total += tx.getAmount();
    }
    return total;
  }

  private Map<Category, Double> sumByCategory(YearMonth month) {
    Map<Category, Double> result = new LinkedHashMap<>();
    for (Transaction tx : transactions) {
      if (month != null && !YearMonth.from(tx.getDate()).equals(month)) {
        continue;
      }
      result.merge(tx.getCategory(), tx.getAmount(), Double::sum);
    }
    return result;
  }

  /** Mốc bắt đầu chu kỳ đang diễn ra, tính theo {@code today}. */
  private static LocalDate periodStart(Period period, LocalDate today) {
    return switch (period) {
      case DAILY -> today;
      case WEEKLY -> today.with(DayOfWeek.MONDAY);
      case MONTHLY -> today.withDayOfMonth(1);
      case YEARLY -> today.withDayOfYear(1);
    };
  }

  private static YearMonth requireMonth(YearMonth month) {
    if (month == null) {
      throw new IllegalArgumentException("Tháng không được để trống");
    }
    return month;
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
   * Nạp giao dịch từ file, thay thế danh sách hiện tại. Cố tình không đi qua
   * addTransaction: số dư trong file ví đã là số dư cuối cùng, áp lại các giao
   * dịch lần nữa sẽ trừ tiền hai lần. Việc kiểm tra trùng mã vẫn giữ để file
   * hỏng bị phát hiện ngay thay vì lọt vào bộ nhớ.
   */
  public void loadTransactions(String path) throws IOException {
    List<Transaction> loaded = requireTransactionStorage().load(path);
    transactions.clear();
    for (Transaction tx : loaded) {
      requireNewTransaction(tx);
      transactions.add(tx);
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
