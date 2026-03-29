package view;

import java.math.BigDecimal;
import java.util.UUID;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Stock;
import view.components.chart.StockChart;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;
import view.components.toast.ToastMode;

/**
 * A small demo application that showcases toast notifications via {@link NotificationService} and
 * {@link ToastTray}. On startup, sample notifications fill the tray for layout preview (long
 * auto-dismiss). Buttons still enqueue with the default 3 second dismiss.
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

    StockChart chart = new StockChart(buildSeedStock());
    chart.setPrefHeight(280);

    VBox center = new VBox(28, heading, chart, buttons);
    center.setAlignment(Pos.CENTER);
    center.setPadding(new Insets(40));

    ToastTray tray = new ToastTray(notifications.getItems());

    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);
    toastArea.getChildren().add(tray);
    seedDemoNotifications();

    StackPane root = new StackPane(center, toastArea);
    root.setStyle("-fx-background-color: #121212;");

    stage.setScene(new Scene(root, 800, 560));
    stage.setTitle("Toast Notification Demo");
    stage.show();
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

  private static Stock buildSeedStock() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    double[] prices = {
        150.00, 151.20, 149.80, 152.50, 153.10,
        151.75, 154.30, 155.00, 153.60, 156.20,
        157.40, 155.90, 158.00, 159.30, 157.80,
        160.10, 161.50, 159.90, 162.00, 163.25
    };
    for (double p : prices) {
      stock.addNewSalesPrice(BigDecimal.valueOf(p));
    }
    return stock;
  }

  @SuppressWarnings("unused")
  private static void discard(UUID id) {}

  public static void main(String[] args) {
    launch(args);
  }
}
