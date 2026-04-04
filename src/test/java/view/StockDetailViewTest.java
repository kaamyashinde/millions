package view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import model.Stock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import recommendation.StockRecommendation;

/**
 * Tests refresh behavior of the stock detail view.
 */
class StockDetailViewTest {

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
  void refreshRecomputesRecommendationFromUpdatedHistory() throws Exception {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("100.00"));
    stock.addNewSalesPrice(new BigDecimal("102.00"));

    StockDetailView view =
        runOnFxThread(
            () -> {
              StockDetailView detailView = new StockDetailView();
              detailView.showStock(stock, 1);
              return detailView;
            });

    assertEquals(StockRecommendation.BUY, view.getDisplayedRecommendation());
    assertEquals("Latest price: 102.00", view.getLatestPriceText());

    stock.addNewSalesPrice(new BigDecimal("95.00"));
    runOnFxThread(
        () -> {
          view.refresh(2);
          return view;
        });

    assertEquals(StockRecommendation.SELL, view.getDisplayedRecommendation());
    assertEquals("Latest price: 95.00", view.getLatestPriceText());
  }

  /**
   * Runs work on the JavaFX thread and returns the result.
   *
   * @param supplier work to run on the FX thread
   * @return supplier result
   * @throws Exception any exception thrown by the supplier
   */
  private static StockDetailView runOnFxThread(ViewSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<StockDetailView> ref = new AtomicReference<>();
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
  private interface ViewSupplier {
    StockDetailView get() throws Exception;
  }
}
