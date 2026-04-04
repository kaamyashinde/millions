package view;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import model.persistence.CsvReader;
import view.components.notification.NotificationService;
import view.components.notification.ToastTray;
import view.components.toast.ToastMode;

/**
 * Demo application: <strong>Notifications</strong> tab hosts {@link NotificationsPanel};
 * <strong>Stocks</strong> tab hosts {@link StocksListPanel}; <strong>Savings</strong> tab hosts
 * {@link RegularSavingsPanel} with an {@link Exchange} loaded from bundled CSV and {@link Player}
 * "k" with starting balance 5000. A heading and four toast-trigger buttons sit above the tabs;
 * toasts float over the top-right corner of the scene.
 *
 * @author kaamyashinde
 * @version 1.5.0
 * @since 30-03-2026
 */
public class ToastDemoApp extends Application {

  private static final String DEMO_EXCHANGE_NAME = "NYSE";
  private static final String DEMO_CSV_RESOURCE = "/data/demo-stocks.csv";
  private static final int TICKER_PREVIEW_MAX = 8;

  /** Long auto-dismiss for seeded rows so the stacked tray stays visible for layout checks. */
  private static final Duration SEED_DISPLAY_DURATION = Duration.minutes(10);

  private final NotificationService notifications = new NotificationService();
  private final StackPane toastArea = new StackPane();

  @Override
  public void start(Stage stage) {
    List<Stock> stocks = loadStocksFromClasspath();
    NotificationsPanel notificationsPanel = new NotificationsPanel(notifications);

    Tab notificationsTab = new Tab("Notifications", notificationsPanel);
    notificationsTab.setClosable(false);

    Tab stocksTab;
    Tab savingsTab;
    if (stocks.isEmpty()) {
      notifications.show(
          ToastMode.ERROR,
          "Failed to load stocks",
          "Missing or empty resource " + DEMO_CSV_RESOURCE + ".");
      Label error = new Label("Could not load demo-stocks.csv. Check resources.");
      error.setWrapText(true);
      Label stocksError =
          new Label("Could not load demo-stocks.csv. No listings to show.");
      stocksError.setWrapText(true);
      stocksTab = new Tab("Stocks", stocksError);
      savingsTab = new Tab("Savings", error);
    } else {
      Exchange demoExchange = new Exchange(DEMO_EXCHANGE_NAME, stocks);
      Player demoPlayer = new Player("k", new BigDecimal("5000"));
      showLoadedNotifications(notifications, demoExchange, stocks, demoPlayer);

      StocksListPanel stocksPanel = new StocksListPanel(demoExchange);
      stocksTab = new Tab("Stocks", stocksPanel);
      stocksTab.selectedProperty().addListener((obs, ov, nv) -> {
        if (Boolean.TRUE.equals(nv)) {
          stocksPanel.refresh();
        }
      });

      savingsTab =
          new Tab("Savings", new RegularSavingsPanel(demoExchange, demoPlayer, notifications));
    }
    stocksTab.setClosable(false);
    savingsTab.setClosable(false);

    LearningHubPanel learningHubPanel = new LearningHubPanel();
    Tab learningHubTab = new Tab("Learning Hub", learningHubPanel);
    learningHubTab.setClosable(false);

    TabPane tabs = new TabPane(notificationsTab, stocksTab, savingsTab, learningHubTab);

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

    VBox center = new VBox(14, heading, buttons, tabs);
    center.setAlignment(Pos.TOP_CENTER);
    center.setStyle("-fx-background-color: #121212;");

    ToastTray tray = new ToastTray(notifications.getItems());

    toastArea.setAlignment(Pos.TOP_RIGHT);
    toastArea.setPadding(new Insets(16, 16, 0, 0));
    toastArea.setMouseTransparent(true);
    toastArea.getChildren().add(tray);
    seedDemoNotifications();

    StackPane root = new StackPane(center, toastArea);

    stage.setScene(new Scene(root, 920, 600));
    stage.setTitle("Toast & Savings Demo");
    stage.show();
  }

  /**
   * Creates a styled button that, when clicked, enqueues a notification with the given parameters.
   *
   * @param label       the button text
   * @param mode        the toast severity mode (controls colour)
   * @param title       the notification title
   * @param description the notification body text
   * @param actionLabel the action button label shown on the toast, or {@code null} for none
   * @return the configured {@link Button}
   */
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

  /**
   * Enqueues one notification of each {@link ToastMode} (ERROR, WARNING, INFO, SUCCESS) with a
   * long display duration so the stacked tray remains visible for layout preview.
   */
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

  /**
   * Intentional no-op that discards the notification {@link UUID} returned by
   * {@link NotificationService#show} to suppress unused-return-value warnings.
   *
   * @param id the notification identifier to discard
   */
  @SuppressWarnings("unused")
  private static void discard(UUID id) {}

  private static List<Stock> loadStocksFromClasspath() {
    try (InputStream in = ToastDemoApp.class.getResourceAsStream(DEMO_CSV_RESOURCE)) {
      if (in == null) {
        return List.of();
      }
      return CsvReader.readCsv(in);
    } catch (IOException e) {
      return List.of();
    }
  }

  private static void showLoadedNotifications(
      NotificationService notifications,
      Exchange exchange,
      List<Stock> stocks,
      Player player) {
    String tickers =
        stocks.stream()
            .map(Stock::getSymbol)
            .limit(TICKER_PREVIEW_MAX)
            .collect(Collectors.joining(", "));
    if (stocks.size() > TICKER_PREVIEW_MAX) {
      tickers = tickers + ", …";
    }
    notifications.show(
        ToastMode.INFO,
        "Game loaded",
        exchange.getName()
            + " · "
            + stocks.size()
            + " stock(s): "
            + tickers);
    notifications.show(
        ToastMode.SUCCESS,
        "Player ready",
        player.getName() + " · balance " + player.getMoney().toPlainString());
  }

  public static void main(String[] args) {
    launch(args);
  }
}
