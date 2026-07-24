package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import java.util.Collections;
import java.util.List;

/** Lưu / đọc giao dịch dạng JSON. */
public class JsonStorage implements Storage {

  @Override
  public void save(List<Transaction> transactions, String path) {
    // TODO: serialize sang JSON
  }

  @Override
  public List<Transaction> load(String path) {
    // TODO: deserialize từ JSON
    return Collections.emptyList();
  }
}
