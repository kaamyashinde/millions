package view;

import java.math.BigDecimal;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import model.Exchange;
import model.Player;
import model.Stock;
import view.components.notification.NotificationService;

/**
 * Demo application: <strong>Notifications</strong> tab hosts {@link NotificationsPanel};
 * <strong>Savings</strong> tab hosts {@link RegularSavingsPanel} with a demo {@link Exchange} and
 * {@link Player}.
 *
 * @author kevindmazali
 * @version 1.2.0
 * @since 30-03-2026
 */
public class ToastDemoApp extends Application {

  @Override
  public void start(Stage stage) {
    NotificationService notifications = new NotificationService();

    Exchange demoExchange = createDemoExchange();
    Player demoPlayer = new Player("Demo", new BigDecimal("100000"));
    NotificationsPanel notificationsPanel = new NotificationsPanel(notifications);
    RegularSavingsPanel savingsPanel =
        new RegularSavingsPanel(demoExchange, demoPlayer, notifications);

    Tab notificationsTab = new Tab("Notifications", notificationsPanel);
    notificationsTab.setClosable(false);
    Tab savingsTab = new Tab("Savings", savingsPanel);
    savingsTab.setClosable(false);

    TabPane tabs = new TabPane(notificationsTab, savingsTab);

    stage.setScene(new Scene(tabs, 780, 560));
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

  public static void main(String[] args) {
    launch(args);
  }
}
