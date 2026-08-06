## Lỗi thật đã sửa

`Wallet` **— nặng nhất.** `name` không được validate ở cả constructor lẫn `setName`, nên tạo được ví tên `null`. Đây không phải lỗi lý thuyết: `resolveWallets()` bạn đang viết trong `ExpenseManager` dùng `Collectors.toMap(Wallet::getName, w -> w)`, gặp một ví tên null là NPE ngay. Ngoài ra `setBalance` đang bị comment out kèm `// TODO` — tôi bỏ khối code chết đó và thay bằng chú thích giải thích lý do không mở setter: số dư chỉ nên đổi qua `deposit`/`withdraw` để luôn đi qua validate. `deposit(0)`/`withdraw(0)` trước đây được chấp nhận, giờ yêu cầu lớn hơn 0. Tôi thêm `toString()` vì `ConsoleView` sẽ cần in danh sách ví.

`Transaction`**.** `id` không được validate, trong khi `findTransaction(String id)` và `removeTransaction(String id)` phụ thuộc hoàn toàn vào nó — giờ chặn rỗng. `category` cũng không được validate dù `date` và `wallet` thì có, dẫn tới key `null` trong `statisticsByCategory()` — giờ chặn `null`. `amount` cho phép bằng 0, giờ phải dương. Phần validate bị lặp giữa constructor và setter nên tôi gom vào các helper `private static`. Thông báo lỗi đang là tiếng Anh trong khi `Category` dùng tiếng Việt, tôi thống nhất về tiếng Việt.

`Category`**.** Constructor gọi `setName`/`setType` là method có thể bị override, `javac -Xlint:all` báo cảnh báo `this-escape`; tôi chuyển sang helper `private static` cho khớp với hai lớp trên. Constructor rỗng `Category()` tạo ra đối tượng `name = null, type = null`, khiến `toString()` trả về `null` và `equals()` trả `false` — tôi bỏ nó, và việc này không ảnh hưởng Gson vì Gson dựng đối tượng qua `Unsafe` chứ không cần constructor rỗng. Quan trọng hơn, `equals` dùng `equalsIgnoreCase` còn `hashCode` dùng `toLowerCase` — hai cách chuẩn hoá khác nhau cho cùng một hợp đồng; giờ cả hai đi qua một hàm `normalize()` duy nhất.

`CashWallet` logic đã đúng, chỉ thiếu newline cuối file.

`Storage` đúng như UML nên tôi không sửa. Chỉ lưu ý: nó chỉ lưu được `Transaction`, còn `loadData` của bạn cần load cả ví — đúng như chú thích bạn tự ghi trong `ExpenseManager`. Chỗ này cần quyết định có tổng quát hoá thành `Storage<T>` hay không.

Tôi viết một bài test tạm chạy thử 23 trường hợp (trim tên, equals/hashCode qua `HashMap`, rút quá số dư, số dư không đổi sau khi lỗi, các nhánh validate) — **23/23 pass**, và biên dịch riêng 5 lớp với `-Xlint:all` không còn cảnh báo nào. File test đó tôi đã xoá, không để lại trong repo.

## Một điểm thiết kế bạn cần biết

`Transaction` và `Wallet` **không có** constructor không tham số, nên mọi lớp con buộc phải gọi `super(...)` — đúng như `CashWallet()` bạn đã viết. Tôi giữ nguyên nguyên tắc này để đối tượng luôn ở trạng thái hợp lệ.

## Vì sao project chưa build được

Không lỗi nào dưới đây thuộc 5 lớp trên:


| Chỗ                                           | Vấn đề                                                                                                                                                                         |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `module-info.java`                            | Thiếu `requires com.google.gson;` — bạn đã thêm gson vào `pom.xml` nhưng chưa khai báo trong module                                                                            |
| `ExpenseManager.java`                         | Dùng `transactionStorage` (field thật tên `storage`), thiếu import `IOException` và `Collectors`, chưa có `loadWallets()`, và gán vào `transactions`/`wallets` đang là `final` |
| `TransactionJsonAdapter.java`                 | `addProperty("category", tx.getCategory())` không có overload nhận `Category`; phần deserialize truyền `String` vào constructor cần `Category`                                 |
| `BankAccount`, `EWallet`, `Expense`, `Income` | Constructor còn `// TODO` rỗng nên gọi `super()` ngầm, mà lớp cha không có constructor rỗng                                                                                    |


