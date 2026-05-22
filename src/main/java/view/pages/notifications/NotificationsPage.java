package view.pages.notifications;

import controller.NotificationsController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import view.components.notification.ToastTray;
import view.components.toast.ToastMode;
import view.theme.ThemeStyles;

/**
 * Demo page for notification service: trigger sample toasts and clear the list.
 */
public class NotificationsPage extends BorderPane {

  /**
   * @param controller notifications tab controller
   */
  public NotificationsPage(NotificationsController controller) {
    var notifications = controller.getNotificationService();
    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Notifications");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    Label daysLabel = new Label("Days:");
    ThemeStyles.addStyleClasses(daysLabel, "muted-text");
    TextField daysField = new TextField();
    daysField.setPromptText("e.g. 14");
    daysField.setPrefWidth(100);
    ThemeStyles.styleField(daysField);

    Button advanceWeeksBtn = new Button("Advance by weeks");
    ThemeStyles.styleButton(advanceWeeksBtn);

    HBox advanceRow = new HBox(12, daysLabel, daysField, advanceWeeksBtn);
    advanceRow.setAlignment(Pos.CENTER);

    Button errBtn = makeButton(notifications, "Error", ToastMode.ERROR,
        "Something went wrong!", "Check the logs for more details.", "Dismiss");
    Button warnBtn = makeButton(notifications, "Warning", ToastMode.WARNING,
        "Low balance", "Your portfolio is below the threshold.", null);
    Button infoBtn = makeButton(notifications, "Info", ToastMode.INFO,
        "Market opens soon", "Trading resumes in 30 minutes.", null);
    Button successBtn = makeButton(notifications, "Success", ToastMode.SUCCESS,
        "Trade executed", "10 AAPL shares purchased.", "View");

    Button clearBtn = new Button("Clear all");
    ThemeStyles.styleButton(clearBtn);
    clearBtn.setOnAction(_ -> notifications.clear());

    HBox buttons = new HBox(14, errBtn, warnBtn, infoBtn, successBtn, clearBtn);
    buttons.setAlignment(Pos.CENTER);
    ThemeStyles.addStyleClasses(buttons, "finance-notification-actions");

    VBox top = new VBox(28, heading, advanceRow, buttons);
    top.setAlignment(Pos.CENTER);
    setTop(top);

    ToastTray tray = new ToastTray(notifications.getItems());
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox trayRow = new HBox(spacer, tray);
    trayRow.setAlignment(Pos.TOP_RIGHT);

    ScrollPane scroll = new ScrollPane(trayRow);
    scroll.setFitToWidth(true);
    BorderPane.setMargin(scroll, new Insets(24, 0, 0, 0));
    setCenter(scroll);
  }

  private static Button makeButton(
      view.components.notification.NotificationService notifications,
      String label,
      ToastMode mode,
      String title,
      String description,
      String actionLabel) {
    Button btn = new Button(label);
    btn.setPrefWidth(110);
    ThemeStyles.styleButton(btn);
    btn.setOnAction(_ -> {
      if (actionLabel == null) {
        notifications.show(mode, title, description);
      } else {
        notifications.show(mode, title, description, actionLabel, null);
      }
    });
    return btn;
  }
}
