package view.components.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import old_view.components.notification.NotificationItem;
import old_view.components.toast.ToastMode;

class NotificationItemTest {

  @Test
  void validMinimalItem() {
    UUID id = UUID.randomUUID();
    NotificationItem item = new NotificationItem(id, ToastMode.INFO, "Title", null, null, null);
    assertEquals(id, item.id());
    assertEquals(ToastMode.INFO, item.mode());
    assertEquals("Title", item.title());
    assertNull(item.description());
    assertNull(item.actionLabel());
    assertNull(item.onAction());
  }

  @Test
  void validFullItem() {
    UUID id = UUID.randomUUID();
    Runnable action = () -> { };
    NotificationItem item =
        new NotificationItem(id, ToastMode.SUCCESS, "Done", "Details", "OK", action);
    assertEquals("Details", item.description());
    assertEquals("OK", item.actionLabel());
    assertNotNull(item.onAction());
  }

  @Test
  void nullIdThrows() {
    assertThrows(NullPointerException.class,
        () -> new NotificationItem(null, ToastMode.ERROR, "T", null, null, null));
  }

  @Test
  void nullModeThrows() {
    assertThrows(NullPointerException.class,
        () -> new NotificationItem(UUID.randomUUID(), null, "T", null, null, null));
  }

  @Test
  void nullTitleThrows() {
    assertThrows(NullPointerException.class,
        () -> new NotificationItem(UUID.randomUUID(), ToastMode.WARNING, null, null, null, null));
  }
}
