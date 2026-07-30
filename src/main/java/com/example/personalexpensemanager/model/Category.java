package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;
import java.util.Locale;
import java.util.Objects;

/** Danh mục thu/chi (Ăn uống, Lương, ...). */
public class Category {

  private String name;
  private TransactionType type;

  public Category(String name, TransactionType type) {
    this.name = requireValidName(name);
    this.type = requireType(type);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = requireValidName(name);
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    this.type = requireType(type);
  }

  /**
   * Hai danh mục là một khi trùng tên (không phân biệt hoa/thường) và trùng loại.
   * Lưu ý: Category được dùng làm key của {@code Map<Category, Budget>}, nên đừng
   * gọi setName/setType sau khi đã đưa vào map — hashCode sẽ đổi và không tra
   * lại được entry cũ.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Category other)) {
      return false;
    }
    return type == other.type && normalize(name).equals(normalize(other.name));
  }

  @Override
  public int hashCode() {
    return Objects.hash(normalize(name), type);
  }

  @Override
  public String toString() {
    return name;
  }

  /** Chuẩn hoá tên để equals và hashCode luôn nhất quán với nhau. */
  private static String normalize(String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  private static String requireValidName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Tên danh mục không được để trống");
    }
    return name.trim();
  }

  private static TransactionType requireType(TransactionType type) {
    if (type == null) {
      throw new IllegalArgumentException("Loại giao dịch không được để trống");
    }
    return type;
  }
}
