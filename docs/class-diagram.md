<<<<<<< HEAD
# Sơ đồ lớp (UML) — Personal Expense Manager

Tài liệu mô tả quan hệ OOP theo đặc tả thiết kế.

## Kế thừa (Inheritance)

```text
Transaction (abstract)
├── Income
└── Expense
    └── RecurringExpense

Wallet (abstract)
├── CashWallet
├── BankAccount
└── EWallet
```

## Interface

```text
«interface» Storage
├── CsvStorage
└── JsonStorage
```

## Enum

| Enum | Giá trị |
|------|---------|
| `TransactionType` | `INCOME`, `EXPENSE` |
| `WalletType` | `CASH`, `BANK`, `EWALLET` |
| `Period` | `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY` |

## Composition / Coordination

```text
ExpenseManager
  *-- Transaction
  *-- Wallet
  *-- Category
  *-- Budget          (Map<Category, Budget>)
  o-- Storage

Category  <--  Budget
Category  <--  Transaction
Wallet    <--  Transaction

ConsoleView / AppView  -->  ExpenseManager
MainController (JavaFX) -->  (FXML + AppView / ExpenseManager)
```

## Nguyên tắc OOP

| Nguyên tắc | Áp dụng |
|------------|---------|
| Đóng gói | Field `private`; getter/setter + validate (`amount`, `balance`) |
| Kế thừa | `Income`/`Expense` ← `Transaction`; các ví ← `Wallet` |
| Đa hình | `getSignedAmount()`, `printInfo()`, `withdraw()`, `Storage.save/load` |
| Trừu tượng | `Transaction`, `Wallet` (abstract); `Storage` (interface) |

## Mermaid (tham khảo)

```mermaid
classDiagram
  direction TB

  class Transaction {
    <<abstract>>
    -id: String
    -amount: double
    -date: LocalDate
    -note: String
    -category: Category
    -wallet: Wallet
    +getType()* TransactionType
    +getSignedAmount()* double
    +printInfo()
  }

  class Income {
    -source: String
  }
  class Expense {
    -paymentMethod: String
  }
  class RecurringExpense {
    -period: Period
    +nextDueDate() LocalDate
  }

  Transaction <|-- Income
  Transaction <|-- Expense
  Expense <|-- RecurringExpense

  class Wallet {
    <<abstract>>
    -name: String
    -balance: double
    +deposit(amount)
    +withdraw(amount)
    +getWalletType()* WalletType
  }
  class CashWallet
  class BankAccount {
    -bankName: String
    -accountNumber: String
  }
  class EWallet {
    -provider: String
  }

  Wallet <|-- CashWallet
  Wallet <|-- BankAccount
  Wallet <|-- EWallet

  class Category {
    -name: String
    -type: TransactionType
  }
  class Budget {
    -category: Category
    -limit: double
    -period: Period
    +isExceeded(spent) boolean
  }

  class Storage {
    <<interface>>
    +save(transactions, path)
    +load(path) List~Transaction~
  }
  class CsvStorage
  class JsonStorage
  Storage <|.. CsvStorage
  Storage <|.. JsonStorage

  class ExpenseManager {
    -transactions: List
    -wallets: List
    -categories: List
    -budgets: Map
    -storage: Storage
    +monthlySummary()
    +statisticsByCategory()
  }

  Transaction --> Category
  Transaction --> Wallet
  Budget --> Category
  ExpenseManager o--> Storage
  ExpenseManager *--> Transaction
  ExpenseManager *--> Wallet
  ExpenseManager *--> Category
  ExpenseManager *--> Budget
```
=======
# Sơ đồ lớp (Class Diagram) — Personal Expense Manager

> File này dùng cú pháp Mermaid. Nếu đặt trong repo GitHub (đuôi `.md`), GitHub sẽ tự
> động render thành sơ đồ khi xem file trên web — không cần cài thêm công cụ nào.
> Có thể để nguyên file này trong repo, hoặc copy khối code bên dưới vào README.md.

