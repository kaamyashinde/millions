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
 * @version 1.0.0
 * @since 2026-03-29
 */
public class NotificationService {

  private final ObservableList<NotificationItem> items = FXCollections.observableArrayList();
  private final Map<UUID, PauseTransition> pendingDismiss = new HashMap<>();
  private final Duration defaultAutoDismiss;

  /**
   * Creates a service with a default auto-dismiss delay of three seconds.
   */
  public NotificationService() {
    this(Duration.seconds(3));
  }

  /**
   * Creates a service with the given default auto-dismiss delay.
   *
   * @param defaultAutoDismiss the duration used by the convenience {@code show} overloads
   */
  public NotificationService(Duration defaultAutoDismiss) {
    this.defaultAutoDismiss = defaultAutoDismiss;
  }

  /**
   * Returns the default duration used for auto-dismissing notifications.
   *
   * @return the default auto-dismiss duration
   */
  public Duration getDefaultAutoDismiss() {
    return defaultAutoDismiss;
  }

  /**
   * List consumed by {@link ToastTray}. Do not add or remove entries except via this service.
   *
   * @return the observable notification list rendered by bound trays
   */
  public ObservableList<NotificationItem> getItems() {
    return items;
  }

  /**
   * Shows a notification with a title only, using the default auto-dismiss duration.
   *
   * @param mode the toast mode
   * @param title the notification title
   * @return the id assigned to the new notification
   */
  public UUID show(ToastMode mode, String title) {
    return show(mode, title, null, null, null, defaultAutoDismiss);
  }

  /**
   * Shows a notification with a title and description, using the default auto-dismiss duration.
   *
   * @param mode the toast mode
   * @param title the notification title
   * @param description the optional notification body text
   * @return the id assigned to the new notification
   */
  public UUID show(ToastMode mode, String title, String description) {
    return show(mode, title, description, null, null, defaultAutoDismiss);
  }

  /**
   * Shows a notification with an optional action, using the default auto-dismiss duration.
   *
   * @param mode the toast mode
   * @param title the notification title
   * @param description the optional notification body text
   * @param actionLabel the optional label for the action button
   * @param onAction the optional callback invoked when the action is triggered
   * @return the id assigned to the new notification
   */
  public UUID show(ToastMode mode, String title, String description, String actionLabel,
      Runnable onAction) {
    return show(mode, title, description, actionLabel, onAction, defaultAutoDismiss);
  }

  /**
   * Shows a notification and schedules its removal after {@code displayDuration}.
   *
   * @param mode the toast mode
   * @param title the notification title
   * @param description the optional notification body text
   * @param actionLabel the optional label for the action button
   * @param onAction the optional callback invoked when the action is triggered
   * @param displayDuration how long the notification should remain visible
   * @return the id for {@link #dismiss(UUID)}
   */
  public UUID show(ToastMode mode, String title, String description, String actionLabel,
      Runnable onAction, Duration displayDuration) {
    UUID id = UUID.randomUUID();
    NotificationItem item =
        new NotificationItem(id, mode, title, description, actionLabel, onAction);
    boolean _ = items.add(item);
    scheduleDismiss(id, displayDuration);
    return id;
  }

  /**
   * Starts the auto-dismiss timer for the given notification id.
   *
   * @param id the notification id whose timer should be tracked
   * @param displayDuration how long the notification should remain visible
   */
  private void scheduleDismiss(UUID id, Duration displayDuration) {
    PauseTransition pause = new PauseTransition(displayDuration);
    pause.setOnFinished(_ -> dismiss(id));
    pendingDismiss.put(id, pause);
    pause.play();
  }

  /**
   * Removes the notification and cancels its auto-dismiss timer if still pending.
   *
   * @param id the notification id to remove
   */
  public void dismiss(UUID id) {
    PauseTransition pause = pendingDismiss.remove(id);
    if (pause != null) {
      pause.stop();
    }
    items.removeIf(it -> it.id().equals(id));
  }

  /**
   * Removes all notifications and cancels all pending auto-dismiss timers.
   */
  public void clear() {
    pendingDismiss.values().forEach(PauseTransition::stop);
    pendingDismiss.clear();
    items.clear();
  }
}
