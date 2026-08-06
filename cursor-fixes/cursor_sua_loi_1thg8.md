## Tại sao có 2 hàm main ( PersonalExpenseManagerApp.main với Launcher.main )

---

Vì hai hàm đó phục vụ hai cách chạy khác nhau, và chỉ một trong hai thực sự khởi động JavaFX.

`PersonalExpenseManagerApp.main` là hàm main **thật**: nó gọi `launch(args)`, tức là câu lệnh duy nhất khởi động được JavaFX runtime. `Launcher.main` không làm gì cả ngoài việc gọi sang hàm trên — nó chỉ là một lớp vỏ.

Lý do phải có lớp vỏ đó: JVM có một luật riêng cho JavaFX. Khi lớp chứa `main` **kế thừa `Application`** (đúng như `PersonalExpenseManagerApp`), JVM kiểm tra JavaFX runtime có nằm trên module path không, thiếu là nó chặn ngay với `Error: JavaFX runtime components are missing`. `Launcher` không kế thừa `Application` nên không bị kiểm tra, chạy bình thường rồi mới gọi sang lớp kia — lúc này JavaFX đã được nạp qua đường thông thường.

Vì thế `pom.xml` dòng 76 trỏ vào `Launcher`, không trỏ vào `PersonalExpenseManagerApp`:

```76:76:pom.xml
              <mainClass>com.example.personalexpensemanager/com.example.personalexpensemanager.Launcher</mainClass>
```

Còn `main` trong `PersonalExpenseManagerApp` được giữ lại để bạn bấm Run thẳng vào lớp đó trong IntelliJ cũng lên được (IntelliJ tự cấu hình module path giúp), và vì đó là quy ước chuẩn của mọi project JavaFX.

Bỏ bớt một cái được không? Được, nhưng mỗi hướng đều mất một thứ: xoá `Launcher` thì phải sửa `pom.xml` trỏ thẳng vào `PersonalExpenseManagerApp` và mất lớp bảo hiểm khi đóng gói jar; xoá `main` trong `PersonalExpenseManagerApp` thì `Launcher` phải tự gọi `Application.launch(PersonalExpenseManagerApp.class, args)`. Giữ cả hai như hiện tại là cách phổ biến nhất, tổng cộng đúng 3 dòng code.