package view.pages.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import view.components.notification.NotificationItem;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

class NotificationHistoryPageTest {

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

  @Test
  void emptyHistoryShowsEmptyStateText() throws Exception {
    NotificationService service = new NotificationService(Duration.seconds(10));

    NotificationHistoryPage page =
        runOnFxThread(() -> new NotificationHistoryPage(service));

    assertTrue(page.getDisplayedNotifications().isEmpty());
    assertEquals("No notifications yet.", page.getEmptyStateText());
  }

  @Test
  void displayedRowsFollowNotificationHistoryNewestFirst() throws Exception {
    NotificationService service = new NotificationService(Duration.seconds(10));

    NotificationHistoryPage page = runOnFxThread(() -> {
      NotificationHistoryPage created = new NotificationHistoryPage(service);
      service.show(ToastMode.INFO, "Older", "First message");
      service.show(ToastMode.SUCCESS, "Newer", "Second message");
      return created;
    });

    List<NotificationItem> displayed = page.getDisplayedNotifications();
    assertEquals(2, displayed.size());
    assertEquals("Newer", displayed.getFirst().title());
    assertEquals("Older", displayed.get(1).title());
  }

  @Test
  void clearButtonEmptiesNotificationHistory() throws Exception {
    NotificationService service = new NotificationService(Duration.seconds(10));

    NotificationHistoryPage page = runOnFxThread(() -> {
      NotificationHistoryPage created = new NotificationHistoryPage(service);
      service.show(ToastMode.WARNING, "Heads up", "Something happened");
      new Scene(created);
      created.applyCss();
      created.layout();
      return created;
    });

    runOnFxThread(() -> {
      Button clearButton = (Button) page.lookup("#clear-notifications-button");
      assertNotNull(clearButton);
      clearButton.fire();
      return null;
    });

    assertTrue(page.getDisplayedNotifications().isEmpty());
    assertTrue(service.getHistoryItems().isEmpty());
  }

  private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(() -> {
      try {
        ref.set(supplier.get());
      } catch (Exception exception) {
        err.set(exception);
      } finally {
        latch.countDown();
      }
    });
    latch.await(5, TimeUnit.SECONDS);
    if (err.get() != null) {
      throw err.get();
    }
    return ref.get();
  }

  @FunctionalInterface
  private interface FxSupplier<T> {
    T get() throws Exception;
  }
}
