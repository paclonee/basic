package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import java.util.Collections;
import java.util.List;

/** Lưu / đọc giao dịch dạng CSV. */
public class CsvStorage implements Storage {

  @Override
  public void save(List<Transaction> transactions, String path) {
    // TODO: ghi CSV (header + từng dòng giao dịch)
  }

  @Override
  public List<Transaction> load(String path) {
    // TODO: parse CSV thành List<Transaction>
    return Collections.emptyList();
  }
}
