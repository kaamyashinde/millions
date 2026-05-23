package view.pages.stocks;

import static util.Validator.checkNotNull;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import controller.StockDetailController;
import controller.StocksController;
import controller.TradingController;
import model.core.asset.Stock;
import model.core.asset.info.StockFinancialInfo;
import view.components.chart.ChartRange;
import view.components.chart.StockChart;
import view.theme.ThemeStyles;

/**
 * JavaFX panel listing stocks with a detail pane and buy actions.
 */
public class StocksPage extends BorderPane {

  private final StocksController stocks;
  private final StockDetailController stockDetail;
  private final Runnable onTradeComplete;

  private final Label metaLabel = new Label();
  private final BorderPane chartPanel = new BorderPane();
  private final Label chartPlaceholder = new Label("Select a stock to view its price chart.");
  private final TableView<Stock> table = new TableView<>();
  private final StockDetailView detailView = new StockDetailView();
  private ChartRange selectedChartRange = ChartRange.ALL;

  /**
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

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Available Stocks");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    metaLabel.setWrapText(true);
    ThemeStyles.addStyleClasses(metaLabel, "finance-meta");
    VBox.setMargin(metaLabel, new Insets(0, 0, 8, 0));

    HBox topRow = new HBox(16, heading);
    topRow.setAlignment(Pos.CENTER_LEFT);

    VBox top = new VBox(4, topRow, metaLabel);
    setTop(top);

    buildTable();
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

    chartPlaceholder.setWrapText(true);
    ThemeStyles.addStyleClasses(chartPlaceholder, "empty-state");
    chartPanel.setPadding(new Insets(16));
    chartPanel.setMinWidth(360);
    ThemeStyles.addStyleClasses(chartPanel, "finance-panel");

    VBox rightColumn = new VBox(12, table, detailView);
    rightColumn.setFillWidth(true);
    ScrollPane rightScroll = new ScrollPane(rightColumn);
    rightScroll.setFitToWidth(true);
    rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    SplitPane splitPane = new SplitPane(chartPanel, rightScroll);
    splitPane.setDividerPositions(0.58);
    setCenter(splitPane);

    refresh();
  }

  private void buildTable() {
    TableColumn<Stock, String> colSym = new TableColumn<>("Symbol");
    colSym.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSymbol()));

    TableColumn<Stock, String> colCompany = new TableColumn<>("Company");
    colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompany()));

    TableColumn<Stock, String> colPrice = new TableColumn<>("Latest Price");
    colPrice.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getSalesPrice().toPlainString()));

    TableColumn<Stock, String> colRevenue = new TableColumn<>("Revenue (Mock)");
    colRevenue.setCellValueFactory(
        c -> {
          StockFinancialInfo fin = stockDetail.financialInfo(c.getValue());
          return new SimpleStringProperty(stockDetail.formatMoney(fin.revenue()));
        });

    TableColumn<Stock, String> colHealth = new TableColumn<>("Health");
    colHealth.setCellValueFactory(
        c -> new SimpleStringProperty(stockDetail.financialInfo(c.getValue()).health().displayLabel()));

    table.getColumns().setAll(colSym, colCompany, colPrice, colRevenue, colHealth);
  }

  /**
   * Reloads list metadata and the detail pane for the current selection.
   */
  public void refresh() {
    metaLabel.setText(stocks.getMetaText());
    stocks.refresh();
    Stock selected = stocks.getSelectedStock();
    if (selected != null) {
      table.getSelectionModel().select(selected);
    }
    table.refresh();
    updateDetail(stocks.getSelectedStock());
  }

  /**
   * @return embedded stock detail view
   */
  public StockDetailView getDetailView() {
    return detailView;
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
    if (selected == null) {
      chartPlaceholder.setText("Select a stock to view its price chart.");
      chartPanel.setCenter(chartPlaceholder);
      chartPanel.setBottom(null);
      return;
    }
    if (selected.getHistoricalPrices().isEmpty()) {
      chartPlaceholder.setText("No price history is available for " + selected.getSymbol() + " yet.");
      chartPanel.setCenter(chartPlaceholder);
      chartPanel.setBottom(null);
      return;
    }

    StockChart chart = new StockChart(selected, selectedChartRange);
    chart.setMinHeight(360);
    chart.setPrefHeight(520);
    chartPanel.setCenter(chart);
    chartPanel.setBottom(buildChartRangeBar(selected));
  }

  private HBox buildChartRangeBar(Stock selected) {
    HBox rangeBar = new HBox(4);
    ToggleGroup chartRangeGroup = new ToggleGroup();
    ThemeStyles.addStyleClasses(rangeBar, "chart-range-bar");

    for (ChartRange range : ChartRange.values()) {
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
    }

    return rangeBar;
  }
}
