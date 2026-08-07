package com.example.personalexpensemanager.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 * Chạy tác vụ nặng ngoài luồng giao diện JavaFX.
 *
 * <p>Quy tắc chung (desktop / mobile / web đều giống ý này):
 * <ul>
 *   <li>Luồng UI chỉ vẽ và nhận thao tác người dùng — không được bị chặn bởi I/O.</li>
 *   <li>Đọc/ghi file, gọi mạng, xuất báo cáo → làm trên luồng nền.</li>
 *   <li>Khi xong, chỉ cập nhật UI từ luồng UI (ở đây: callback {@code onSuccess}/{@code onFailure}
 *       của {@link Task} đã chạy sẵn trên JavaFX Application Thread).</li>
 * </ul>
 */
public final class FxBackground {

  private FxBackground() {
  }

  /**
   * Chạy {@code work} trên thread nền; gọi {@code onSuccess}/{@code onFailure} trên luồng UI.
   *
   * @param daemon {@code true} thì JVM có thể thoát dù thread còn chạy (phù hợp load/export);
   *               {@code false} khi cần chắc chắn hoàn tất trước khi app tắt (phù hợp save lúc đóng).
   */
  public static <T> void run(
          String threadName,
          boolean daemon,
          Callable<T> work,
          Consumer<T> onSuccess,
          Consumer<Throwable> onFailure) {
    Task<T> task = new Task<>() {
      @Override
      protected T call() throws Exception {
        return work.call();
      }
    };

    task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
    task.setOnFailed(event -> {
      Throwable error = task.getException();
      onFailure.accept(error != null ? error : new IllegalStateException("Tác vụ nền thất bại"));
    });
    task.setOnCancelled(event ->
            onFailure.accept(new IllegalStateException("Tác vụ nền bị huỷ")));

    Thread thread = new Thread(task, threadName);
    thread.setDaemon(daemon);
    thread.start();
  }
}
