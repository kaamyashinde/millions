package view;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Exchange;
import model.Player;
import model.Stock;
import model.persistence.CsvReader;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

/**
 * Demo application: <strong>Notifications</strong> tab hosts {@link NotificationsPanel};
 * <strong>Savings</strong> tab hosts {@link RegularSavingsPanel} with an {@link Exchange} loaded
 * from bundled CSV and {@link Player} "k" with starting balance 5000.
 *
 * @author kevindmazali
 * @version 1.3.0
 * @since 30-03-2026
 */
public class ToastDemoApp extends Application {

  private static final String DEMO_EXCHANGE_NAME = "NYSE";
  private static final String DEMO_CSV_RESOURCE = "/data/demo-stocks.csv";
  private static final int TICKER_PREVIEW_MAX = 8;

  @Override
  public void start(Stage stage) {
    NotificationService notifications = new NotificationService();

    List<Stock> stocks = loadStocksFromClasspath();
    NotificationsPanel notificationsPanel = new NotificationsPanel(notifications);

    Tab notificationsTab = new Tab("Notifications", notificationsPanel);
    notificationsTab.setClosable(false);

    Tab savingsTab;
    if (stocks.isEmpty()) {
      notifications.show(
          ToastMode.ERROR,
          "Failed to load stocks",
          "Missing or empty resource " + DEMO_CSV_RESOURCE + ".");
      Label error = new Label("Could not load demo-stocks.csv. Check resources.");
      error.setWrapText(true);
      savingsTab = new Tab("Savings", error);
    } else {
      Exchange demoExchange = new Exchange(DEMO_EXCHANGE_NAME, stocks);
      Player demoPlayer = new Player("k", new BigDecimal("5000"));
      showLoadedNotifications(notifications, demoExchange, stocks, demoPlayer);
      savingsTab =
          new Tab("Savings", new RegularSavingsPanel(demoExchange, demoPlayer, notifications));
    }
    savingsTab.setClosable(false);

    TabPane tabs = new TabPane(notificationsTab, savingsTab);

    stage.setScene(new Scene(tabs, 780, 560));
    stage.setTitle("Toast & Savings Demo");
    stage.show();
  }

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
