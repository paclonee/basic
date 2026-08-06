package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import com.example.personalexpensemanager.model.Wallet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu / đọc danh sách đối tượng dạng JSON.
 * Dùng qua các factory {@link #forTransactions()} và {@link #forWallets()} vì mỗi
 * loại dữ liệu cần một adapter đa hình riêng.
 *
 * @param <T> loại đối tượng được lưu
 */
public class JsonStorage<T> implements Storage<T> {

  private final Gson gson;
  private final Type listType;

  private JsonStorage(Gson gson, Type listType) {
    this.gson = gson;
    this.listType = listType;
  }

  /** Storage cho giao dịch: Income / Expense / RecurringExpense. */
  public static JsonStorage<Transaction> forTransactions() {
    return new JsonStorage<>(
            newGson(Transaction.class, new TransactionJsonAdapter()),
            new TypeToken<List<Transaction>>() {}.getType());
  }

  /** Storage cho ví: CashWallet / BankAccount / EWallet. */
  public static JsonStorage<Wallet> forWallets() {
    return new JsonStorage<>(
            newGson(Wallet.class, new WalletJsonAdapter()),
            new TypeToken<List<Wallet>>() {}.getType());
  }

  /**
   * Phải là registerTypeHierarchyAdapter: với registerTypeAdapter, Gson chỉ dùng
   * adapter khi kiểu khai báo đúng là lớp cha, còn khi ghi List thì nó lấy kiểu
   * thực tế (Income, BankAccount, ...) rồi rơi về reflection và nổ vì module
   * không opens package model cho Gson.
   */
  private static Gson newGson(Class<?> baseType, Object adapter) {
    return new GsonBuilder()
            .registerTypeHierarchyAdapter(baseType, adapter)
            .setPrettyPrinting()
            .create();
  }

  // Ghi/đọc luôn cố định UTF-8: tên danh mục và ghi chú là tiếng Việt, không thể
  // phụ thuộc charset mặc định của máy chạy.

  @Override
  public void save(List<T> items, String path) throws IOException {
    Path file = Path.of(path);
    Path folder = file.getParent();
    if (folder != null) {
      Files.createDirectories(folder); // lần chạy đầu chưa có thư mục data/
    }
    try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
      gson.toJson(items, listType, writer);
    }
  }

  @Override
  public List<T> load(String path) throws IOException {
    Path file = Path.of(path);
    if (!Files.exists(file)) {
      return new ArrayList<>(); // chưa có file -> coi như chưa có dữ liệu nào
    }
    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
      List<T> result = gson.fromJson(reader, listType);
      return result != null ? result : new ArrayList<>();
    }
  }
}
