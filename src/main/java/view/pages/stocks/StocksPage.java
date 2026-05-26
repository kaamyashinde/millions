package view.pages.stocks;

import static util.Validator.checkNotNull;

import controller.MarketMover;
import controller.StockDetailController;
import controller.StocksController;
import controller.TradingController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.core.asset.Stock;
import model.core.asset.info.StockFinancialInfo;
import view.components.chart.AnalysisToolbar;
import view.components.chart.ChartRange;
import view.components.chart.ChartToolSelection;
import view.components.chart.StockChart;
import view.components.chart.tool.ElliottWaveTool;
import view.components.chart.tool.FibonacciTool;
import view.components.chart.tool.MoonPhaseTool;
import view.components.table.AppTableView;
import view.theme.ThemeStyles;
import view.util.UiFormat;

/**
 * JavaFX panel listing stocks with a detail pane and buy actions.
 */
public class StocksPage extends BorderPane {

  private static final LocalDate SIMULATION_START_DATE = LocalDate.of(2024, 1, 11);
  private static final int MARKET_MOVER_LIMIT = 3;
  private static final double CHART_MIN_HEIGHT = 200;
  private static final double CHART_PREF_HEIGHT = 280;
  private static final double CHART_MAX_HEIGHT = 320;

  private final StocksController stocks;
  private final StockDetailController stockDetail;
  private final Runnable onTradeComplete;

  private final Label metaLabel = new Label();
  private final TextField searchField = new TextField();
  private final VBox chartCard = new VBox(8);
  private final VBox chartSlot = new VBox(chartCard);
  private final Label chartPlaceholder = new Label("Select a stock to view its price chart.");
  private final TableView<Stock> table = new TableView<>();
  private final AppTableView<MarketMover> winnersTable =
      new AppTableView<>("No winners yet.");
  private final AppTableView<MarketMover> losersTable =
      new AppTableView<>("No losers yet.");
  private final StockDetailView detailView = new StockDetailView();
  private ChartRange selectedChartRange = ChartRange.ALL;
  private ChartToolSelection selectedChartTool = ChartToolSelection.NONE;
  private StockChart currentChart;

