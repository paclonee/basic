package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import java.util.List;

/**
 * Hợp đồng lưu trữ giao dịch (CSV / JSON, ...).
 * Triển khai cụ thể quyết định định dạng file.
 */
public interface Storage {

  /** Ghi danh sách giao dịch ra đường dẫn {@code path}. */
  void save(List<Transaction> transactions, String path);

  /** Đọc danh sách giao dịch từ đường dẫn {@code path}. */
  List<Transaction> load(String path);
}
