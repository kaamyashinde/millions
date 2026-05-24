package view.pages.portfolio;

import static util.Validator.checkNotNull;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
import controller.ExitGameController;
import controller.HoldingSummary;
import controller.PortfolioController;
import controller.TradingController;
import util.I18n;
import view.dialogs.ExitGameDialog;
import view.theme.ThemePalette;
import model.analysis.performance.PerformanceComparison;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;
import view.dialogs.TradeDialog;
import view.theme.ThemeStyles;

/**
 * JavaFX panel showing player summary data, current holdings, and portfolio-vs-market metrics.
 */
public class PlayerPortfolioPage extends BorderPane {

  private final PortfolioController portfolio;
  private final TradingController trading;
  private final ExitGameController exitGame;
  private final Runnable onTradeComplete;
  private final Runnable onProfileDeleted;

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

  private final TableView<HoldingSummary> holdingsTable = new TableView<>();
  private final ImageView avatarView = new ImageView();
  private final ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());

  /**
   * @param portfolio portfolio summary and holdings
   * @param trading trading operations for sell dialog
   * @param exitGame exit-game flow controller
   * @param onTradeComplete invoked after a successful trade
   * @param onProfileDeleted invoked after profile deletion
   */
  public PlayerPortfolioPage(
      PortfolioController portfolio,
      TradingController trading,
      ExitGameController exitGame,
      Runnable onTradeComplete,
      Runnable onProfileDeleted) {
    checkNotNull(portfolio, "portfolio");
    checkNotNull(trading, "trading");
    checkNotNull(exitGame, "exitGame");
    checkNotNull(onTradeComplete, "onTradeComplete");
    checkNotNull(onProfileDeleted, "onProfileDeleted");
    this.portfolio = portfolio;
    this.trading = trading;
    this.exitGame = exitGame;
    this.onProfileDeleted = onProfileDeleted;
    this.onTradeComplete = onTradeComplete;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Player Overview");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    avatarView.setFitWidth(56);
    avatarView.setFitHeight(56);
    avatarView.setPreserveRatio(true);
    avatarView.setSmooth(true);

    HBox topRow = new HBox(16, avatarView, heading);
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
    holdingsTable.setItems(portfolio.getHoldings());
    holdingsTable.setPlaceholder(new Label("No holdings yet."));
    holdingsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(holdingsTable, Priority.ALWAYS);

    VBox metricsBox = buildMetricsBox();
    Button exitGameButton = new Button(I18n.get("exitGame.pin.confirm"));
    exitGameButton.setStyle("-fx-text-fill: " + ThemePalette.ERROR + ";");
    ThemeStyles.styleButton(exitGameButton);
    exitGameButton.setOnAction(_ -> {
      if (getScene() != null) {
        ExitGameDialog.show(getScene().getWindow(), exitGame, onProfileDeleted);
      }
    });
    VBox bottom = new VBox(12, metricsBox, exitGameButton);
    bottom.setPadding(new Insets(0, 0, 4, 0));
    setCenter(holdingsTable);
    setBottom(bottom);

    refresh();
  }

  private Label createHeaderLabel(String text) {
    Label label = new Label(text);
    ThemeStyles.addStyleClasses(label, "text-secondary");
    label.setStyle("-fx-font-weight: bold;");
    return label;
  }

  private void buildHoldingsTable() {
    TableColumn<HoldingSummary, String> symbolColumn = new TableColumn<>("Symbol");
    symbolColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().symbol()));

    TableColumn<HoldingSummary, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));

    TableColumn<HoldingSummary, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().assetType()));

    TableColumn<HoldingSummary, String> quantityColumn = new TableColumn<>("Quantity");
    quantityColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().totalQuantity().toPlainString()));

    TableColumn<HoldingSummary, String> purchasePriceColumn = new TableColumn<>("Avg. Purchase Price");
    purchasePriceColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().avgPurchasePrice().toPlainString()));

    TableColumn<HoldingSummary, String> currentPriceColumn = new TableColumn<>("Current Price");
    currentPriceColumn.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().currentPrice().toPlainString()));

    TableColumn<HoldingSummary, Void> actionsColumn = new TableColumn<>("Actions");
    actionsColumn.setPrefWidth(90);
    actionsColumn.setCellFactory(_ -> new TableCell<>() {
      private final Button sellButton = new Button("Sell");

      {
        ThemeStyles.styleButton(sellButton);
        sellButton.setOnAction(_ -> {
          HoldingSummary holding = getTableRow() != null ? getTableRow().getItem() : null;
          if (holding != null && getScene() != null) {
            TradeDialog.showSell(
                getScene().getWindow(),
                trading,
                holding.symbol(),
                onTradeComplete);
          }
        });
      }

      @Override
      protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : sellButton);
      }
    });

    holdingsTable.getColumns().setAll(
        symbolColumn,
        nameColumn,
        typeColumn,
        quantityColumn,
        purchasePriceColumn,
        currentPriceColumn,
        actionsColumn);
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
    portfolio.refresh();
    loadAvatarThumbnail();
    playerLabel.setText(portfolio.getPlayerName());
    tradingDayLabel.setText(Integer.toString(portfolio.getTradingDay()));
    balanceLabel.setText(portfolio.getFormattedBalance());
    netWorthLabel.setText(portfolio.getFormattedNetWorth());

    PerformanceComparison comparison = portfolio.getLastComparison();
    portfolioReturnValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.portfolio().returnPercent(), true));
    portfolioVolatilityValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.portfolio().volatility(), true));
    portfolioSharpeValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.portfolio().sharpeRatio(), false));
    benchmarkReturnValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.benchmark().returnPercent(), true));
    benchmarkVolatilityValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.benchmark().volatility(), true));
    benchmarkSharpeValueLabel.setText(
        PortfolioController.formatMetricValue(comparison.benchmark().sharpeRatio(), false));
    holdingsTable.refresh();
  }

  private void loadAvatarThumbnail() {
    avatarView.setImage(avatarLoader.load(portfolio.getAvatarPath(), 56));
  }

  /**
   * @return visible player label text
   */
  public String getDisplayedPlayerName() {
    return playerLabel.getText();
  }

  /**
   * @return visible balance label text
   */
  public String getDisplayedBalance() {
    return balanceLabel.getText();
  }

  /**
   * @return visible portfolio return text
   */
  public String getPortfolioReturnText() {
    return portfolioReturnValueLabel.getText();
  }

  /**
   * @return visible benchmark return text
   */
  public String getBenchmarkReturnText() {
    return benchmarkReturnValueLabel.getText();
  }

  /**
   * @return current holdings row count
   */
  public int getHoldingCount() {
    return portfolio.getHoldings().size();
  }
}
