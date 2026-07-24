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
