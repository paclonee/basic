package com.example.personalexpensemanager.storage;

import static com.example.personalexpensemanager.storage.JsonFields.optionalString;

import com.example.personalexpensemanager.enums.Period;
import com.example.personalexpensemanager.enums.TransactionType;
import com.example.personalexpensemanager.model.*;
import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Chuyển đổi JSON <-> Transaction, xử lý tính đa hình: Income/Expense/
 * RecurringExpense đều là Transaction nhưng có field riêng, nên Gson
 * mặc định không tự biết dựng lớp con nào — cần custom adapter này.
 */
public class TransactionJsonAdapter implements JsonSerializer<Transaction>, JsonDeserializer<Transaction> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public JsonElement serialize(Transaction tx, Type typeOfSrc, JsonSerializationContext ctx) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", tx.getId());
        obj.addProperty("amount", tx.getAmount());
        obj.addProperty("date", tx.getDate().format(DATE_FORMAT));
        obj.addProperty("note", tx.getNote());
        obj.add("category", serializeCategory(tx.getCategory()));
        obj.addProperty("walletName", tx.getWallet().getName());

        if (tx instanceof RecurringExpense re) {
            obj.addProperty("kind", "RECURRING_EXPENSE");
            obj.addProperty("paymentMethod", re.getPaymentMethod());
            obj.addProperty("period", re.getPeriod().name());
        } else if (tx instanceof Expense ex) {
            obj.addProperty("kind", "EXPENSE");
            obj.addProperty("paymentMethod", ex.getPaymentMethod());
        } else if (tx instanceof Income inc) {
            obj.addProperty("kind", "INCOME");
            obj.addProperty("source", inc.getSource());
        } else {
            throw new JsonIOException("Chưa hỗ trợ ghi JSON cho lớp " + tx.getClass().getName());
        }
        return obj;
    }

    @Override
    public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        JsonObject obj = json.getAsJsonObject();
        String id = obj.get("id").getAsString();
        double amount = obj.get("amount").getAsDouble();
        LocalDate date = LocalDate.parse(obj.get("date").getAsString(), DATE_FORMAT);
        String note = optionalString(obj, "note");
        Category category = deserializeCategory(obj.get("category"));
        String walletName = obj.get("walletName").getAsString();

        // Ví chỉ phục hồi tạm bằng tên (balance = 0); ExpenseManager sẽ gán lại
        // đúng đối tượng Wallet thật sau khi nạp xong danh sách ví.
        Wallet placeholderWallet = new CashWallet(walletName, 0);

        String kind = obj.get("kind").getAsString();
        return switch (kind) {
            case "INCOME" -> new Income(id, amount, date, note, category, placeholderWallet,
                    optionalString(obj, "source"));
            case "EXPENSE" -> new Expense(id, amount, date, note, category, placeholderWallet,
                    optionalString(obj, "paymentMethod"));
            case "RECURRING_EXPENSE" -> new RecurringExpense(id, amount, date, note, category, placeholderWallet,
                    optionalString(obj, "paymentMethod"), Period.valueOf(obj.get("period").getAsString()));
            default -> throw new JsonParseException("Unknown transaction kind: " + kind);
        };
    }

    /** Category phải ghi thành object {name, type} vì cần cả hai để dựng lại. */
    private static JsonObject serializeCategory(Category category) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", category.getName());
        obj.addProperty("type", category.getType().name());
        return obj;
    }

    private static Category deserializeCategory(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            throw new JsonParseException("Giao dịch trong file JSON thiếu danh mục");
        }
        JsonObject obj = element.getAsJsonObject();
        return new Category(
                obj.get("name").getAsString(),
                TransactionType.valueOf(obj.get("type").getAsString()));
    }
}
