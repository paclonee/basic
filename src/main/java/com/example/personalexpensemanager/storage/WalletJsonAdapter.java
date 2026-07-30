package com.example.personalexpensemanager.storage;

import static com.example.personalexpensemanager.storage.JsonFields.optionalDouble;
import static com.example.personalexpensemanager.storage.JsonFields.optionalString;

import com.example.personalexpensemanager.enums.WalletType;
import com.example.personalexpensemanager.model.BankAccount;
import com.example.personalexpensemanager.model.CashWallet;
import com.example.personalexpensemanager.model.EWallet;
import com.example.personalexpensemanager.model.Wallet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * Chuyển đổi JSON <-> Wallet. Cùng lý do như {@link TransactionJsonAdapter}:
 * CashWallet / BankAccount / EWallet có field riêng nên Gson không tự biết dựng
 * lớp con nào, phải ghi kèm loại ví rồi tự dựng lại.
 */
public class WalletJsonAdapter implements JsonSerializer<Wallet>, JsonDeserializer<Wallet> {

  @Override
  public JsonElement serialize(Wallet wallet, Type typeOfSrc, JsonSerializationContext ctx) {
    JsonObject obj = new JsonObject();
    obj.addProperty("type", wallet.getWalletType().name());
    obj.addProperty("name", wallet.getName());
    obj.addProperty("balance", wallet.getBalance());

    if (wallet instanceof BankAccount bank) {
      obj.addProperty("bankName", bank.getBankName());
      obj.addProperty("accountNumber", bank.getAccountNumber());
      obj.addProperty("transactionFee", bank.getTransactionFee());
    } else if (wallet instanceof EWallet ewallet) {
      obj.addProperty("provider", ewallet.getProvider());
    } else if (!(wallet instanceof CashWallet)) {
      throw new JsonIOException("Chưa hỗ trợ ghi JSON cho lớp " + wallet.getClass().getName());
    }
    return obj;
  }

  @Override
  public Wallet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
    JsonObject obj = json.getAsJsonObject();
    String name = obj.get("name").getAsString();
    double balance = obj.get("balance").getAsDouble();
    WalletType type = WalletType.valueOf(obj.get("type").getAsString());

    return switch (type) {
      case CASH -> new CashWallet(name, balance);
      case BANK -> {
        BankAccount bank = new BankAccount(name, balance,
                optionalString(obj, "bankName"), optionalString(obj, "accountNumber"));
        bank.setTransactionFee(optionalDouble(obj, "transactionFee", 0));
        yield bank;
      }
      case EWALLET -> new EWallet(name, balance, optionalString(obj, "provider"));
    };
  }
}
