package view.components.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  private static final double AXIS_DELTA = 0.000001;

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
  void yAxisUsesTenPercentPaddingAroundVisiblePriceHistory() throws Exception {
    AxisBounds lowPriceBounds =
        runOnFxThread(() -> axisBoundsFor(stockWithPrices("LOW", "Low Range", "10.00", "12.00")));
    AxisBounds highPriceBounds =
        runOnFxThread(
            () -> axisBoundsFor(stockWithPrices("HIGH", "High Range", "900.00", "1200.00")));

    assertEquals(9.80, lowPriceBounds.lowerBound(), AXIS_DELTA);
    assertEquals(12.20, lowPriceBounds.upperBound(), AXIS_DELTA);
    assertEquals(870.00, highPriceBounds.lowerBound(), AXIS_DELTA);
    assertEquals(1230.00, highPriceBounds.upperBound(), AXIS_DELTA);
  }

  @Test
  void yAxisUsesOnlySelectedChartRange() throws Exception {
    AxisBounds bounds =
        runOnFxThread(
            () ->
                axisBoundsFor(
                    stockWithPrices(
                        "RNG", "Range Filter", "10", "20", "30", "40", "50", "60"),
                    ChartRange.FIVE_DAYS));

    assertEquals(16.00, bounds.lowerBound(), AXIS_DELTA);
    assertEquals(64.00, bounds.upperBound(), AXIS_DELTA);
  }

  @Test
  void yAxisUsesTenPercentOfPriceForFlatVisibleData() throws Exception {
    AxisBounds bounds =
        runOnFxThread(
            () ->
                axisBoundsFor(
                    stockWithPrices("FLT", "Flat Range", "100", "100", "100"),
                    ChartRange.ONE_DAY));

    assertEquals(90.00, bounds.lowerBound(), AXIS_DELTA);
    assertEquals(110.00, bounds.upperBound(), AXIS_DELTA);
  }

  @Test
  void yAxisLowerBoundCanBeNegativeAfterPadding() throws Exception {
    AxisBounds bounds =
        runOnFxThread(
            () -> axisBoundsFor(stockWithPrices("LOW", "Low Price", "0.01", "0.20")));

    assertEquals(-0.009, bounds.lowerBound(), AXIS_DELTA);
    assertEquals(0.219, bounds.upperBound(), AXIS_DELTA);
  }

  @Test
  void allRangeRendersFullPriceHistory() throws Exception {
    StockChart chart =
        runOnFxThread(
            () -> new StockChart(stockWithPrices("FULL", "Full Range", "10", "11", "12")));

    assertEquals(3, chart.getData().getFirst().getData().size());
    assertEquals(1, chart.getData().getFirst().getData().getFirst().getXValue());
    assertEquals(3, chart.getData().getFirst().getData().getLast().getXValue());
  }

  @Test
  void limitedRangeRendersLatestPriceHistoryWithOriginalDayNumbers() throws Exception {
    StockChart chart =
        runOnFxThread(
            () ->
                new StockChart(
                    stockWithPrices("LIM", "Limited Range", "10", "11", "12", "13", "14", "15"),
                    ChartRange.FIVE_DAYS));

    assertEquals(5, chart.getData().getFirst().getData().size());
    assertEquals(2, chart.getData().getFirst().getData().getFirst().getXValue());
    assertEquals(6, chart.getData().getFirst().getData().getLast().getXValue());
  }

  @Test
  void rangeLargerThanHistoryRendersAllAvailablePrices() throws Exception {
    StockChart chart =
        runOnFxThread(
            () ->
                new StockChart(
                    stockWithPrices("SHORT", "Short History", "10", "11"), ChartRange.FIVE_DAYS));

    assertEquals(2, chart.getData().getFirst().getData().size());
    assertEquals(1, chart.getData().getFirst().getData().getFirst().getXValue());
    assertEquals(2, chart.getData().getFirst().getData().getLast().getXValue());
  }

  @Test
  void oneDayRangeShowsSinglePointSymbol() throws Exception {
    StockChart chart =
        runOnFxThread(
            () ->
                new StockChart(
                    stockWithPrices("ONE", "One Day", "10", "11", "12"), ChartRange.ONE_DAY));

    assertEquals(1, chart.getData().getFirst().getData().size());
    assertEquals(3, chart.getData().getFirst().getData().getFirst().getXValue());
    assertTrue(chart.getCreateSymbols());
  }

  private static AxisBounds axisBoundsFor(Stock stock) {
    return axisBoundsFor(stock, ChartRange.ALL);
  }

  private static AxisBounds axisBoundsFor(Stock stock, ChartRange range) {
    StockChart chart = new StockChart(stock, range);
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
