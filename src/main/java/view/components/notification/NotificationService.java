package view.components.notification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import view.components.toast.ToastMode;

/**
 * Owns the notification list and schedules auto-dismiss. Bind a {@link ToastTray} to {@link
 * #getItems()} to display toasts. Mutate notifications through this service so timers stay
 * consistent.
 *
 * @author kaamyashinde
 */
public class NotificationService {

  private final ObservableList<NotificationItem> items = FXCollections.observableArrayList();
  private final Map<UUID, PauseTransition> pendingDismiss = new HashMap<>();
  private final Duration defaultAutoDismiss;

  public NotificationService() {
    this(Duration.seconds(3));
  }

  public NotificationService(Duration defaultAutoDismiss) {
    this.defaultAutoDismiss = defaultAutoDismiss;
  }

  public Duration getDefaultAutoDismiss() {
    return defaultAutoDismiss;
  }

  /**
   * List consumed by {@link ToastTray}. Do not add or remove entries except via this service.
   */
  public ObservableList<NotificationItem> getItems() {
    return items;
  }

  public UUID show(ToastMode mode, String title) {
    return show(mode, title, null, null, null, defaultAutoDismiss);
  }

  public UUID show(ToastMode mode, String title, String description) {
    return show(mode, title, description, null, null, defaultAutoDismiss);
  }

  public UUID show(ToastMode mode, String title, String description, String actionLabel,
      Runnable onAction) {
    return show(mode, title, description, actionLabel, onAction, defaultAutoDismiss);
  }

  /**
   * Shows a notification and schedules its removal after {@code displayDuration}.
   *
   * @return the id for {@link #dismiss(UUID)}
   */
  public UUID show(ToastMode mode, String title, String description, String actionLabel,
      Runnable onAction, Duration displayDuration) {
    UUID id = UUID.randomUUID();
    NotificationItem item =
        new NotificationItem(id, mode, title, description, actionLabel, onAction);
    items.add(item);
    scheduleDismiss(id, displayDuration);
    return id;
  }

  private void scheduleDismiss(UUID id, Duration displayDuration) {
    PauseTransition pause = new PauseTransition(displayDuration);
    pause.setOnFinished(e -> dismiss(id));
    pendingDismiss.put(id, pause);
    pause.play();
  }

  /**
   * Removes the notification and cancels its auto-dismiss timer if still pending.
   */
  public void dismiss(UUID id) {
    PauseTransition pause = pendingDismiss.remove(id);
    if (pause != null) {
      pause.stop();
    }
    items.removeIf(it -> it.id().equals(id));
  }

  public void clear() {
    for (PauseTransition pause : pendingDismiss.values()) {
      pause.stop();
    }
    pendingDismiss.clear();
    items.clear();
  }
}
