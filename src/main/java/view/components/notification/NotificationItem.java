package view.components.notification;

import static util.Validator.checkNotNull;

import java.util.UUID;

import view.components.toast.ToastMode;

/**
 * Immutable notification payload shown as a {@link view.components.toast.Toast} in a {@link
 * ToastTray}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 * @param id unique identifier for this notification
 * @param mode toast severity to render
 * @param title required title text
 * @param description optional description text
 * @param actionLabel optional label for the action button
 * @param onAction optional callback for the action button
 */
public record NotificationItem(
    UUID id,
    ToastMode mode,
    String title,
    String description,
    String actionLabel,
    Runnable onAction
) {

  /**
   * Validates the required notification fields before the record is created.
   */
  public NotificationItem {
    checkNotNull(id, "Id");
    checkNotNull(mode, "Mode");
    checkNotNull(title, "Title");
  }
}
