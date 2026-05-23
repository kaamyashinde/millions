package view.components.notification;

import static model.utils.Validator.checkNotNull;

import model.core.player.Player;
import model.core.player.level.PlayerLevel;
import model.core.player.PlayerObserver;
import view.components.toast.ToastMode;

/**
 * View-layer observer that fires a toast notification via {@link NotificationService}
 * when a player's level changes. Tracks the previous level so that only actual
 * transitions trigger a notification.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class LevelUpNotificationObserver implements PlayerObserver {

  private final NotificationService notifications;
  private PlayerLevel previousLevel;

  /**
   * Creates a notification observer that will track level changes from the given initial level.
   *
   * @param notifications the notification service used to display toasts
   * @param initialLevel  the player's level at the time this observer is registered
   */
  public LevelUpNotificationObserver(NotificationService notifications, PlayerLevel initialLevel) {
    checkNotNull(notifications, "notifications");
    checkNotNull(initialLevel, "initialLevel");
    this.notifications = notifications;
    this.previousLevel = initialLevel;
  }

  /**
   * Checks whether the player's level has changed since the last notification,
   * and if so, shows a congratulatory toast.
   *
   * @param player the player whose state changed
   */
  @Override
  public void onPlayerStateChanged(Player player) {
    PlayerLevel currentLevel = player.getPlayerLevel();
    if (currentLevel != previousLevel) {
      notifications.show(
          ToastMode.SUCCESS,
          "Level up!",
          player.getName() + " is now a " + currentLevel.name());
      previousLevel = currentLevel;
    }
  }
}
