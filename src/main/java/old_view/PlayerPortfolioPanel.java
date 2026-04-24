package old_view;

import static model.utils.Validator.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
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
import javafx.scene.image.Image;
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

/**
 * JavaFX panel showing player summary data, current holdings, and portfolio-vs-market metrics.
 */
public class PlayerPortfolioPanel extends BorderPane {

  private static final String CARD_STYLE =
      "-fx-background-color: #f6f7fb;"
          + "-fx-border-color: #d7dce5;"
          + "-fx-background-radius: 12;"
          + "-fx-border-radius: 12;"
          + "-fx-padding: 14;";

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

  /**
   * Builds a player summary panel backed by the given exchange and player.
   *
   * @param exchange exchange supplying market and trading-day state
   * @param player player whose summary and holdings should be shown
   * @param avatarPath path to profile avatar image (may not exist yet)
   */
  public PlayerPortfolioPanel(Exchange exchange, Player player, Path avatarPath) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    checkNotNull(avatarPath, "avatarPath");
    this.exchange = exchange;
    this.player = player;
    this.avatarPath = avatarPath;

    setPadding(new Insets(16));

    Text heading = new Text("Player overview");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));

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
    summaryGrid.setHgap(18);
    summaryGrid.setVgap(8);
    summaryGrid.addRow(0, new Label("Player"), playerLabel, new Label("Trading day"), tradingDayLabel);
    summaryGrid.addRow(1, new Label("Balance"), balanceLabel, new Label("Net worth"), netWorthLabel);

    VBox top = new VBox(12, topRow, summaryGrid);
    setTop(top);

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

    TableColumn<Share, String> purchasePriceColumn = new TableColumn<>("Purchase price");
    purchasePriceColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getPurchasePrice().toPlainString()));

    TableColumn<Share, String> currentPriceColumn = new TableColumn<>("Current price");
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
    Label heading = new Label("Risk-adjusted performance vs market");
    heading.setFont(Font.font("System", FontWeight.BOLD, 16));

    GridPane metricsGrid = new GridPane();
    metricsGrid.setHgap(18);
    metricsGrid.setVgap(8);

    metricsGrid.addRow(0, new Label("Metric"), new Label("Your portfolio"), new Label("Market benchmark"));
    metricsGrid.addRow(1, new Label("Return %"), portfolioReturnValueLabel, benchmarkReturnValueLabel);
    metricsGrid.addRow(
        2,
        new Label("Volatility"),
        portfolioVolatilityValueLabel,
        benchmarkVolatilityValueLabel);
    metricsGrid.addRow(3, new Label("Sharpe ratio"), portfolioSharpeValueLabel, benchmarkSharpeValueLabel);

    VBox metricsBox = new VBox(10, heading, metricsGrid);
    metricsBox.setPadding(new Insets(14, 0, 0, 0));
    metricsBox.setStyle(CARD_STYLE);
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
    avatarView.setImage(null);
    if (!Files.isRegularFile(avatarPath)) {
      return;
    }
    try (InputStream in = Files.newInputStream(avatarPath)) {
      avatarView.setImage(new Image(in, 56, 56, true, true));
    } catch (IOException exception) {
      avatarView.setImage(null);
    }
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
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }
}