Ngoài ra có `src/main/resources/com/target/` chứa 104 KB file `.class` cũ, chắc do một lần build sai đường dẫn `-d`. Nó nằm trong `resources` nên sẽ bị đóng gói vào jar.

> **Bạn muốn tôi làm tiếp phần nào để project build lại được?**
>
> - Sửa hết: module-info, ExpenseManager, TransactionJsonAdapter và 4 lớp con (Recommended) ✔
> - Chỉ sửa module-info + ExpenseManager, phần model để tôi tự làm
> - Chỉ thêm requires com.google.gson vào module-info
> - Không sửa gì thêm, tôi tự làm tiếp

> **Thư mục rác src/main/resources/com/target/ xử lý thế nào?**
>
> - Xoá luôn (Recommended) ✔
> - Giữ lại, tôi tự kiểm tra

> **Phần vừa sửa có commit luôn không?**
>
> - Đợi sửa xong hết rồi commit một lần (Recommended)
> - Commit ngay 4 lớp nền móng này
> - Không commit, tôi tự làm
>
> Answer: Tính sau nhé

Rõ. Tôi sẽ làm cho project build lại được. Đọc nốt các file còn lại trước.

Bắt đầu với `module-info` và các lớp con.

Giờ `Budget` và `TransactionJsonAdapter`.

Giờ viết lại `ExpenseManager`.

Xoá thư mục rác rồi build.

Project compile được rồi. Giờ test end-to-end thật, chạy **dưới dạng module** để chắc Gson hoạt động qua JPMS chứ không chỉ compile được.

Đây chính là loại lỗi chỉ lộ ra khi chạy thật. `registerTypeAdapter` chỉ áp dụng cho đúng kiểu `Transaction`; khi ghi `List<Transaction>`, Gson dùng kiểu thực tế (`Income`) nên bỏ qua adapter và rơi về reflection. Sửa bằng `registerTypeHierarchyAdapter`.

35/35 pass. Xem thử file JSON sinh ra rồi dọn file tạm.

JSON đúng và giữ nguyên tiếng Việt. Một điểm cần chắc: `FileWriter`/`FileReader` dùng charset mặc định của hệ thống — nó chạy được vì JDK 18+ mặc định UTF-8, nhưng nên khai báo tường minh cho chắc.

Vẫn 35/35. Dọn file tạm và build sạch lần cuối.

`./mvnw clean test` **đã pass**, project build lại được, và tôi đã kiểm chứng bằng 35 test end-to-end chạy **dưới dạng JPMS module** — 35/35 pass. Chưa commit theo ý bạn.

## Lỗi runtime quan trọng nhất tìm được

Việc chạy thật (thay vì chỉ compile) lộ ra một bug mà compiler không bao giờ báo: `JsonStorage` dùng `registerTypeAdapter(Transaction.class, ...)`, nhưng adapter đó **chỉ áp dụng khi kiểu khai báo đúng là** `Transaction`. Lúc ghi `List<Transaction>`, Gson lấy kiểu *thực tế* là `Income`, không thấy adapter nào, nên rơi về reflection và nổ `InaccessibleObjectException` vì module không `opens` package `model` cho Gson.

Cách sửa sai ở đây sẽ là thêm `opens ... to com.google.gson`: làm vậy Gson dùng reflection và **không ghi field** `kind`, còn lúc đọc thì adapter lại đòi `kind` — round-trip hỏng âm thầm. Nên tôi đổi sang `registerTypeHierarchyAdapter`, để adapter áp dụng cho cả `Transaction` và mọi lớp con. Không cần `opens`, và tính đa hình hoạt động cả hai chiều.