```mermaid
classDiagram
    direction TB

    %% ===== ENUMS =====
    class TransactionType {
        <<enumeration>>
        INCOME
        EXPENSE
    }

    class WalletType {
        <<enumeration>>
        CASH
        BANK
        EWALLET
    }

    class Period {
        <<enumeration>>
        DAILY
        WEEKLY
        MONTHLY
        YEARLY
    }

    %% ===== TRANSACTION HIERARCHY =====
    class Transaction {
        <<abstract>>
        -String id
        -double amount
        -LocalDate date
        -String note
        -Category category
        -Wallet wallet
        +Transaction(id, amount, date, note, category, wallet)
        +getType() TransactionType*
        +getSignedAmount() double*
        +printInfo() void
        +getId() String
        +getAmount() double
        +setAmount(double) void
        +getDate() LocalDate
        +getNote() String
        +getCategory() Category
        +getWallet() Wallet
    }

    class Income {
        -String source
        +getType() TransactionType
        +getSignedAmount() double
    }

    class Expense {
        -String paymentMethod
        +getType() TransactionType
        +getSignedAmount() double
    }

    class RecurringExpense {
        -Period period
        +nextDueDate() LocalDate
    }

    Transaction <|-- Income
    Transaction <|-- Expense
    Expense <|-- RecurringExpense
    Transaction ..> TransactionType : uses
    RecurringExpense ..> Period : uses

    %% ===== WALLET HIERARCHY =====
    class Wallet {
        <<abstract>>
        -String name
        -double balance
        +deposit(double) void
        +withdraw(double) void
        +getWalletType() WalletType*
        +getName() String
        +getBalance() double
    }

    class CashWallet {
        +getWalletType() WalletType
    }

    class BankAccount {
        -String bankName
        -String accountNumber
        +withdraw(double) void
        +getWalletType() WalletType
    }

    class EWallet {
        -String provider
        +getWalletType() WalletType
    }

    Wallet <|-- CashWallet
    Wallet <|-- BankAccount
    Wallet <|-- EWallet
    Wallet ..> WalletType : uses

    %% ===== STORAGE (INTERFACE) =====
    class Storage {
        <<interface>>
        +save(List~Transaction~, String path) void
        +load(String path) List~Transaction~
    }

    class CsvStorage {
        +save(List~Transaction~, String path) void
        +load(String path) List~Transaction~
    }

    class JsonStorage {
        +save(List~Transaction~, String path) void
        +load(String path) List~Transaction~
    }

    Storage <|.. CsvStorage
    Storage <|.. JsonStorage

    %% ===== CATEGORY & BUDGET =====
    class Category {
        -String name
        -TransactionType type
    }

    class Budget {
        -Category category
        -double limitAmount
        -Period period
        +isExceeded(double spent) boolean
    }

    Budget --> Category : giới hạn
    Budget ..> Period : uses
    Category ..> TransactionType : uses

    %% ===== EXPENSE MANAGER (COORDINATOR) =====
    class ExpenseManager {
        -List~Transaction~ transactions
        -List~Wallet~ wallets
        -List~Category~ categories
        -Map~Category, Budget~ budgets
        -Storage storage
        +addTransaction(Transaction) void
        +removeTransaction(String id) void
        +updateTransaction(Transaction) void
        +findTransaction(...) List~Transaction~
        +monthlySummary() void
        +statisticsByCategory() Map~Category, Double~
    }

    ExpenseManager "1" o-- "*" Transaction : quản lý
    ExpenseManager "1" o-- "*" Wallet : quản lý
    ExpenseManager "1" o-- "*" Category : quản lý
    ExpenseManager "1" o-- "*" Budget : quản lý
    ExpenseManager --> Storage : sử dụng

    %% ===== PRESENTATION LAYER =====
    class ConsoleView {
        -ExpenseManager manager
        +showMenu() void
        +run() void
    }

    ConsoleView --> ExpenseManager : sử dụng
```

## Chú thích ký hiệu quan hệ

- `<|--` : kế thừa (extends)
- `<|..` : cài đặt interface (implements)
- `o--` : composition/aggregation (ExpenseManager "sở hữu" danh sách các đối tượng)
- `-->` : phụ thuộc/tham chiếu bình thường (association/dependency)
- `..>` : dependency nhẹ (chỉ dùng tới, ví dụ dùng enum)
- `*` sau tên phương thức trong lớp abstract/interface: phương thức trừu tượng

## Cách dùng cho bài nộp

1. Commit file `class-diagram.md` này vào thư mục gốc hoặc `docs/` của repo.
2. Xem trực tiếp trên GitHub (web) — Mermaid sẽ tự render thành hình.
3. Nếu cần ảnh tĩnh (PNG/SVG) để dán vào báo cáo Word/PDF, mở file này trên
   [Mermaid Live Editor](https://mermaid.live), dán nội dung trong khối ```mermaid```,
   rồi export ra PNG/SVG.
>>>>>>> d490f574385e112b2927f10a23b129acfaae3ed7
