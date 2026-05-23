package view.components.chart;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.StackPane;
import model.core.asset.Stock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests price-axis behaviour for stock charts.
 */
class StockChartTest {

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
  void yAxisAutoRangesToPriceHistory() throws Exception {
    AxisBounds lowPriceBounds =
        runOnFxThread(() -> axisBoundsFor(stockWithPrices("LOW", "Low Range", "10.00", "12.00")));
    AxisBounds highPriceBounds =
        runOnFxThread(
            () -> axisBoundsFor(stockWithPrices("HIGH", "High Range", "900.00", "1200.00")));

    assertTrue(lowPriceBounds.lowerBound() <= 10.00);
    assertTrue(lowPriceBounds.upperBound() >= 12.00);
    assertTrue(highPriceBounds.lowerBound() <= 900.00);
    assertTrue(highPriceBounds.upperBound() >= 1200.00);
    assertTrue(highPriceBounds.upperBound() > lowPriceBounds.upperBound());
  }

  private static AxisBounds axisBoundsFor(Stock stock) {
    StockChart chart = new StockChart(stock);
    layout(chart);

    NumberAxis yAxis = (NumberAxis) chart.getYAxis();
    return new AxisBounds(yAxis.getLowerBound(), yAxis.getUpperBound());
  }

  private static void layout(StockChart chart) {
    StackPane root = new StackPane(chart);
    new Scene(root, 800, 500);
    root.applyCss();
    root.layout();
  }

  private static Stock stockWithPrices(String symbol, String company, String... prices) {
    Stock stock = new Stock(symbol, company);
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }

  private static <T> T runOnFxThread(FxSupplier<T> supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<T> ref = new AtomicReference<>();
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

  private record AxisBounds(double lowerBound, double upperBound) {}

  @FunctionalInterface
  private interface FxSupplier<T> {
    T get() throws Exception;
  }
}