## Những gì đã sửa

`module-info.java` thêm `requires com.google.gson;` (bạn đã có gson trong pom nhưng chưa khai báo trong module).

`TransactionJsonAdapter` ghi `category` bằng `addProperty(...)` vốn không có overload nhận `Category`, và lúc đọc lại truyền `String` vào constructor cần `Category` — giờ ghi thành object `{name, type}` để dựng lại được cả tên lẫn loại. Ngoài ra `note` null làm `getAsString()` nổ trên `JsonNull`, tôi thêm helper `optionalString`. Với lớp con lạ thì trước đây ghi ra JSON thiếu `kind` rồi hỏng lúc đọc, giờ báo lỗi ngay.

`ExpenseManager` tôi viết lại. Bản cũ đang dở giữa: gán vào field `transactionStorage` không tồn tại, thiếu import `IOException` và `Collectors`, gọi `loadWallets()` chưa có, và gán lại `transactions`/`wallets` đang là `final`. Tôi bỏ `loadData(transactionPath, walletPath)` vì `Storage` chỉ lưu giao dịch, và giữ đúng ý tưởng của bạn: `resolveWallets()` nay nối placeholder wallet với các ví đã `addWallet`, tra theo tên. Thêm `registerLoadedCategories()` vì file giao dịch mang theo danh mục của nó. Các getter trả về view chỉ đọc để không ai thêm/xoá vòng qua bước kiểm tra.

`Expense`, `Income`, `RecurringExpense`, `BankAccount`, `EWallet`, `Budget` đã cài đặt xong. Hai ví kế thừa theo đúng mẫu `CashWallet()` bạn đã chọn là gọi `super(...)` với giá trị mặc định. Riêng constructor rỗng của `Expense`/`Income`/`RecurringExpense` tôi bỏ, vì một giao dịch không có trạng thái rỗng nào hợp lệ (id, số tiền, ngày, danh mục, ví đều bắt buộc), và Gson không cần constructor rỗng.

`BankAccount.withdraw` có phí giao dịch, nhưng phí **mặc định bằng 0** và cấu hình qua `setTransactionFee` — tôi không tự bịa ra một mức phí cho bạn.

`JsonStorage` còn một điểm nữa: `FileWriter`/`FileReader` dùng charset mặc định của máy. Nó chạy được vì JDK 18+ mặc định UTF-8, nhưng tên danh mục là tiếng Việt nên tôi cố định UTF-8 tường minh qua `Files.newBufferedWriter/Reader`.

Thư mục rác `src/main/resources/com/target/` đã xoá.

## Còn lại là stub, tôi không chạm

`CsvStorage` (vẫn trả về list rỗng), `ConsoleView`, `AppView`, `MainController` — đúng phạm vi bạn giao. Lưu ý `CsvStorage` hiện "chạy mà không làm gì", nếu `setStorage(new CsvStorage())` thì save/load im lặng không lưu gì cả.

Vẫn còn một câu hỏi thiết kế tôi chưa tự quyết: `Storage` chỉ lưu được `Transaction`, nên số dư ví không được ghi xuống file. Muốn lưu cả ví thì cần tổng quát hoá thành `Storage<T>` hoặc thêm một interface riêng cho ví — bạn muốn đi hướng nào thì tôi làm tiếp.

---

**User**

ok, cứ tổng quát hoá nhé

---

**Cursor**

Rõ. Tổng quát hoá `Storage<T>` thì phải giải luôn bài toán đa hình cho `Wallet` giống như đã làm với `Transaction`.

Giờ `JsonStorage<T>` và refactor `TransactionJsonAdapter` để dùng helper chung.

Giờ `ExpenseManager` với hai storage.

Giờ test lại end-to-end, lần này kiểm tra đúng thứ mà việc tổng quát hoá mở ra: số dư ví sống sót qua save/load.

34/34 pass. Xem file ví sinh ra rồi dọn.

Giờ cập nhật sơ đồ UML cho khớp với `Storage<T>`.

