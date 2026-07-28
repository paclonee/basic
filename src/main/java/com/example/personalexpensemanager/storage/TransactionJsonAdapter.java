package com.example.personalexpensemanager.storage;

import com.example.personalexpensemanager.enums.Period;
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
        obj.addProperty("category", tx.getCategory());
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
        }
        return obj;
    }

    @Override
    public Transaction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        JsonObject obj = json.getAsJsonObject();
        String id = obj.get("id").getAsString();
        double amount = obj.get("amount").getAsDouble();
        LocalDate date = LocalDate.parse(obj.get("date").getAsString(), DATE_FORMAT);
        String note = obj.has("note") ? obj.get("note").getAsString() : null;
        String category = obj.has("category") ? obj.get("category").getAsString() : null;
        String walletName = obj.get("walletName").getAsString();

        // Ví chỉ phục hồi tạm bằng tên (balance = 0); ExpenseManager sẽ gán lại
        // đúng đối tượng Wallet thật sau khi nạp xong danh sách ví.
        Wallet placeholderWallet = new CashWallet(walletName, 0);

        String kind = obj.get("kind").getAsString();
        return switch (kind) {
            case "INCOME" -> new Income(id, amount, date, note, category, placeholderWallet,
                    obj.get("source").getAsString());
            case "EXPENSE" -> new Expense(id, amount, date, note, category, placeholderWallet,
                    obj.get("paymentMethod").getAsString());
            case "RECURRING_EXPENSE" -> new RecurringExpense(id, amount, date, note, category, placeholderWallet,
                    obj.get("paymentMethod").getAsString(), Period.valueOf(obj.get("period").getAsString()));
            default -> throw new JsonParseException("Unknown transaction kind: " + kind);
        };
    }
}