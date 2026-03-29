package view.components.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import view.components.toast.ToastMode;

class NotificationServiceTest {

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }

  private void runOnFxAndWait(Runnable r) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        r.run();
      } catch (Exception e) {
        err.set(e);
      } finally {
        latch.countDown();
      }
    });
    latch.await(5, TimeUnit.SECONDS);
    if (err.get() != null) {
      throw err.get();
    }
  }

  @Test
  void showAddsOneItem() throws Exception {
    NotificationService service = new NotificationService(Duration.millis(500));
    runOnFxAndWait(() -> service.show(ToastMode.INFO, "Hi"));
    AtomicInteger size = new AtomicInteger();
    runOnFxAndWait(() -> size.set(service.getItems().size()));
    assertEquals(1, size.get());
  }

  @Test
  void autoDismissRemovesItemAndEmptiesTray() throws Exception {
    NotificationService service = new NotificationService(Duration.millis(80));
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(new ToastTray(service.getItems())));
    runOnFxAndWait(
        () -> service.show(ToastMode.SUCCESS, "Done", null, null, null, Duration.millis(80)));

    Thread.sleep(250);

    AtomicInteger listSize = new AtomicInteger();
    AtomicInteger childCount = new AtomicInteger();
    runOnFxAndWait(() -> {
      listSize.set(service.getItems().size());
      childCount.set(trayRef.get().getChildren().size());
    });
    assertEquals(0, listSize.get());
    assertEquals(0, childCount.get());
  }

  @Test
  void dismissBeforeTimeoutRemovesItem() throws Exception {
    NotificationService service = new NotificationService(Duration.seconds(30));
    AtomicReference<UUID> idRef = new AtomicReference<>();
    runOnFxAndWait(() -> idRef.set(service.show(ToastMode.WARNING, "Stay")));
    AtomicInteger sizeBefore = new AtomicInteger();
    runOnFxAndWait(() -> sizeBefore.set(service.getItems().size()));
    assertEquals(1, sizeBefore.get());

    runOnFxAndWait(() -> service.dismiss(idRef.get()));

    AtomicInteger sizeAfter = new AtomicInteger();
    runOnFxAndWait(() -> sizeAfter.set(service.getItems().size()));
    assertEquals(0, sizeAfter.get());
  }

  @Test
  void clearRemovesAll() throws Exception {
    NotificationService service = new NotificationService(Duration.seconds(10));
    runOnFxAndWait(() -> {
      service.show(ToastMode.INFO, "A");
      service.show(ToastMode.INFO, "B");
    });
    AtomicInteger mid = new AtomicInteger();
    runOnFxAndWait(() -> mid.set(service.getItems().size()));
    assertEquals(2, mid.get());

    runOnFxAndWait(service::clear);

    AtomicInteger end = new AtomicInteger();
    runOnFxAndWait(() -> end.set(service.getItems().size()));
    assertEquals(0, end.get());
  }

  @Test
  void defaultDurationIsThreeSeconds() {
    NotificationService service = new NotificationService();
    assertEquals(Duration.seconds(3), service.getDefaultAutoDismiss());
  }

  @Test
  void dismissIsIdempotent() throws Exception {
    NotificationService service = new NotificationService(Duration.millis(50));
    AtomicReference<UUID> idRef = new AtomicReference<>();
    runOnFxAndWait(() -> idRef.set(service.show(ToastMode.ERROR, "E")));
    runOnFxAndWait(() -> service.dismiss(idRef.get()));
    runOnFxAndWait(() -> service.dismiss(idRef.get()));
    AtomicInteger size = new AtomicInteger();
    runOnFxAndWait(() -> size.set(service.getItems().size()));
    assertEquals(0, size.get());
  }
}