Xong, `./mvnw clean test` pass và 34/34 test end-to-end pass khi chạy dưới dạng module. **Số dư ví giờ đã sống sót qua save/load** — đó chính là thứ mà thiết kế cũ không làm được.

## Đã tổng quát hoá thế nào

`Storage<T>` giờ nhận tham số kiểu, `save(List<T>, path)` và `load(path)` trả về `List<T>`.

Nhưng tổng quát hoá interface là phần dễ. Phần thực chất là: `Wallet` gặp **đúng bài toán đa hình** như `Transaction` — `CashWallet`, `BankAccount`, `EWallet` có field riêng nên Gson không biết dựng lớp con nào. Nên tôi viết thêm `WalletJsonAdapter` ghi kèm `type` rồi tự dựng lại, y hệt cách `TransactionJsonAdapter` dùng `kind`. Nếu chỉ tổng quát hoá interface mà bỏ qua chỗ này thì `loadWallets` sẽ nổ đúng cái `InaccessibleObjectException` lần trước.

`JsonStorage<T>` giờ dùng qua hai factory `JsonStorage.forTransactions()` và `JsonStorage.forWallets()`. Tôi để constructor là `private` vì mỗi loại dữ liệu cần một adapter khác nhau — mở constructor ra thì rất dễ tạo một `JsonStorage` không có adapter nào và lại rơi vào lỗi reflection.

`ExpenseManager` nay giữ hai storage riêng, `transactionStorage` và `walletStorage` — trùng đúng với cái tên `transactionStorage` mà bạn đã viết dở trước đó. API persistence:


| Method                                  | Việc                                               |
| --------------------------------------- | -------------------------------------------------- |
| `loadData(txPath, walletPath)`          | Nạp ví trước rồi giao dịch, dùng lúc khởi động app |
| `saveData(txPath, walletPath)`          | Ghi cả hai, dùng lúc thoát app                     |
| `saveTransactions` / `loadTransactions` | Chỉ giao dịch                                      |
| `saveWallets` / `loadWallets`           | Chỉ ví                                             |


`getStorage`/`setStorage`/`save`/`load` cũ đã bỏ vì một `Storage` duy nhất không còn đủ nghĩa.

Hai chi tiết tôi xử lý thêm khi viết phần load. `loadWallets` gọi `resolveWallets()` ở cuối, nên gọi hai method theo thứ tự nào cũng đúng, không chỉ riêng thứ tự trong `loadData`. Và cả hai hàm load đều đi qua `addWallet`/`addTransaction` thay vì `addAll`, để file hỏng (trùng tên ví, trùng mã giao dịch) bị phát hiện ngay chứ không lọt vào bộ nhớ — test "nạp lại lần hai không nhân đôi dữ liệu" kiểm tra chính điều này.

`CsvStorage` giờ là `Storage<Transaction>` và vẫn là stub, nhưng tôi đổi từ "im lặng không làm gì" sang `UnsupportedOperationException` có thông báo rõ. Trước đây `saveTransactions` với `CsvStorage` sẽ báo thành công mà không ghi gì cả — mất dữ liệu âm thầm, tệ hơn là crash.

Thêm một file nhỏ `JsonFields` chứa `optionalString`/`optionalDouble` dùng chung cho hai adapter, thay vì lặp lại cùng một đoạn kiểm tra `JsonNull` ở hai nơi.

## Một việc bạn nên biết

`docs/class-diagram.md` không còn trong repo nữa — commit `b5b4caf` ("Hoan thien lop truu tuong") của bạn đã xoá nó, 266 dòng. Chỉ còn `docs/class-diagram.png`. Tôi không tự khôi phục vì không rõ bạn xoá có chủ ý hay không. Nếu là vô tình thì lấy lại được bằng `git checkout fc9e309 -- docs/class-diagram.md`, và lúc đó tôi sẽ cập nhật phần `Storage` trong sơ đồ cho khớp với `Storage<T>` cùng hai adapter mới.

Vẫn chưa commit gì cả. Hiện có 16 file sửa và 2 file mới (`JsonFields.java`, `WalletJsonAdapter.java`).


