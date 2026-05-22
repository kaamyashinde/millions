package controller;

import view.components.notification.NotificationService;

/**
 * Wraps {@link NotificationService} for the notifications tab and workspace toast tray.
 */
public class NotificationsController {

  private final NotificationService notificationService;

  /**
   * @param notificationService session-scoped notification service
   */
  public NotificationsController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  /**
   * @return the underlying notification service
   */
  public NotificationService getNotificationService() {
    return notificationService;
  }
}
