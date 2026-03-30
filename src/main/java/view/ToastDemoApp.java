package view;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Exchange;
import model.Player;
import model.Stock;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;
import view.components.toast.ToastMode;

/**
 * Demo application: <strong>Toasts</strong> tab showcases {@link NotificationService} and
 * {@link ToastTray}; <strong>Savings</strong> tab hosts {@link RegularSavingsPanel} with a demo
 * {@link Exchange} and {@link Player}. Sample notifications seed the tray on startup for layout
 * preview.
 *
 * @author kevindmazali
 * @version 1.1.0
 * @since 30-03-2026
 */
public class ToastDemoApp extends Application {

  /** Long auto-dismiss for seeded rows so the stacked tray stays visible for layout checks. */
  private static final Duration SEED_DISPLAY_DURATION = Duration.minutes(10);

  private final NotificationService notifications = new NotificationService();
  private final StackPane toastArea = new StackPane();

  @Override
  public void start(Stage stage) {
    Text heading = new Text("Toast Demo");
    heading.setFont(Font.font("System", FontWeight.BOLD, 26));
    heading.setFill(Color.web("#e0e0e0"));

    Label daysLabel = new Label("Days:");
    daysLabel.setTextFill(Color.web("#e0e0e0"));

    TextField daysField = new TextField();
    daysField.setPromptText("e.g. 14");
    daysField.setPrefWidth(100);
    daysField.setStyle(
        "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: #e0e0e0;"
            + "-fx-prompt-text-fill: #888888;"
            + "-fx-border-color: #3d3d3d;"
            + "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
    );

    Button advanceWeeksBtn = new Button("Advance by weeks");
    advanceWeeksBtn.setStyle(
        "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: #e0e0e0;"
            + "-fx-border-color: #3d3d3d;"
            + "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
    );
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

    HBox buttons = new HBox(14, errBtn, warnBtn, infoBtn, successBtn);
    buttons.setAlignment(Pos.CENTER);

    VBox toastContent = new VBox(28, heading, advanceRow, buttons);
    toastContent.setAlignment(Pos.CENTER);
    toastContent.setPadding(new Insets(40));

    Exchange demoExchange = createDemoExchange();
    Player demoPlayer = new Player("Demo", new BigDecimal("100000"));
    RegularSavingsPanel savingsPanel =
        new RegularSavingsPanel(demoExchange, demoPlayer, notifications);

    Tab toastTab = new Tab("Toasts", toastContent);
    toastTab.setClosable(false);
    Tab savingsTab = new Tab("Savings", savingsPanel);
    savingsTab.setClosable(false);

    TabPane tabs = new TabPane(toastTab, savingsTab);
    tabs.setStyle("-fx-background-color: #121212;");
    StackPane.setAlignment(tabs, Pos.CENTER);

    ToastTray tray = new ToastTray(notifications.getItems());

    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);
    toastArea.getChildren().add(tray);
    seedDemoNotifications();

    StackPane root = new StackPane(tabs, toastArea);
    root.setStyle("-fx-background-color: #121212;");

    stage.setScene(new Scene(root, 780, 560));
    stage.setTitle("Toast & Savings Demo");
    stage.show();
  }

  /**
   * Demo exchange with two liquid stocks for savings symbol validation.
   *
   * @return a non-null exchange at trading day 1
   */
  private static Exchange createDemoExchange() {
    Stock aapl = new Stock("AAPL", "Apple Inc.");
    aapl.addNewSalesPrice(new BigDecimal("100.00"));
    Stock msft = new Stock("MSFT", "Microsoft Corp.");
    msft.addNewSalesPrice(new BigDecimal("200.00"));
    return new Exchange("DEMO", List.of(aapl, msft));
  }

  private Button makeButton(String label, ToastMode mode, String title, String description,
      String actionLabel) {
    Button btn = new Button(label);
    btn.setPrefWidth(110);
    btn.setStyle(
        "-fx-background-color: #2a2a2a;"
            + "-fx-text-fill: " + mode.getColorHex() + ";"
            + "-fx-border-color: " + mode.getColorHex() + ";"
            + "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;"
    );
    btn.setOnAction(_ -> {
      if (actionLabel == null) {
        discard(notifications.show(mode, title, description));
      } else {
        discard(notifications.show(mode, title, description, actionLabel, null));
      }
    });
    return btn;
  }

  private void seedDemoNotifications() {
    discard(notifications.show(ToastMode.ERROR, "Something went wrong!",
        "Check the logs for more details.", "Dismiss", null, SEED_DISPLAY_DURATION));
    discard(notifications.show(ToastMode.WARNING, "Low balance",
        "Your portfolio is below the threshold.", null, null, SEED_DISPLAY_DURATION));
    discard(notifications.show(ToastMode.INFO, "Market opens soon",
        "Trading resumes in 30 minutes.", null, null, SEED_DISPLAY_DURATION));
    discard(notifications.show(ToastMode.SUCCESS, "Trade executed",
        "10 AAPL shares purchased.", "View", null, SEED_DISPLAY_DURATION));
  }

  @SuppressWarnings("unused")
  private static void discard(UUID id) {}

  public static void main(String[] args) {
    launch(args);
  }
}
