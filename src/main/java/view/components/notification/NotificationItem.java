package view.components.notification;

import static model.utils.Validator.checkNotNull;

import java.util.UUID;
import view.components.toast.ToastMode;

/**
 * Immutable notification payload shown as a {@link view.components.toast.Toast} in a {@link
 * ToastTray}.
 *
 * @author kaamyashinde
 */
public record NotificationItem(
    UUID id,
    ToastMode mode,
    String title,
    String description,
    String actionLabel,
    Runnable onAction
) {

  public NotificationItem {
    checkNotNull(id, "Id");
    checkNotNull(mode, "Mode");
    checkNotNull(title, "Title");
  }
}
