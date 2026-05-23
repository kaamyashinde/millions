package view.pages.portfolio;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.Exchange;
import model.Player;
import model.Share;
import model.analysis.MetricValue;
import model.analysis.PerformanceComparison;
import model.analysis.PortfolioPerformanceService;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;
import view.theme.ThemeStyles;

/**
 * JavaFX panel showing player summary data, current holdings, and portfolio-vs-market metrics.
 */
public class PlayerPortfolioPage extends BorderPane {

  private final Exchange exchange;
  private final Player player;
  private final Path avatarPath;
  private final PortfolioPerformanceService performanceService = new PortfolioPerformanceService();

  private final Label playerLabel = new Label();
  private final Label balanceLabel = new Label();
  private final Label netWorthLabel = new Label();
  private final Label tradingDayLabel = new Label();

  private final Label portfolioReturnValueLabel = new Label();
  private final Label portfolioVolatilityValueLabel = new Label();
  private final Label portfolioSharpeValueLabel = new Label();
  private final Label benchmarkReturnValueLabel = new Label();
  private final Label benchmarkVolatilityValueLabel = new Label();
  private final Label benchmarkSharpeValueLabel = new Label();

  private final TableView<Share> holdingsTable = new TableView<>();
  private final ObservableList<Share> holdings = FXCollections.observableArrayList();
  private final ImageView avatarView = new ImageView();
  private final ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());

  /**
   * Builds a player summary panel backed by the given exchange and player.
   *
   * @param exchange exchange supplying market and trading-day state
   * @param player player whose summary and holdings should be shown
   * @param avatarPath path to profile avatar image (may not exist yet)
   */
  public PlayerPortfolioPage(Exchange exchange, Player player, Path avatarPath) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    checkNotNull(avatarPath, "avatarPath");
    this.exchange = exchange;
    this.player = player;
    this.avatarPath = avatarPath;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Player Overview");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    Button refreshButton = new Button("Refresh");
    refreshButton.setOnAction(_ -> refresh());
    styleButton(refreshButton);

    avatarView.setFitWidth(56);
    avatarView.setFitHeight(56);
    avatarView.setPreserveRatio(true);
    avatarView.setSmooth(true);

    HBox topRow = new HBox(16, avatarView, heading, refreshButton);
    topRow.setAlignment(Pos.CENTER_LEFT);

    GridPane summaryGrid = new GridPane();
    summaryGrid.setHgap(24);
    summaryGrid.setVgap(10);
    summaryGrid.addRow(0, createHeaderLabel("Player"), playerLabel, createHeaderLabel("Trading Day"), tradingDayLabel);
    summaryGrid.addRow(1, createHeaderLabel("Balance"), balanceLabel, createHeaderLabel("Net Worth"), netWorthLabel);

    VBox summaryCard = new VBox(12, topRow, summaryGrid);
    ThemeStyles.addStyleClasses(summaryCard, "card");
    setTop(summaryCard);
    BorderPane.setMargin(summaryCard, new Insets(0, 0, 16, 0));

    buildHoldingsTable();
    holdingsTable.setItems(holdings);
    holdingsTable.setPlaceholder(new Label("No holdings yet."));
    holdingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(holdingsTable, Priority.ALWAYS);

    VBox metricsBox = buildMetricsBox();
    setCenter(holdingsTable);
    setBottom(metricsBox);

    refresh();
  }

  private Label createHeaderLabel(String text) {
    Label label = new Label(text);
    ThemeStyles.addStyleClasses(label, "text-secondary");
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }

  private void buildHoldingsTable() {
    TableColumn<Share, String> symbolColumn = new TableColumn<>("Symbol");
    symbolColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsset().getSymbol()));

    TableColumn<Share, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsset().getDisplayName()));

    TableColumn<Share, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAsset().getAssetType()));

    TableColumn<Share, String> quantityColumn = new TableColumn<>("Quantity");
    quantityColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getQuantity().toPlainString()));

    TableColumn<Share, String> purchasePriceColumn = new TableColumn<>("Purchase Price");
    purchasePriceColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getPurchasePrice().toPlainString()));

    TableColumn<Share, String> currentPriceColumn = new TableColumn<>("Current Price");
    currentPriceColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getAsset().getSalesPrice().toPlainString()));

    holdingsTable.getColumns().setAll(
        List.of(
            symbolColumn,
            nameColumn,
            typeColumn,
            quantityColumn,
            purchasePriceColumn,
            currentPriceColumn));
  }

  private VBox buildMetricsBox() {
    Label heading = new Label("Risk-Adjusted Performance vs Market");
    heading.setFont(Font.font("System", FontWeight.BOLD, 16));

    GridPane metricsGrid = new GridPane();
    metricsGrid.setHgap(24);
    metricsGrid.setVgap(10);

    metricsGrid.addRow(0, createHeaderLabel("Metric"), createHeaderLabel("Your Portfolio"), createHeaderLabel("Market Benchmark"));
    metricsGrid.addRow(1, new Label("Return %"), portfolioReturnValueLabel, benchmarkReturnValueLabel);
    metricsGrid.addRow(
        2,
        new Label("Volatility"),
        portfolioVolatilityValueLabel,
        benchmarkVolatilityValueLabel);
    metricsGrid.addRow(3, new Label("Sharpe Ratio"), portfolioSharpeValueLabel, benchmarkSharpeValueLabel);

    VBox metricsBox = new VBox(12, heading, metricsGrid);
    metricsBox.setPadding(new Insets(14));
    ThemeStyles.addStyleClasses(metricsBox, "finance-summary-card");
    BorderPane.setMargin(metricsBox, new Insets(16, 0, 0, 0));
    return metricsBox;
  }

  /**
   * Refreshes the labels, holdings list, and side-by-side metrics from the live model state.
   */
  public void refresh() {
    loadAvatarThumbnail();
    playerLabel.setText(player.getName());
    tradingDayLabel.setText(Integer.toString(exchange.getDay()));
    balanceLabel.setText(player.getMoney().setScale(2, RoundingMode.HALF_UP).toPlainString());
    netWorthLabel.setText(player.getNetWorth().setScale(2, RoundingMode.HALF_UP).toPlainString());
    holdings.setAll(player.getPortfolio().getShares());

    PerformanceComparison comparison = performanceService.compareAgainstMarket(player, exchange);
    portfolioReturnValueLabel.setText(formatMetricValue(comparison.portfolio().returnPercent(), true));
    portfolioVolatilityValueLabel.setText(formatMetricValue(comparison.portfolio().volatility(), true));
    portfolioSharpeValueLabel.setText(formatMetricValue(comparison.portfolio().sharpeRatio(), false));
    benchmarkReturnValueLabel.setText(formatMetricValue(comparison.benchmark().returnPercent(), true));
    benchmarkVolatilityValueLabel.setText(formatMetricValue(comparison.benchmark().volatility(), true));
    benchmarkSharpeValueLabel.setText(formatMetricValue(comparison.benchmark().sharpeRatio(), false));
    holdingsTable.refresh();
  }

  private void loadAvatarThumbnail() {
    avatarView.setImage(avatarLoader.load(avatarPath, 56));
  }

  /**
   * Returns the visible player label text.
   *
   * @return displayed player name
   */
  public String getDisplayedPlayerName() {
    return playerLabel.getText();
  }

  /**
   * Returns the visible balance label text.
   *
   * @return displayed balance
   */
  public String getDisplayedBalance() {
    return balanceLabel.getText();
  }

  /**
   * Returns the visible portfolio return text.
   *
   * @return displayed portfolio return text
   */
  public String getPortfolioReturnText() {
    return portfolioReturnValueLabel.getText();
  }

  /**
   * Returns the visible benchmark return text.
   *
   * @return displayed benchmark return text
   */
  public String getBenchmarkReturnText() {
    return benchmarkReturnValueLabel.getText();
  }

  /**
   * Returns the number of visible holdings rows.
   *
   * @return current holdings row count
   */
  public int getHoldingCount() {
    return holdings.size();
  }

  private static String formatMetricValue(MetricValue metric, boolean percentDisplay) {
    if (!metric.isAvailable()) {
      return switch (metric.status()) {
        case NO_TRADES -> "N/A (no trades yet)";
        case INSUFFICIENT_HISTORY -> "N/A (need more history)";
        case ZERO_VOLATILITY -> "N/A (zero volatility)";
        case AVAILABLE -> throw new IllegalArgumentException("Available metrics do not need fallback text.");
      };
    }

    BigDecimal value = metric.value();
    if (percentDisplay) {
      return value.multiply(BigDecimal.valueOf(100))
          .setScale(2, RoundingMode.HALF_UP)
          .toPlainString() + "%";
    }
    return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
  }

  private static void styleButton(Button button) {
    ThemeStyles.styleButton(button);
  }
}
