package com.example.personalexpensemanager.storage;

import java.io.IOException;
import java.util.List;

/**
 * Hợp đồng lưu trữ một loại dữ liệu (CSV / JSON, ...).
 * Triển khai cụ thể quyết định định dạng file.
 *
 * @param <T> loại đối tượng được lưu, ví dụ Transaction hoặc Wallet
 */
public interface Storage<T> {

  /** Ghi danh sách đối tượng ra đường dẫn {@code path}. */
  void save(List<T> items, String path) throws IOException;

  /** Đọc danh sách đối tượng từ đường dẫn {@code path}. */
  List<T> load(String path) throws IOException;
}
