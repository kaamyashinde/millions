package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.Exchange;
import model.Player;
import model.Stock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the player metrics panel state and refresh behavior.
 */
class PlayerPortfolioPanelTest {

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
  void emptyPlayerShowsNoTradesAndNoHoldings() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00");
    Exchange exchange = new Exchange("NYSE", List.of(apple));
    Player player = new Player("k", new BigDecimal("5000.00"));

    PlayerPortfolioPanel panel =
        runOnFxThread(() -> new PlayerPortfolioPanel(exchange, player, Path.of("/no/avatar.png")));

    assertEquals("k", panel.getDisplayedPlayerName());
    assertEquals("5000.00", panel.getDisplayedBalance());
    assertEquals(0, panel.getHoldingCount());
    assertEquals("N/A (no trades yet)", panel.getPortfolioReturnText());
    assertEquals("N/A (need more history)", panel.getBenchmarkReturnText());
  }

  @Test
  void refreshAfterTradeAndAdvanceUpdatesHoldingsAndMetrics() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00");
    Exchange exchange = new Exchange("NYSE", List.of(apple));
    Player player = new Player("k", new BigDecimal("5000.00"));
    PlayerPortfolioPanel panel =
        runOnFxThread(() -> new PlayerPortfolioPanel(exchange, player, Path.of("/no/avatar.png")));

    exchange.buy("AAPL", BigDecimal.ONE, player);
    exchange.advance();
    runOnFxThread(
        () -> {
          panel.refresh();
          return panel;
        });

    assertEquals(1, panel.getHoldingCount());
    assertTrue(panel.getPortfolioReturnText().endsWith("%"));
    assertTrue(panel.getBenchmarkReturnText().endsWith("%"));
  }

  /**
   * Creates a stock with ordered price history.
   *
   * @param symbol stock symbol
   * @param company company name
   * @param prices ordered prices, oldest to newest
   * @return stock populated with those prices
   */
  private static Stock stockWithPrices(String symbol, String company, String... prices) {
    Stock stock = new Stock(symbol, company);
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }

  /**
   * Runs work on the JavaFX thread and returns the result.
   *
   * @param supplier work to run on the FX thread
   * @return supplier result
   * @throws Exception any exception thrown by the supplier
   */
  private static PlayerPortfolioPanel runOnFxThread(PanelSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<PlayerPortfolioPanel> ref = new AtomicReference<>();
    AtomicReference<Exception> err = new AtomicReference<>();
    Platform.runLater(
        () -> {
          try {
            ref.set(supplier.get());
          } catch (Exception e) {
            err.set(e);
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
  private interface PanelSupplier {
    PlayerPortfolioPanel get() throws Exception;
  }
}