  /**
   * Creates the stocks page.
   *
   * @param stocks stocks list and selection state
   * @param stockDetail fundamentals and market events for the detail pane
   * @param trading trading operations for buy dialog
   * @param onTradeComplete invoked after a successful trade
   */
  public StocksPage(
      StocksController stocks,
      StockDetailController stockDetail,
      TradingController trading,
      Runnable onTradeComplete) {
    checkNotNull(stocks, "stocks");
    checkNotNull(stockDetail, "stockDetail");
    checkNotNull(trading, "trading");
    checkNotNull(onTradeComplete, "onTradeComplete");
    this.stocks = stocks;
    this.stockDetail = stockDetail;
    this.onTradeComplete = onTradeComplete;

    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Available Stocks");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    metaLabel.setWrapText(true);
    ThemeStyles.addStyleClasses(metaLabel, "finance-meta");
    VBox.setMargin(metaLabel, new Insets(0, 0, 8, 0));
    searchField.setPromptText("Search by symbol or company");
    searchField.setId("stocks-search-field");
    ThemeStyles.styleField(searchField);
    searchField.textProperty().addListener((obs, previous, value) -> {
      stocks.setSearchTerm(value);
      syncTableSelection();
      updateDetail(stocks.getSelectedStock());
      updateMetaText();
    });

    HBox topRow = new HBox(16, heading);
    topRow.setAlignment(Pos.CENTER_LEFT);

    final VBox header = new VBox(8, topRow, searchField, metaLabel);

    buildTable();
    table.setId("stocks-table");
    table.setItems(stocks.getStocks());
    table.setPlaceholder(new Label("No stocks available."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setMinHeight(220);
    table.setPrefHeight(320);
    VBox.setVgrow(table, Priority.ALWAYS);

    table.getSelectionModel().selectedItemProperty().addListener((obs, previous, selected) -> {
      stocks.setSelectedStock(selected);
      updateDetail(selected);
    });

    detailView.setTradeHandlers(
        trading, () -> getScene() != null ? getScene().getWindow() : null, onTradeComplete);
    detailView.setOnEventClicked(
        event -> {
          if (currentChart != null) {
            currentChart.toggleEventMarker(event.day(), event.title());
          }
        });

    chartPlaceholder.setWrapText(true);
    ThemeStyles.addStyleClasses(chartPlaceholder, "empty-state");
    chartCard.setPadding(new Insets(16));
    chartCard.setMinWidth(360);
    ThemeStyles.addStyleClasses(chartCard, "finance-panel");
    chartSlot.setAlignment(Pos.TOP_LEFT);
    VBox.setVgrow(chartCard, Priority.NEVER);

    VBox rightColumn = new VBox(12, table, detailView);
    rightColumn.setFillWidth(true);

    SplitPane splitPane = new SplitPane(chartSlot, rightColumn);
    splitPane.setDividerPositions(0.58);
    VBox.setVgrow(splitPane, Priority.ALWAYS);

    VBox pageContent = new VBox(12, header, buildMarketMoversPanel(), splitPane);
    pageContent.setId("stocks-page-content");
    pageContent.setPadding(new Insets(16));
    pageContent.setFillWidth(true);

    ScrollPane rootScroll = new ScrollPane(pageContent);
    rootScroll.setId("stocks-root-scroll");
    rootScroll.setFitToWidth(true);
    rootScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    rootScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    setCenter(rootScroll);

    refresh();
  }

  private void buildTable() {
    TableColumn<Stock, String> colSym = new TableColumn<>("Symbol");
    colSym.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSymbol()));

    TableColumn<Stock, String> colCompany = new TableColumn<>("Company");
    colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompany()));

    TableColumn<Stock, String> colPrice = new TableColumn<>("Latest Price");
    colPrice.setCellValueFactory(
        c -> new SimpleStringProperty(UiFormat.decimal(c.getValue().getSalesPrice())));

    TableColumn<Stock, String> colRevenue = new TableColumn<>("Revenue (Mock)");
    colRevenue.setCellValueFactory(
        c -> {
          StockFinancialInfo fin = stockDetail.financialInfo(c.getValue());
          return new SimpleStringProperty(stockDetail.formatMoney(fin.revenue()));
        });

    TableColumn<Stock, String> colHealth = new TableColumn<>("Health");
    colHealth.setCellValueFactory(
        c ->
            new SimpleStringProperty(
                stockDetail.financialInfo(c.getValue()).health().displayLabel()));

    table.getColumns().setAll(colSym, colCompany, colPrice, colRevenue, colHealth);
  }

  /**
   * Reloads list metadata and the detail pane for the current selection.
   */
  public void refresh() {
    stocks.refresh();
    syncTableSelection();
    updateMetaText();
    table.refresh();
    updateMarketMovers();
    updateDetail(stocks.getSelectedStock());
  }

  /**
   * Returns the embedded stock detail view.
   *
   * @return embedded stock detail view
   */
  public StockDetailView getDetailView() {
    return detailView;
  }

  /**
   * Returns the current text in the search field.
   *
   * @return search field text
   */
  public String getSearchText() {
    return searchField.getText();
  }

  /**
   * Returns the winner rows currently shown in the market movers panel.
   *
   * @return immutable snapshot of winner rows
   */
  public List<MarketMover> getDisplayedWinners() {
    return List.copyOf(winnersTable.getItems());
  }

  /**
   * Returns the loser rows currently shown in the market movers panel.
   *
   * @return immutable snapshot of loser rows
   */
  public List<MarketMover> getDisplayedLosers() {
    return List.copyOf(losersTable.getItems());
  }

  private void syncTableSelection() {
    Stock selected = stocks.getSelectedStock();
    if (selected != null) {
      table.getSelectionModel().select(selected);
    } else {
      table.getSelectionModel().clearSelection();
    }
  }

  private void updateMetaText() {
    metaLabel.setText(stocks.getMetaText());
  }

  /**
   * Reloads winner and loser rows from the stock controller.
   */
  private void updateMarketMovers() {
    winnersTable.getItems().setAll(stocks.getTopWinners(MARKET_MOVER_LIMIT));
    losersTable.getItems().setAll(stocks.getTopLosers(MARKET_MOVER_LIMIT));
    winnersTable.refresh();
    losersTable.refresh();
  }

  /**
   * Builds the compact market movers summary shown above the main stocks split pane.
   *
   * @return market movers panel
   */
  private VBox buildMarketMoversPanel() {
    Label heading = new Label("Market movers");
    ThemeStyles.addStyleClasses(heading, "section-title");

    configureMoverTable(winnersTable, "market-winners-table");
    configureMoverTable(losersTable, "market-losers-table");

    VBox winnersBox = buildMoverGroup("Winners", winnersTable);
    VBox losersBox = buildMoverGroup("Losers", losersTable);
    HBox tables = new HBox(12, winnersBox, losersBox);
    tables.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(winnersBox, Priority.ALWAYS);
    HBox.setHgrow(losersBox, Priority.ALWAYS);

    VBox panel = new VBox(8, heading, tables);
    panel.setId("market-movers-panel");
    ThemeStyles.addStyleClasses(panel, "finance-panel", "market-movers-panel");
    return panel;
  }

  /**
   * Builds one labeled mover table group.
   *
   * @param title visible table group title
   * @param table configured market mover table
   * @return group containing the title and table
   */
  private static VBox buildMoverGroup(String title, AppTableView<MarketMover> table) {
    Label label = new Label(title);
    ThemeStyles.addStyleClasses(label, "finance-meta", "market-mover-heading");
    VBox group = new VBox(6, label, table);
    group.setFillWidth(true);
    VBox.setVgrow(table, Priority.ALWAYS);
    return group;
  }

  /**
   * Configures columns and sizing for a market mover table.
   *
   * @param table table to configure
   * @param id JavaFX node id used by tests and lookup
   */
  private static void configureMoverTable(AppTableView<MarketMover> table, String id) {
    table.setId(id);
    table.setPrefHeight(150);
    table.setMinHeight(130);
    table.setMaxHeight(170);
    table.setFocusTraversable(false);

    TableColumn<MarketMover, String> symbolColumn =
        AppTableView.createTextColumn("Symbol", MarketMover::symbol);
    TableColumn<MarketMover, String> companyColumn =
        AppTableView.createTextColumn("Company", MarketMover::company);
    TableColumn<MarketMover, BigDecimal> priceColumn =
        AppTableView.createNumericColumn(
            "Price", MarketMover::currentPrice, UiFormat::decimal);
    TableColumn<MarketMover, BigDecimal> changeColumn =
        AppTableView.createNumericColumn(
            "Change", MarketMover::absoluteChange, StocksPage::formatSignedDecimal);
    TableColumn<MarketMover, BigDecimal> percentColumn =
        AppTableView.createNumericColumn(
            "Change %", MarketMover::percentChange, StocksPage::formatSignedPercent);

    table.getColumns()
        .setAll(List.of(symbolColumn, companyColumn, priceColumn, changeColumn, percentColumn));
  }

  /**
   * Formats signed decimal changes with a leading plus sign for positive values.
   *
   * @param value decimal value to format
   * @return signed display text
   */
  private static String formatSignedDecimal(BigDecimal value) {
    String formatted = UiFormat.decimal(value);
    return value != null && value.signum() > 0 ? "+" + formatted : formatted;
  }

  /**
   * Formats signed percent changes with a leading plus sign for positive values.
   *
   * @param value fractional percent value to format
   * @return signed percentage display text
   */
  private static String formatSignedPercent(BigDecimal value) {
    String formatted = UiFormat.percent(value);
    return value != null && value.signum() > 0 ? "+" + formatted : formatted;
  }

  private void updateDetail(Stock selected) {
    int day = stocks.getExchange().getDay();
    updateChart(selected);
    detailView.showStock(
        selected,
        day,
        stockDetail.getLastMarketEvent(),
        stockDetail.getMarketHistory(selected),
        stockDetail);
  }

  /**
   * Refreshes the primary chart panel for the selected stock.
   *
   * @param selected selected stock, or {@code null} when no stock is available
   */
  private void updateChart(Stock selected) {
    currentChart = null;
    if (selected == null) {
      chartPlaceholder.setText("Select a stock to view its price chart.");
      chartCard.getChildren().setAll(chartPlaceholder);
      return;
    }
    if (selected.getHistoricalPrices().isEmpty()) {
      chartPlaceholder.setText(
          "No price history is available for " + selected.getSymbol() + " yet.");
      chartCard.getChildren().setAll(chartPlaceholder);
      return;
    }

    StockChart chart = new StockChart(selected, selectedChartRange);
    currentChart = chart;
    registerAnalysisTools(chart);
    chart.setMinHeight(CHART_MIN_HEIGHT);
    chart.setPrefHeight(CHART_PREF_HEIGHT);
    chart.setMaxHeight(CHART_MAX_HEIGHT);
    VBox chartWrap = new VBox(chart);
    chartWrap.setAlignment(Pos.TOP_CENTER);
    chartCard.getChildren().setAll(chartWrap, buildChartControls(selected, chart));
  }

  private void registerAnalysisTools(StockChart chart) {
    chart.registerTool(new FibonacciTool());
    chart.registerTool(new ElliottWaveTool());
    chart.registerTool(new MoonPhaseTool(SIMULATION_START_DATE));
  }

  private VBox buildChartControls(Stock selected, StockChart chart) {
    AnalysisToolbar analysisToolbar =
        new AnalysisToolbar(
            chart.getTools(),
            chart,
            selectedChartTool,
            selection -> selectedChartTool = selection);
    VBox controls = new VBox(6, analysisToolbar, buildChartRangeBar(selected));
    ThemeStyles.addStyleClasses(controls, "chart-control-stack");
    return controls;
  }

  private HBox buildChartRangeBar(Stock selected) {
    HBox rangeBar = new HBox(4);
    ToggleGroup chartRangeGroup = new ToggleGroup();
    ThemeStyles.addStyleClasses(rangeBar, "chart-range-bar");

    Arrays.stream(ChartRange.values()).forEach(range -> {
      ToggleButton button = new ToggleButton(range.getLabel());
      button.setToggleGroup(chartRangeGroup);
      button.setUserData(range);
      button.setFocusTraversable(false);
      ThemeStyles.addStyleClasses(button, "chart-range-button");
      button.setSelected(range == selectedChartRange);
      button.selectedProperty()
          .addListener(
              (obs, wasSelected, isSelected) -> {
                if (isSelected) {
                  selectedChartRange = range;
                  updateChart(selected);
                }
                if (!isSelected && chartRangeGroup.getSelectedToggle() == null) {
                  button.setSelected(true);
                }
              });
      rangeBar.getChildren().add(button);
    });

    return rangeBar;
  }
}
