package view.components.notification;

import static util.Validator.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import view.components.toast.Toast;

/**
 * A vertical tray that mirrors an {@link ObservableList} of {@link NotificationItem}: each item is
 * rendered as a styled {@link Toast}. List index {@code i} matches child index {@code i} (top to
 * bottom). New items appended with {@code list.add} appear at the bottom.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 */
public class ToastTray extends VBox {

  private final ObservableList<NotificationItem> items;
  private final Map<UUID, Node> idToNode = new HashMap<>();

  /**
   * Binds this tray to the given list. Existing items are shown immediately; subsequent changes
   * update children.
   *
   * @param items the source list (typically owned by {@link NotificationService})
   */
  public ToastTray(ObservableList<NotificationItem> items) {
    checkNotNull(items, "Items");
    this.items = items;
    setSpacing(8);
    setAlignment(Pos.TOP_RIGHT);

    for (int i = 0; i < items.size(); i++) {
      insertToastAt(items.get(i), i);
    }
    items.addListener(this::onItemsChanged);
  }

  /**
   * Mirrors additions and removals from the backing notification list into the tray children.
   *
   * @param c the list change describing inserted and removed notifications
   */
  private void onItemsChanged(ListChangeListener.Change<? extends NotificationItem> c) {
    while (c.next()) {
      if (c.wasRemoved()) {
        for (NotificationItem removed : c.getRemoved()) {
          Node node = idToNode.remove(removed.id());
          if (node != null) {
            getChildren().remove(node);
          }
        }
      }
      if (c.wasAdded()) {
        for (int i = c.getFrom(); i < c.getTo(); i++) {
          NotificationItem item = items.get(i);
          insertToastAt(item, i);
        }
      }
    }
  }

  /**
   * Creates and inserts a toast node for the given notification at the requested position.
   *
   * @param item the notification to render
   * @param index the child index where the toast should be inserted
   */
  private void insertToastAt(NotificationItem item, int index) {
    Toast toast =
        new Toast(
            item.mode(), item.title(), item.description(), item.actionLabel(), item.onAction());
    idToNode.put(item.id(), toast);
    getChildren().add(index, toast);
  }

  /**
   * Legacy toast style helper retained for tests and older callers during the CSS migration.
   */
  public static final class ToastNotificationStyles {

    private ToastNotificationStyles() {
    }

    /**
     * Builds the former shared border style for a toast container.
     *
     * @return inline JavaFX style string matching the CSS toast geometry
     */
    public static String toastBorderStyle() {
      return "-fx-background-radius: 8;"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 1.5;";
    }
  }
}