---

**Cursor**

`Wallet` **không phải interface** — nó là abstract class:

```9:12:src/main/java/com/example/personalexpensemanager/model/Wallet.java
public abstract class Wallet {

  private String name;
  private double balance;
```

Thật ra trong toàn bộ code của bạn chỉ có **đúng một** interface, là `Storage`. Còn `Transaction` và `Wallet` đều là abstract class.

Và khi tôi viết "thêm một interface riêng cho ví", tôi đang nói về interface **lưu trữ** ví, tức là thứ nằm ở tầng `storage`, chứ không phải interface mô tả bản thân cái ví. Hai thứ khác tầng hoàn toàn:


| Thứ                           | Là gì          | Trả lời câu hỏi                                       |
| ----------------------------- | -------------- | ----------------------------------------------------- |
| `Wallet` (package `model`)    | abstract class | Một cái ví *là gì*: có tên, có số dư, nạp/rút thế nào |
| `Storage` (package `storage`) | interface      | *Ghi/đọc* một danh sách đối tượng ra file thế nào     |


Vấn đề lúc đó nằm ở cột thứ hai. `Storage` cũ hardcode `Transaction`:

```java
public interface Storage {
  void save(List<Transaction> transactions, String path) throws IOException;
  List<Transaction> load(String path) throws IOException;
}
```

Nên dù `Wallet` có tồn tại đầy đủ trong `model`, **không có đường nào để ghi danh sách ví xuống file cả** — số dư ví chỉ sống trong RAM, tắt app là mất. Việc `Wallet` là abstract class hay interface không giúp gì cho chuyện đó.

Hai hướng tôi đưa ra khi đó là: hoặc tổng quát hoá thành `Storage<T>`, hoặc viết thêm một interface thứ hai chỉ dành cho ví, đại loại như thế này:

```java
public interface WalletStorage {
  void save(List<Wallet> wallets, String path) throws IOException;
  List<Wallet> load(String path) throws IOException;
}
```

Bạn đã chọn hướng tổng quát hoá, nên giờ `Storage<Wallet>` chính là "interface lưu trữ ví" đó, chỉ là không cần viết thêm interface mới:

```27:28:src/main/java/com/example/personalexpensemanager/manager/ExpenseManager.java
  private Storage<Transaction> transactionStorage;
  private Storage<Wallet> walletStorage;
```

Hướng generic thắng vì với `WalletStorage` riêng, mỗi lần thêm một loại dữ liệu cần lưu (ví dụ `Budget` sau này) bạn lại phải viết thêm một interface gần như y hệt. Còn với `Storage<T>` thì chỉ cần `Storage<Budget>`, không thêm dòng interface nào.

Nhân đây nói thêm về câu hỏi ngầm trong thắc mắc của bạn: `Wallet` **không thể** là interface, vì nó giữ state (`name`, `balance` ở dòng 11-12) và có constructor validate. Interface trong Java không giữ được field instance, nên nếu đổi `Wallet` thành interface thì `CashWallet`, `BankAccount`, `EWallet` mỗi lớp phải tự khai báo lại `name`/`balance` cùng toàn bộ logic `deposit`/`withdraw` — lặp code ba lần. Đây đúng là ranh giới kinh điển: **abstract class khi cần chia sẻ state và code dùng chung, interface khi chỉ cần định nghĩa hợp đồng**. `Storage` không giữ state nào nên nó là interface, hoàn toàn hợp lý.

---

**Cursor**

Tôi phải nói thẳng một điều trước, vì nó quan trọng hơn phần giải thích: hiện tại tắt app mở lại vẫn mất dữ liệu. Thay đổi đó chỉ tạo ra khả năng lưu, chứ chưa tự động lưu.

Tôi vừa tìm khắp src/main/java: saveData và loadData chỉ được định nghĩa trong ExpenseManager (dòng 223 và 229), không có một chỗ nào gọi chúng. PersonalExpenseManagerApp.start() cũng chỉ load file FXML rồi hiện cửa sổ, thậm chí chưa tạo ExpenseManager nào cả.

