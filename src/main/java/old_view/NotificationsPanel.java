package old_view;

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
import old_view.components.notification.NotificationService;
import old_view.components.notification.ToastTray;
import old_view.components.toast.ToastMode;

/**
 * Demo panel for {@link NotificationService} and {@link ToastTray}: trigger sample notifications,
 * clear the list, and view the stacked tray in-tab (no global overlay).
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
public class NotificationsPanel extends BorderPane {

  private static final String FIELD_STYLE =
      "-fx-border-radius: 6;"
          + "-fx-background-radius: 6;";

  private final NotificationService notifications;

  /**
   * Builds a panel wired to the given notification service.
   *
   * @param notifications service that owns the list bound to the tray
   */
  public NotificationsPanel(NotificationService notifications) {
    this.notifications = notifications;

    setPadding(new Insets(16));

    Text heading = new Text("Notifications");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));

    Label daysLabel = new Label("Days:");

    TextField daysField = new TextField();
    daysField.setPromptText("e.g. 14");
    daysField.setPrefWidth(100);
    daysField.setStyle(FIELD_STYLE);

    Button advanceWeeksBtn = new Button("Advance by weeks");
    styleButton(advanceWeeksBtn);
    advanceWeeksBtn.setOnAction(_ -> {});

    HBox advanceRow = new HBox(12, daysLabel, daysField, advanceWeeksBtn);
    advanceRow.setAlignment(Pos.CENTER);

    Button errBtn = makeButton("Error", ToastMode.ERROR,
        "Something went wrong!", "Check the logs for more details.", "Dismiss");
    Button warnBtn = makeButton("Warning", ToastMode.WARNING,
        "Low balance", "Your portfolio is below the threshold.", null);
    Button infoBtn = makeButton("Info", ToastMode.INFO,
        "Market opens soon", "Trading resumes in 30 minutes.", null);
    Button successBtn = makeButton("Success", ToastMode.SUCCESS,
        "Trade executed", "10 AAPL shares purchased.", "View");

    Button clearBtn = new Button("Clear all");
    styleButton(clearBtn);
    clearBtn.setOnAction(_ -> notifications.clear());

    HBox buttons = new HBox(14, errBtn, warnBtn, infoBtn, successBtn, clearBtn);
    buttons.setAlignment(Pos.CENTER);

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
    scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    BorderPane.setMargin(scroll, new Insets(24, 0, 0, 0));
    setCenter(scroll);
  }

  private Button makeButton(String label, ToastMode mode, String title, String description,
      String actionLabel) {
    Button btn = new Button(label);
    btn.setPrefWidth(110);
    styleButton(btn);
    btn.setOnAction(_ -> {
      if (actionLabel == null) {
        notifications.show(mode, title, description);
      } else {
        notifications.show(mode, title, description, actionLabel, null);
      }
    });
    return btn;
  }

  private static void styleButton(Button b) {
    b.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }
}
