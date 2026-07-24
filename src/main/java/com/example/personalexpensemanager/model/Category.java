package com.example.personalexpensemanager.model;

import com.example.personalexpensemanager.enums.TransactionType;

/** Danh mục thu/chi (Ăn uống, Lương, ...). */
public class Category {

  private String name;
  private TransactionType type;

  public Category() {
    // TODO
  }

  public Category(String name, TransactionType type) {
    // TODO
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    // TODO
  }

  public TransactionType getType() {
    return type;
  }

  public void setType(TransactionType type) {
    // TODO
  }
}
