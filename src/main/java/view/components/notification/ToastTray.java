package view.components.notification;

import static model.utils.Validator.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import view.components.toast.Toast;
import view.components.toast.ToastMode;

/**
 * A vertical tray that mirrors an {@link ObservableList} of {@link NotificationItem}: each item is
 * rendered as a styled {@link Toast}. List index {@code i} matches child index {@code i} (top to
 * bottom). New items appended with {@code list.add} appear at the bottom.
 *
 * @author kaamyashinde
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

  private void insertToastAt(NotificationItem item, int index) {
    Toast toast =
        new Toast(item.mode(), item.title(), item.description(), item.actionLabel(), item.onAction());
    toast.setStyle(ToastNotificationStyles.toastBorderStyle(item.mode()));
    idToNode.put(item.id(), toast);
    getChildren().add(index, toast);
  }

  /**
   * Shared toast chrome (background, border) aligned with {@link view.ToastDemoApp}.
   */
  public static final class ToastNotificationStyles {

    private ToastNotificationStyles() {
    }

    public static String toastBorderStyle(ToastMode mode) {
      return "-fx-background-color: #1e1e1e;"
          + "-fx-background-radius: 8;"
          + "-fx-border-color: " + mode.getColorHex() + ";"
          + "-fx-border-radius: 8;"
          + "-fx-border-width: 1.5;";
    }
  }
}
