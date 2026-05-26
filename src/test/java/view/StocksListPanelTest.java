package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.MarketMover;
import controller.StockDetailController;
import controller.StocksController;
import controller.TradingController;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import model.core.market.Exchange;
import model.core.market.ExchangeBuilder;
import model.core.player.Player;
import model.core.asset.Stock;
import view.components.chart.ChartRange;
import view.components.chart.ChartToolSelection;
import view.components.chart.StockChart;
import view.components.chart.tool.ChartTool;
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
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) stockTable(panel);

    assertEquals(StockChart.class, chartFromCard(chartCard).getClass());
    layout(panel);
    double appleUpperBound = yAxisUpperBound(chartFromCard(chartCard));
    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("AAPL", panel.getDetailView().getSelectedStock().getSymbol());

    runOnFxThread(
        () -> {
          table.getSelectionModel().select(1);
          return panel;
        });

    assertNotNull(panel.getDetailView().getSelectedStock());
    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());
    assertEquals(StockChart.class, chartFromCard(chartCard).getClass());
    layout(panel);
    double microsoftUpperBound = yAxisUpperBound(chartFromCard(chartCard));
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
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) stockTable(panel);

    assertTrue(chartControls(chartCard) instanceof VBox);
    assertEquals(7, dataPointCount(chartFromCard(chartCard)));

    runOnFxThread(
        () -> {
          rangeButton(chartCard, ChartRange.FIVE_DAYS).fire();
          return panel;
        });

    assertEquals(5, dataPointCount(chartFromCard(chartCard)));
    assertEquals(3, firstChartDay(chartFromCard(chartCard)));
    assertTrue(rangeButton(chartCard, ChartRange.FIVE_DAYS).isSelected());

    runOnFxThread(
        () -> {
          table.getSelectionModel().select(1);
          return panel;
        });

    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());
    assertEquals(5, dataPointCount(chartFromCard(chartCard)));
    assertEquals(3, firstChartDay(chartFromCard(chartCard)));
    assertTrue(rangeButton(chartCard, ChartRange.FIVE_DAYS).isSelected());
  }

  @Test
  void chartAnalysisSelectorStartsWithNoneAndRegistersTools() throws Exception {
    Stock apple = stockWithPriceRange("AAPL", "Apple Inc.", 100, 40);
    Exchange exchange = new ExchangeBuilder("NYSE").stocks(List.of(apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);
    StockChart chart = chartFromCard(chartCard);

    assertEquals(3, chart.getTools().size());
    assertTrue(analysisButton(chartCard, ChartToolSelection.NONE).isSelected());
    assertEquals(1, chart.getData().size());
    assertTrue(chart.getTools().stream().noneMatch(tool -> tool.activeProperty().get()));
  }

  @Test
  void chartAnalysisSelectorActivatesOneToolAndNoneClearsOverlays() throws Exception {
    Stock apple = stockWithPriceRange("AAPL", "Apple Inc.", 100, 40);
    Exchange exchange = new ExchangeBuilder("NYSE").stocks(List.of(apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);
    StockChart chart = chartFromCard(chartCard);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.FIBONACCI).fire();
          return panel;
        });

    assertTrue(chartTool(chart, ChartToolSelection.FIBONACCI).activeProperty().get());
    assertFalse(chartTool(chart, ChartToolSelection.ELLIOTT_WAVE).activeProperty().get());
    assertTrue(chart.getData().size() > 1);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.ELLIOTT_WAVE).fire();
          return panel;
        });

    assertFalse(chartTool(chart, ChartToolSelection.FIBONACCI).activeProperty().get());
    assertTrue(chartTool(chart, ChartToolSelection.ELLIOTT_WAVE).activeProperty().get());
    assertTrue(chart.getData().size() > 1);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.MOON_PHASES).fire();
          return panel;
        });

    assertTrue(chartTool(chart, ChartToolSelection.MOON_PHASES).activeProperty().get());
    assertTrue(chart.getData().size() > 1);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.NONE).fire();
          return panel;
        });

    assertEquals(1, chart.getData().size());
    assertTrue(chart.getTools().stream().noneMatch(tool -> tool.activeProperty().get()));
  }

  @Test
  void moonPhaseAnalysisShowsMarkerOnSinglePointDemoChart() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "152.54");
    Exchange exchange = new ExchangeBuilder("NYSE").stocks(List.of(apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);
    StockChart chart = chartFromCard(chartCard);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.MOON_PHASES).fire();
          return panel;
        });

    assertTrue(chartTool(chart, ChartToolSelection.MOON_PHASES).activeProperty().get());
    assertTrue(chart.getData().size() > 1);
  }

  @Test
  void selectedAnalysisToolPersistsWhenChartRangeChanges() throws Exception {
    Stock apple = stockWithPriceRange("AAPL", "Apple Inc.", 100, 40);
    Exchange exchange = new ExchangeBuilder("NYSE").stocks(List.of(apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    SplitPane splitPane = mainSplitPane(panel);
    VBox chartCard = chartCard(splitPane);

    runOnFxThread(
        () -> {
          analysisButton(chartCard, ChartToolSelection.MOON_PHASES).fire();
          rangeButton(chartCard, ChartRange.FIVE_DAYS).fire();
          return panel;
        });

    StockChart rebuiltChart = chartFromCard(chartCard);
    assertTrue(analysisButton(chartCard, ChartToolSelection.MOON_PHASES).isSelected());
    assertTrue(chartTool(rebuiltChart, ChartToolSelection.MOON_PHASES).activeProperty().get());
    assertEquals(36, firstChartDay(rebuiltChart));
  }

  @Test
  void searchFieldFiltersBySymbolAndCompanyName() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "102.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "900.00", "1200.00");
    Exchange exchange =
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    layout(panel);
    @SuppressWarnings("unchecked")
    TableView<Stock> table = (TableView<Stock>) stockTable(panel);
    TextField search = (TextField) panel.lookup("#stocks-search-field");

    runOnFxThread(
        () -> {
          search.setText("micro");
          return panel;
        });

    assertEquals(1, table.getItems().size());
    assertEquals("MSFT", table.getItems().getFirst().getSymbol());
    assertEquals("MSFT", panel.getDetailView().getSelectedStock().getSymbol());

    runOnFxThread(
        () -> {
          search.setText("zz");
          return panel;
        });

    assertEquals(0, table.getItems().size());
    assertEquals(null, panel.getDetailView().getSelectedStock());

    runOnFxThread(
        () -> {
          search.clear();
          return panel;
        });

    assertEquals(2, table.getItems().size());
    assertEquals("AAPL", panel.getDetailView().getSelectedStock().getSymbol());
  }

  @Test
  void marketMoversPanelDisplaysWinnerAndLoserRows() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "110.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "100.00", "90.00");
    Stock alphabet = stockWithPrices("GOOGL", "Alphabet Inc.", "100.00", "100.00");
    Exchange exchange =
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple, alphabet)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    layout(panel);
    ScrollPane rootScroll = (ScrollPane) panel.getCenter();
    VBox pageContent = (VBox) rootScroll.getContent();
    SplitPane splitPane = mainSplitPane(panel);

    assertEquals("stocks-root-scroll", rootScroll.getId());
    assertEquals("market-movers-panel", pageContent.getChildren().get(1).getId());
    assertEquals(splitPane, pageContent.getChildren().get(2));
    assertTrue(splitPane.getItems().getFirst() instanceof VBox);
    assertTrue(splitPane.getItems().get(1) instanceof VBox);
    assertTrue(nodeById(pageContent.getChildren().get(1), "market-winners-table").isPresent());
    assertTrue(nodeById(pageContent.getChildren().get(1), "market-losers-table").isPresent());
    assertEquals(List.of("AAPL"),
        panel.getDisplayedWinners().stream().map(MarketMover::symbol).toList());
    assertEquals(List.of("MSFT"),
        panel.getDisplayedLosers().stream().map(MarketMover::symbol).toList());
    assertTrue(visibleText(panel).contains("+10.00%"));
    assertTrue(visibleText(panel).contains("-10.00%"));
  }

  @Test
  void marketMoversPanelShowsEmptyStateWhenNoMoversExist() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "100.00", "100.00");
    Exchange exchange =
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));
    layout(panel);

    assertTrue(panel.getDisplayedWinners().isEmpty());
    assertTrue(panel.getDisplayedLosers().isEmpty());
    assertEquals(
        "No winners yet.",
        ((Label) marketTable(panel, "#market-winners-table").getPlaceholder()).getText());
    assertEquals(
        "No losers yet.",
        ((Label) marketTable(panel, "#market-losers-table").getPlaceholder()).getText());
  }

  @Test
  void marketMoversPanelRefreshesWhenPricesChange() throws Exception {
    Stock apple = stockWithPrices("AAPL", "Apple Inc.", "100.00", "102.00");
    Stock microsoft = stockWithPrices("MSFT", "Microsoft", "100.00", "90.00");
    Exchange exchange =
        new ExchangeBuilder("NYSE").stocks(List.of(microsoft, apple)).build();
    Player player = new Player("tester", new BigDecimal("10000.00"));

    StocksPage panel = runOnFxThread(() -> createPage(exchange, player));

    runOnFxThread(
        () -> {
          apple.addNewSalesPrice(new BigDecimal("101.00"));
          microsoft.addNewSalesPrice(new BigDecimal("95.00"));
          panel.refresh();
          return panel;
        });

    assertEquals(List.of("MSFT"),
        panel.getDisplayedWinners().stream().map(MarketMover::symbol).toList());
    assertEquals(List.of("AAPL"),
        panel.getDisplayedLosers().stream().map(MarketMover::symbol).toList());
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

  private static Stock stockWithPriceRange(
      String symbol, String company, int firstPrice, int count) {
    Stock stock = new Stock(symbol, company);
    for (int i = 0; i < count; i++) {
      stock.addNewSalesPrice(BigDecimal.valueOf(firstPrice + i));
    }
    return stock;
  }

  private static VBox chartCard(SplitPane splitPane) {
    Node first = splitPane.getItems().getFirst();
    if (first instanceof VBox slot && slot.getChildren().size() == 1) {
      Node child = slot.getChildren().getFirst();
      if (child instanceof VBox card) {
        return card;
      }
    }
    throw new IllegalStateException("Expected chart card in split pane, got " + first);
  }

  private static StockChart chartFromCard(VBox chartCard) {
    for (Node child : chartCard.getChildren()) {
      if (child instanceof StockChart stockChart) {
        return stockChart;
      }
      if (child instanceof VBox wrap) {
        for (Node nested : wrap.getChildren()) {
          if (nested instanceof StockChart stockChart) {
            return stockChart;
          }
        }
      }
    }
    throw new IllegalStateException("Expected StockChart in chart card, got " + chartCard.getChildren());
  }

  private static VBox chartControls(VBox chartCard) {
    if (chartCard.getChildren().isEmpty()) {
      throw new IllegalStateException("Chart card has no children");
    }
    Node last = chartCard.getChildren().getLast();
    if (last instanceof VBox controls) {
      return controls;
    }
    throw new IllegalStateException("Expected chart controls VBox, got " + last);
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

  private static ToggleButton rangeButton(VBox chartCard, ChartRange range) {
    return toggleButton(chartControls(chartCard), range.getLabel()).orElseThrow();
  }

  private static TableView<?> stockTable(StocksPage panel) {
    SplitPane splitPane = mainSplitPane(panel);
    VBox rightColumn = (VBox) splitPane.getItems().get(1);
    return (TableView<?>) rightColumn.getChildren().stream()
        .filter(child -> "stocks-table".equals(child.getId()))
        .findFirst()
        .orElseThrow();
  }

  private static TableView<?> marketTable(StocksPage panel, String selector) {
    return (TableView<?>) nodeById(panel, selector.substring(1)).orElseThrow();
  }

  private static SplitPane mainSplitPane(StocksPage panel) {
    ScrollPane rootScroll = (ScrollPane) panel.getCenter();
    VBox pageContent = (VBox) rootScroll.getContent();
    return (SplitPane) pageContent.getChildren().get(2);
  }

  private static Optional<Node> nodeById(Node root, String id) {
    if (id.equals(root.getId())) {
      return Optional.of(root);
    }
    if (root instanceof Parent parent) {
      return parent.getChildrenUnmodifiable().stream()
          .map(child -> nodeById(child, id))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst();
    }
    return Optional.empty();
  }

  private static List<String> visibleText(Node root) {
    if (root instanceof Labeled labeled && labeled.getText() != null) {
      return List.of(labeled.getText());
    }
    if (root instanceof Parent parent) {
      return parent.getChildrenUnmodifiable().stream()
          .flatMap(child -> visibleText(child).stream())
          .toList();
    }
    return List.of();
  }

  private static ToggleButton analysisButton(VBox chartCard, ChartToolSelection selection) {
    return toggleButton(chartControls(chartCard), selection.getLabel()).orElseThrow();
  }

  private static Optional<ToggleButton> toggleButton(Node root, String text) {
    if (root instanceof ToggleButton button && button.getText().equals(text)) {
      return Optional.of(button);
    }
    if (root instanceof Parent parent) {
      return parent.getChildrenUnmodifiable().stream()
          .map(child -> toggleButton(child, text))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst();
    }
    return Optional.empty();
  }

  private static ChartTool chartTool(StockChart chart, ChartToolSelection selection) {
    return chart.getTools().stream()
        .filter(tool -> tool.getName().equals(selection.getLabel()))
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
