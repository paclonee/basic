package com.example.personalexpensemanager.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Tiện ích đọc field JSON dùng chung cho các adapter. */
final class JsonFields {

  private JsonFields() {}

  /** Trả về null khi field vắng mặt hoặc là JSON null, tránh getAsString() nổ. */
  static String optionalString(JsonObject obj, String field) {
    JsonElement element = obj.get(field);
    return element == null || element.isJsonNull() ? null : element.getAsString();
  }

  /** Trả về {@code fallback} khi field vắng mặt hoặc là JSON null. */
  static double optionalDouble(JsonObject obj, String field, double fallback) {
    JsonElement element = obj.get(field);
    return element == null || element.isJsonNull() ? fallback : element.getAsDouble();
  }
}
