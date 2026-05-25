package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import controller.FundsController;
import controller.TradingController;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.player.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import view.components.notification.NotificationService;
import view.pages.funds.FundsPage;

class FundsPageTest {

  @BeforeAll
  static void initJavaFx() throws InterruptedException {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.startup(latch::countDown);
      latch.await(5, TimeUnit.SECONDS);
    } catch (IllegalStateException ignored) {
      // toolkit already running
    }
  }

  @Test
  void searchFieldFiltersBySymbolAndFundName() throws Exception {
    Stock apple = stock("AAPL", "Apple Inc.", "150.00");
    Stock microsoft = stock("MSFT", "Microsoft", "300.00");
    Fund tech = fund("TECHX", "Tech Titans Blend Fund", apple, microsoft);
    Fund growth = fund("GROW", "Global Growth Fund", apple, microsoft);
    Exchange exchange = new Exchange.Builder("NYSE")
        .stocks(List.of(apple, microsoft))
        .funds(List.of(tech, growth))
        .build();

    FundsPage page = runOnFxThread(() -> createPage(exchange));
    layout(page);
    @SuppressWarnings("unchecked")
    TableView<Fund> table = (TableView<Fund>) fundTable(page);
    TextField search = (TextField) page.lookup("#funds-search-field");

    runOnFxThread(
        () -> {
          search.setText("tech");
          return page;
        });

    assertEquals(1, table.getItems().size());
    assertEquals("TECHX", table.getItems().getFirst().getSymbol());
    assertEquals("TECHX", page.getDetailView().getSelectedFund().getSymbol());

    runOnFxThread(
        () -> {
          search.setText("zz");
          return page;
        });

    assertEquals(0, table.getItems().size());
    assertNull(page.getDetailView().getSelectedFund());

    runOnFxThread(
        () -> {
          search.clear();
          return page;
        });

    assertEquals(2, table.getItems().size());
    assertEquals("GROW", page.getDetailView().getSelectedFund().getSymbol());
  }

  private static FundsPage createPage(Exchange exchange) {
    FundsController funds = new FundsController(exchange);
    TradingController trading =
        new TradingController(exchange, new Player("tester", new BigDecimal("10000.00")),
            new NotificationService());
    return new FundsPage(funds, trading, () -> {});
  }

  private static TableView<?> fundTable(FundsPage page) {
    SplitPane splitPane = (SplitPane) page.getCenter();
    return (TableView<?>) splitPane.getItems().getFirst();
  }

  private static void layout(FundsPage page) {
    if (page.getScene() == null) {
      new Scene(page, 900, 650);
    }
    page.applyCss();
    page.layout();
  }

  private static Stock stock(String symbol, String company, String price) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(price));
    return stock;
  }

  private static Fund fund(String symbol, String name, Stock first, Stock second) {
    return new Fund(
        symbol,
        name,
        List.of(
            new FundComponent(first, new BigDecimal("0.50")),
            new FundComponent(second, new BigDecimal("0.50"))));
  }

  private static FundsPage runOnFxThread(PageSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<FundsPage> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            ref.set(supplier.get());
          } catch (Exception exception) {
            err.set(exception);
          } finally {
            latch.countDown();
          }
        });
    latch.await(5, TimeUnit.SECONDS);
    if (err.get() != null) {
      throw err.get();
    }
    return ref.get();
  }

  @FunctionalInterface
  private interface PageSupplier {
    FundsPage get() throws Exception;
  }
}
