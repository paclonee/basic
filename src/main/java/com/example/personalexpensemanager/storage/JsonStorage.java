package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Lưu / đọc giao dịch dạng JSON. */
public class JsonStorage implements Storage {
  private final Gson gson;

  public JsonStorage() {
    this.gson = new GsonBuilder()
            .registerTypeAdapter(Transaction.class, new TransactionJsonAdapter())
            .setPrettyPrinting()
            .create();
  }

  @Override
  public void save(List<Transaction> transactions, String path) throws IOException {
    try (Writer writer = new FileWriter(path)) {
      gson.toJson(transactions, writer);
    }
  }

  @Override
  public List<Transaction> load(String path) throws IOException {
    File file = new File(path);
    if (!file.exists()) {
      return new ArrayList<>(); // chưa có file -> coi như chưa có giao dịch nào
    }
    try (Reader reader = new FileReader(path)) {
      Type listType = new TypeToken<List<Transaction>>() {}.getType();
      List<Transaction> result = gson.fromJson(reader, listType);
      return result != null ? result : new ArrayList<>();
    }
  }
}
