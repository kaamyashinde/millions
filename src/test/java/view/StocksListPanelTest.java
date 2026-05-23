package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.StockDetailController;
import controller.StocksController;
import controller.TradingController;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.asset.Stock;
import view.components.chart.ChartRange;
import view.components.chart.StockChart;
import view.components.notification.NotificationService;
import view.pages.stocks.StocksPage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests the stock list to detail-view wiring.
 */
class StocksPageTest {

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
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "900.00", "1200.00");
    Exchange exchange =
        new Exchange.Builder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = (SplitPane) panel.getCenter();
    BorderPane chartPanel = (BorderPane) splitPane.getItems().getFirst();
    ScrollPane rightScroll = (ScrollPane) splitPane.getItems().get(1);
    VBox rightColumn = (VBox) rightScroll.getContent();
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) rightColumn.getChildren().getFirst();

    assertEquals(StockChart.class, chartPanel.getCenter().getClass());
    layout(panel);
    double appleUpperBound = yAxisUpperBound((StockChart) chartPanel.getCenter());
    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("AAPL", panel.getDetailView().getSelectedStock().getSymbol());

    runOnFxThread(
        () -> {
          table.getSelectionModel().select(1);
          return panel;
        });

    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());
    assertEquals(StockChart.class, chartPanel.getCenter().getClass());
    layout(panel);
    double microsoftUpperBound = yAxisUpperBound((StockChart) chartPanel.getCenter());
    assertTrue(microsoftUpperBound >= 1200.00);
    assertTrue(microsoftUpperBound > appleUpperBound);
  }

  @Test
  void chartRangeBarFiltersChartAndPersistsWhenSwitchingStocks() throws Exception {
    Stock apple =
        stockWithPrices("AAPL", "Apple Inc.", "100", "101", "102", "103", "104", "105", "106");
    Stock microsoft =
        stockWithPrices("MSFT", "Microsoft", "900", "901", "902", "903", "904", "905", "906");
    Exchange exchange =
        new Exchange.Builder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = (SplitPane) panel.getCenter();
    BorderPane chartPanel = (BorderPane) splitPane.getItems().getFirst();
    ScrollPane rightScroll = (ScrollPane) splitPane.getItems().get(1);
    VBox rightColumn = (VBox) rightScroll.getContent();
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) rightColumn.getChildren().getFirst();

    assertTrue(chartPanel.getBottom() instanceof HBox);
    assertEquals(7, dataPointCount((StockChart) chartPanel.getCenter()));

    runOnFxThread(
        () -> {
          rangeButton(chartPanel, ChartRange.FIVE_DAYS).fire();
          return panel;
        });

    assertEquals(5, dataPointCount((StockChart) chartPanel.getCenter()));
    assertEquals(3, firstChartDay((StockChart) chartPanel.getCenter()));
    assertTrue(rangeButton(chartPanel, ChartRange.FIVE_DAYS).isSelected());

    runOnFxThread(
        () -> {
          table.getSelectionModel().select(1);
          return panel;
        });

    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());
    assertEquals(5, dataPointCount((StockChart) chartPanel.getCenter()));
    assertEquals(3, firstChartDay((StockChart) chartPanel.getCenter()));
    assertTrue(rangeButton(chartPanel, ChartRange.FIVE_DAYS).isSelected());
  }

  private static StocksPage createPage(Exchange exchange, Player player) {
    StocksController stocks = new StocksController(exchange);
    StockDetailController stockDetail = new StockDetailController(exchange);
    TradingController trading =
        new TradingController(exchange, player, new NotificationService());
    return new StocksPage(stocks, stockDetail, trading, () -> {});
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

  private static double yAxisUpperBound(StockChart chart) {
    return ((NumberAxis) chart.getYAxis()).getUpperBound();
  }

  private static int dataPointCount(StockChart chart) {
    return chart.getData().getFirst().getData().size();
  }

  private static int firstChartDay(StockChart chart) {
    return chart.getData().getFirst().getData().getFirst().getXValue().intValue();
  }

  private static ToggleButton rangeButton(BorderPane chartPanel, ChartRange range) {
    HBox rangeBar = (HBox) chartPanel.getBottom();
    return rangeBar.getChildren().stream()
        .filter(ToggleButton.class::isInstance)
        .map(ToggleButton.class::cast)
        .filter(button -> button.getText().equals(range.getLabel()))
        .findFirst()
        .orElseThrow();
  }

  private static void layout(StocksPage panel) {
    if (panel.getScene() == null) {
      new Scene(panel, 1000, 700);
    }
    panel.applyCss();
    panel.layout();
  }

  /**
   * Runs work on the JavaFX thread and returns the result.
   *
   * @param supplier work to run on the FX thread
   * @return supplier result
   * @throws Exception any exception thrown by the supplier
   */
  private static StocksPage runOnFxThread(PanelSupplier supplier) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<StocksPage> ref = new AtomicReference<>();
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
    StocksPage get() throws Exception;
  }
}
