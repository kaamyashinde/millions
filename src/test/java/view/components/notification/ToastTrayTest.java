package view.components.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import old_view.components.notification.NotificationItem;
import old_view.components.notification.ToastTray;
import old_view.components.toast.Toast;
import old_view.components.toast.ToastMode;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * ToastTray mirrors list order top-to-bottom: index 0 is the top child. New items appended with
 * {@code add} appear at the bottom.
 */
class ToastTrayTest {

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
  void emptyListHasNoChildren() throws Exception {
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(
        new ToastTray(FXCollections.observableArrayList())));
    assertEquals(0, trayRef.get().getChildren().size());
  }

  @Test
  void addOneItemCreatesOneToastChild() throws Exception {
    ObservableList<NotificationItem> list = FXCollections.observableArrayList();
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> {
      NotificationItem item =
          new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "Hello", null, null, null);
      list.add(item);
      trayRef.set(new ToastTray(list));
    });
    assertEquals(1, trayRef.get().getChildren().size());
    assertInstanceOf(Toast.class, trayRef.get().getChildren().getFirst());
  }

  @Test
  void trayCreatedBeforeItemsThenAddUpdatesChildren() throws Exception {
    ObservableList<NotificationItem> list = FXCollections.observableArrayList();
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(new ToastTray(list)));
    assertEquals(0, trayRef.get().getChildren().size());

    runOnFxAndWait(() -> list.add(
        new NotificationItem(UUID.randomUUID(), ToastMode.WARNING, "A", null, null, null)));
    assertEquals(1, trayRef.get().getChildren().size());

    runOnFxAndWait(() -> list.add(
        new NotificationItem(UUID.randomUUID(), ToastMode.ERROR, "B", null, null, null)));
    assertEquals(2, trayRef.get().getChildren().size());
  }

  @Test
  void listIndexMatchesChildOrderTopToBottom() throws Exception {
    ObservableList<NotificationItem> list = FXCollections.observableArrayList();
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> {
      list.add(new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "First", null, null, null));
      list.add(new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "Second", null, null, null));
      trayRef.set(new ToastTray(list));
    });
    assertEquals(2, trayRef.get().getChildren().size());
    assertInstanceOf(Toast.class, trayRef.get().getChildren().get(0));
    assertInstanceOf(Toast.class, trayRef.get().getChildren().get(1));
  }

  @Test
  void removeFromListRemovesChild() throws Exception {
    NotificationItem a =
        new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "A", null, null, null);
    ObservableList<NotificationItem> list = FXCollections.observableArrayList(a);
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(new ToastTray(list)));
    assertEquals(1, trayRef.get().getChildren().size());

    runOnFxAndWait(() -> list.remove(a));
    assertEquals(0, trayRef.get().getChildren().size());
  }

  @Test
  void clearListRemovesAllChildren() throws Exception {
    ObservableList<NotificationItem> list = FXCollections.observableArrayList();
    list.add(new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "A", null, null, null));
    list.add(new NotificationItem(UUID.randomUUID(), ToastMode.INFO, "B", null, null, null));
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(new ToastTray(list)));
    assertEquals(2, trayRef.get().getChildren().size());

    runOnFxAndWait(list::clear);
    assertEquals(0, trayRef.get().getChildren().size());
  }

  @Test
  void alignmentTopRight() throws Exception {
    ObservableList<NotificationItem> list = FXCollections.observableArrayList();
    AtomicReference<ToastTray> trayRef = new AtomicReference<>();
    runOnFxAndWait(() -> trayRef.set(new ToastTray(list)));
    assertEquals(Pos.TOP_RIGHT, trayRef.get().getAlignment());
  }

  @Test
  void toastNotificationStylesContainsGeometry() {
    String style = ToastTray.ToastNotificationStyles.toastBorderStyle();
    assertTrue(style.contains("-fx-border-width"));
    assertTrue(style.contains("8"));
  }
}
