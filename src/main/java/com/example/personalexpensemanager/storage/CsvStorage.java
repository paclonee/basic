package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import java.util.List;

/** Lưu / đọc giao dịch dạng CSV. */
public class CsvStorage implements Storage<Transaction> {

  @Override
  public void save(List<Transaction> transactions, String path) {
    // TODO: ghi CSV (header + từng dòng giao dịch)
    throw new UnsupportedOperationException("CsvStorage.save chưa được cài đặt");
  }

  @Override
  public List<Transaction> load(String path) {
    // TODO: parse CSV thành List<Transaction>
    throw new UnsupportedOperationException("CsvStorage.load chưa được cài đặt");
  }
}
