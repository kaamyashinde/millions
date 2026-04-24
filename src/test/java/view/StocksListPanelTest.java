package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import model.Exchange;
import model.Stock;
import old_view.StocksListPanel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the stock list to detail-view wiring.
 */
class StocksListPanelTest {

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
  void selectingARowUpdatesTheEmbeddedDetailView() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "102.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "200.00", "194.00");
    Exchange exchange = new Exchange("NYSE", List.of(microsoft, apple));

    StocksListPanel panel = runOnFxThread(() -> new StocksListPanel(exchange));
    SplitPane splitPane = (SplitPane) panel.getCenter();
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) splitPane.getItems().getFirst();

    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("AAPL", panel.getDetailView().getSelectedStock().getSymbol());

    runOnFxThread(
        () -> {
          table.getSelectionModel().select(1);
          return panel;
        });

    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());
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
  private static StocksListPanel runOnFxThread(PanelSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<StocksListPanel> ref = new AtomicReference<>();
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
    StocksListPanel get() throws Exception;
  }
}