Cơ chế thực sự
Mọi object Java sống trong RAM. Tắt app là RAM mất sạch, không có cách nào tránh. Muốn dữ liệu sống sót thì phải làm hai việc: lúc thoát ghi ra file, và lúc mở đọc lại từ file. Mỗi việc phải có ai đó gọi, không tự xảy ra.

Việc tổng quát hoá Storage<T> giải quyết vấn đề "có gì để mà ghi", chứ không giải quyết "ai gọi lệnh ghi".

Trước khi sửa: gọi save cũng vẫn mất số dư
Đây mới là chỗ tổng quát hoá thực sự thay đổi tình hình. Giả sử hồi đó bạn đã gọi save() đầy đủ, dữ liệu ví vẫn mất, vì file JSON giao dịch chỉ lưu tên ví dưới dạng một chuỗi:


TransactionJsonAdapter.java
Lines 30-30
obj.addProperty("walletName", tx.getWallet().getName());
Số dư, loại ví, tên ngân hàng, provider — không có gì được ghi. Nên lúc đọc lại, adapter buộc phải dựng một cái ví giả:


TransactionJsonAdapter.java
Lines 58-60
// Ví chỉ phục hồi tạm bằng tên (balance = 0); ExpenseManager sẽ gán lại
// đúng đối tượng Wallet thật sau khi nạp xong danh sách ví.
Wallet placeholderWallet = new CashWallet(walletName, 0);
Mọi ví trở thành CashWallet với số dư 0, kể cả cái vốn là BankAccount có 5 triệu. Và cái comment "ExpenseManager sẽ gán lại đúng đối tượng Wallet thật" khi đó là một lời hứa không thể thực hiện, vì Storage bị khoá cứng vào Transaction nên không có đường nào đọc danh sách ví thật từ file lên. Đó chính là lý do tôi nói "số dư ví không được ghi xuống file".

Sau khi sửa
Giờ ví có file riêng với đầy đủ thông tin, target/smoke-wallets.json lúc tôi test trông như thế này:

{
  "type": "BANK",
  "name": "Vietcombank",
  "balance": 5000000.0,
  "bankName": "VCB",
  "accountNumber": "0123456789",
  "transactionFee": 1100.0
}
Nên loadWallets dựng lại được đúng BankAccount với đúng 5 triệu, rồi resolveWallets() đổi placeholder trong từng giao dịch sang đối tượng ví thật đó. Lời hứa trong comment giờ mới thành hiện thực — test "SO DU VI SONG SOT qua save/load" và "T2 trỏ sang đúng instance ví đã nạp" kiểm tra chính xác hai bước này.

Phần còn thiếu để bạn thật sự "tắt máy mở lại"
Cần một chỗ gọi. Đại loại như thế này trong PersonalExpenseManagerApp — tôi chỉ phác ra chứ đang ở Ask mode nên không sửa file:

private final ExpenseManager manager =
    new ExpenseManager(JsonStorage.forTransactions(), JsonStorage.forWallets());
@Override
public void start(Stage stage) throws IOException {
  manager.loadData("data/transactions.json", "data/wallets.json");
  // ... dựng scene, truyền manager vào controller ...
  stage.setOnCloseRequest(event -> {
    try {
      manager.saveData("data/transactions.json", "data/wallets.json");
    } catch (IOException e) {
      // hiện Alert cho người dùng
    }
  });
}
loadData an toàn khi chạy lần đầu: JsonStorage.load thấy file chưa tồn tại thì trả về list rỗng chứ không nổ. Nhớ tạo sẵn thư mục data/, vì Files.newBufferedWriter không tự tạo thư mục cha.

Ngoài ra MainController hiện chỉ set một cái label, chưa có đường nào để người dùng thêm giao dịch, nên kể cả wire xong save/load thì vẫn chưa có dữ liệu gì để mà lưu. Muốn tôi làm phần wiring này thì bạn chuyển sang Agent mode nhé.

---