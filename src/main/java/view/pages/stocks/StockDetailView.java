package view.pages.stocks;

import controller.StockDetailController;
import controller.TradingController;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import model.analysis.recommendation.StockRecommendation;
import model.analysis.recommendation.StockRecommendationService;
import model.core.asset.Stock;
import model.core.asset.info.StockFinancialInfo;
import model.core.asset.info.StockFinancialInfoProvider;
import model.core.market.event.MarketEvent;
import view.components.recommendation.StockRecommendationLabel;
import view.dialogs.TradeDialog;
import view.theme.ThemeStyles;
import view.util.UiFormat;

/**
 * Dedicated stock detail view showing summary data, mock company fundamentals, trend-based
 * recommendation, and market event context.
 *
 * <p>The recommendation is computed through {@link StockRecommendationService}, keeping expert
 * advice presentation separate from the stock's actual price-update logic. Mock revenue and profit
 * come from {@link StockFinancialInfoProvider}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class StockDetailView extends BorderPane {

  private final StockRecommendationService recommendationService = new StockRecommendationService();
  private final StockFinancialInfoProvider financialInfoProvider = new StockFinancialInfoProvider();

  private final Label titleLabel = new Label("Stock details");
  private final Label subtitleLabel = new Label("Select a stock to inspect its latest trend.");
  private final Label dayLabel = new Label("Trading day: -");
  private final Label latestPriceLabel = new Label("Latest price: -");
  private final Label marketEventLabel = new Label("Latest market event: none");
  private final Label fundamentalsHeading = new Label("Company fundamentals");
  private final Label revenueLabel = new Label("Revenue: -");
  private final Label profitLabel = new Label("Profit: -");
  private final Label healthLabel = new Label("Health: -");
  private final VBox fundamentalsBox;
  private final Label marketHistoryHeading = new Label("Past events");
  private final Label basisLabel = new Label("Recommendation basis: recent price trend");
  private final StockRecommendationLabel recommendationLabel =
      new StockRecommendationLabel(StockRecommendation.HOLD);
  private final Label placeholderLabel =
      new Label("Choose a stock from the list to view chart and recommendation details.");
  private final ListView<MarketEvent> marketHistoryList = new ListView<>();
  private final VBox recommendationBox;
  private final VBox content = new VBox(16);

  private final HBox tradeActionsBox = new HBox(10);
  private final Button buyButton = new Button("Buy");
  private final Button sellButton = new Button("Sell");

  private Stock selectedStock;
  private Optional<MarketEvent> selectedMarketEvent = Optional.empty();
  private List<MarketEvent> selectedMarketHistory = List.of();
  private StockDetailController stockDetailController;
  private TradingController tradingController;
  private Supplier<Window> dialogOwnerSupplier;
  private Runnable onTradeComplete;
  private Consumer<MarketEvent> onEventClicked;

  /**
   * Builds an initially empty stock detail view.
   */
  public StockDetailView() {
    setPadding(new Insets(16));
    setPrefWidth(430);

    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
    subtitleLabel.setWrapText(true);
    marketEventLabel.setWrapText(true);
    marketHistoryHeading.setFont(Font.font("System", FontWeight.BOLD, 14));
    basisLabel.setWrapText(true);
    placeholderLabel.setWrapText(true);
    marketHistoryList.setPlaceholder(new Label("No past events for this stock yet."));
    marketHistoryList.setCellFactory(unused -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(MarketEvent event, boolean empty) {
        super.updateItem(event, empty);
        if (empty || event == null) {
          setText(null);
        } else {
          setText(formatMarketHistoryItem(event));
        }
      }
    });
    marketHistoryList.setOnMouseClicked(
        event -> {
          if (event.getClickCount() != 1) {
            return;
          }
          MarketEvent selected = marketHistoryList.getSelectionModel().getSelectedItem();
          if (selected != null && onEventClicked != null) {
            onEventClicked.accept(selected);
          }
        });
    marketHistoryList.setMaxHeight(140);

    fundamentalsHeading.setFont(Font.font("System", FontWeight.BOLD, 14));
    revenueLabel.setWrapText(true);
    profitLabel.setWrapText(true);
    healthLabel.setWrapText(true);
    fundamentalsBox =
        new VBox(4, fundamentalsHeading, revenueLabel, profitLabel, healthLabel);
    ThemeStyles.addStyleClasses(fundamentalsBox, "card");

    final VBox header =
        new VBox(
            10,
            titleLabel,
            subtitleLabel,
            dayLabel,
            latestPriceLabel,
            marketEventLabel,
            fundamentalsBox);
    recommendationBox = new VBox(8, new Label("Recommendation"), recommendationLabel, basisLabel);
    ThemeStyles.addStyleClasses(recommendationBox, "finance-summary-card");

    ThemeStyles.styleAccentButton(buyButton);
    ThemeStyles.styleDangerButton(sellButton);
    tradeActionsBox.getChildren().addAll(buyButton, sellButton);
    tradeActionsBox.setVisible(false);
    tradeActionsBox.setManaged(false);

    content.getChildren().addAll(tradeActionsBox, recommendationBox, placeholderLabel);
    VBox.setVgrow(content, Priority.ALWAYS);

    setTop(header);
    setCenter(content);
    showStock(null, 0);
  }

  /**
   * Displays details for the selected stock and refreshes the recommendation.
   *
   * @param stock selected stock, or {@code null} to show the empty state
   * @param tradingDay current exchange trading day
   */
  public void showStock(Stock stock, int tradingDay) {
    showStock(stock, tradingDay, Optional.empty(), List.of());
  }

  /**
   * Displays details for the selected stock and the latest market event relevant to the current
   * day.
   *
   * @param stock selected stock, or {@code null} to show the empty state
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if one occurred on the current day
   */
  public void showStock(Stock stock, int tradingDay, Optional<MarketEvent> marketEvent) {
    showStock(stock, tradingDay, marketEvent, List.of());
  }

  /**
   * Displays stock details without a stock detail controller.
   *
   * @param stock selected stock, or {@code null} for the empty state
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if any
   * @param marketHistory chronological market events for the selected stock
   */
  public void showStock(
      Stock stock,
      int tradingDay,
      Optional<MarketEvent> marketEvent,
      List<MarketEvent> marketHistory) {
    showStock(stock, tradingDay, marketEvent, marketHistory, null);
  }

  /**
   * Displays stock details using an optional {@link StockDetailController} for fundamentals.
   *
   * @param stock selected stock, or {@code null} for the empty state
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if any
   * @param marketHistory chronological market events for the selected stock
   * @param stockDetail controller for fundamentals and recommendations, or {@code null}
   */
  public void showStock(
      Stock stock,
      int tradingDay,
      Optional<MarketEvent> marketEvent,
      List<MarketEvent> marketHistory,
      StockDetailController stockDetail) {
    stockDetailController = stockDetail;
    selectedStock = stock;
    selectedMarketEvent = marketEvent;
    selectedMarketHistory = List.copyOf(marketHistory);
    dayLabel.setText("Trading day: " + (tradingDay > 0 ? tradingDay : "-"));

    if (stock == null) {
      titleLabel.setText("Stock details");
      subtitleLabel.setText("Select a stock to inspect its latest trend.");
      latestPriceLabel.setText("Latest price: -");
      marketEventLabel.setText("Latest market event: none");
      clearFundamentalsLabels();
      marketHistoryList.setItems(FXCollections.observableArrayList());
      recommendationLabel.setRecommendation(StockRecommendation.HOLD);
      placeholderLabel.setText(
          "Choose a stock from the list to view chart and recommendation details.");
      content.getChildren().setAll(tradeActionsBox, recommendationBox, placeholderLabel);
      updateTradeActions(null);
      return;
    }

    titleLabel.setText(stock.getSymbol() + " · " + stock.getCompany());
    subtitleLabel.setText("Single-stock detail view with trend recommendation.");
    latestPriceLabel.setText("Latest price: " + formatLatestPrice(stock));
    marketEventLabel.setText(buildMarketEventText(stock, marketEvent));
    applyFundamentalsLabels(stock);
    marketHistoryList.setItems(
        FXCollections.observableArrayList(buildMarketHistoryList(marketHistory)));
    recommendationLabel.setRecommendation(resolveRecommendation(stock));
    updateTradeActions(stock);

    if (stock.getHistoricalPrices().isEmpty()) {
      placeholderLabel.setText("No price history is available for this stock yet.");
      content.getChildren().setAll(
          tradeActionsBox,
          recommendationBox,
          marketHistoryHeading,
          marketHistoryList,
          placeholderLabel);
      return;
    }

    content.getChildren().setAll(
        tradeActionsBox, recommendationBox, marketHistoryHeading, marketHistoryList);
  }

  /**
   * Registers a callback invoked when the user clicks a past market event in the list.
   *
   * @param onEventClicked consumer receiving the clicked event, or {@code null} to clear
   */
  public void setOnEventClicked(Consumer<MarketEvent> onEventClicked) {
    this.onEventClicked = onEventClicked;
  }

  /**
   * Configures buy/sell actions shown when a stock is selected.
   *
   * @param trading trading controller
   * @param dialogOwnerSupplier supplies the modal owner window
   * @param onTradeComplete invoked after a successful trade
   */
  public void setTradeHandlers(
      TradingController trading,
      Supplier<Window> dialogOwnerSupplier,
      Runnable onTradeComplete) {
    this.tradingController = trading;
    this.dialogOwnerSupplier = dialogOwnerSupplier;
    this.onTradeComplete = onTradeComplete;
    wireTradeButtons();
    updateTradeActions(selectedStock);
  }

  private StockRecommendation resolveRecommendation(Stock stock) {
    if (stockDetailController != null) {
      return stockDetailController.recommend(stock);
    }
    return recommendationService.recommend(stock);
  }

  private void wireTradeButtons() {
    buyButton.setOnAction(unused -> {
      if (selectedStock == null || tradingController == null || dialogOwnerSupplier == null) {
        return;
      }
      TradeDialog.showBuy(
          dialogOwnerSupplier.get(),
          tradingController,
          selectedStock.getSymbol(),
          onTradeComplete);
    });
    sellButton.setOnAction(unused -> {
      if (selectedStock == null || tradingController == null || dialogOwnerSupplier == null) {
        return;
      }
      TradeDialog.showSell(
          dialogOwnerSupplier.get(),
          tradingController,
          selectedStock.getSymbol(),
          onTradeComplete);
    });
  }

  private void updateTradeActions(Stock stock) {
    if (tradingController == null || stock == null) {
      tradeActionsBox.setVisible(false);
      tradeActionsBox.setManaged(false);
      return;
    }
    tradeActionsBox.setVisible(true);
    tradeActionsBox.setManaged(true);
    buyButton.setText("Buy " + stock.getSymbol());
    boolean hasShares = tradingController.getOwnedQuantity(stock.getSymbol()).signum() > 0;
    sellButton.setVisible(hasShares);
    sellButton.setManaged(hasShares);
    if (hasShares) {
      sellButton.setText("Sell " + stock.getSymbol());
    }
  }

  /**
   * Refreshes the view for the currently selected stock.
   *
   * @param tradingDay current exchange trading day
   */
  public void refresh(int tradingDay) {
    showStock(selectedStock, tradingDay, selectedMarketEvent, selectedMarketHistory);
  }

  /**
   * Refreshes the view while also updating the latest market event context.
   *
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if one occurred on the current day
   */
  public void refresh(int tradingDay, Optional<MarketEvent> marketEvent) {
    showStock(selectedStock, tradingDay, marketEvent, selectedMarketHistory);
  }

  /**
   * Refreshes the view while also updating the stored event history.
   *
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if one occurred on the current day
   * @param marketHistory past market events relevant to the selected stock
   */
  public void refresh(
      int tradingDay, Optional<MarketEvent> marketEvent, List<MarketEvent> marketHistory) {
    showStock(selectedStock, tradingDay, marketEvent, marketHistory);
  }

  /**
   * Returns the stock currently displayed in the detail view.
   *
   * @return selected stock, or {@code null} when the view is empty
   */
  public Stock getSelectedStock() {
    return selectedStock;
  }

  /**
   * Returns the recommendation currently shown in the detail view.
   *
   * @return rendered recommendation badge value
   */
  public StockRecommendation getDisplayedRecommendation() {
    return recommendationLabel.getRecommendation();
  }

  /**
   * Returns the latest-price label text currently shown in the detail view.
   *
   * @return latest-price label text
   */
  public String getLatestPriceText() {
    return latestPriceLabel.getText();
  }

  /**
   * Returns the market-event label text currently shown in the detail view.
   *
   * @return market-event label text
   */
  public String getMarketEventText() {
    return marketEventLabel.getText();
  }

  /**
   * Returns the rendered past-event rows currently displayed in the detail view.
   *
   * @return immutable copy of the current past-event rows
   */
  public List<String> getDisplayedMarketHistory() {
    return marketHistoryList.getItems().stream()
        .map(StockDetailView::formatMarketHistoryItem)
        .toList();
  }

  /**
   * Returns the revenue line currently shown under company fundamentals.
   *
   * @return revenue label text
   */
  public String getRevenueLabelText() {
    return revenueLabel.getText();
  }

  /**
   * Returns the profit line currently shown under company fundamentals.
   *
   * @return profit label text
   */
  public String getProfitLabelText() {
    return profitLabel.getText();
  }

  /**
   * Returns the health line currently shown under company fundamentals.
   *
   * @return health label text
   */
  public String getHealthLabelText() {
    return healthLabel.getText();
  }

  /**
   * Formats the latest price if one exists.
   *
   * @param stock stock whose latest price should be shown
   * @return formatted latest price or placeholder text
   */
  private void clearFundamentalsLabels() {
    revenueLabel.setText("Revenue: -");
    profitLabel.setText("Profit: -");
    healthLabel.setText("Health: -");
  }

  private void applyFundamentalsLabels(Stock stock) {
    StockFinancialInfo fin =
        stockDetailController != null
            ? stockDetailController.financialInfo(stock)
            : financialInfoProvider.forStock(stock);
    revenueLabel.setText(
        "Revenue: "
            + (stockDetailController != null
                ? stockDetailController.formatMoney(fin.revenue())
                : financialInfoProvider.formatMoney(fin.revenue())));
    profitLabel.setText(
        "Profit: "
            + (stockDetailController != null
                ? stockDetailController.formatMoney(fin.profit())
                : financialInfoProvider.formatMoney(fin.profit())));
    healthLabel.setText("Health: " + fin.health().displayLabel());
  }

  private static String formatLatestPrice(Stock stock) {
    if (stock.getHistoricalPrices().isEmpty()) {
      return "-";
    }
    BigDecimal latestPrice = stock.getSalesPrice();
    return UiFormat.decimal(latestPrice);
  }

  /**
   * Builds the text shown for the latest market event in the detail view.
   *
   * @param stock currently selected stock
   * @param marketEvent latest market event for the exchange
   * @return user-facing market-event text
   */
  private static String buildMarketEventText(Stock stock, Optional<MarketEvent> marketEvent) {
    if (marketEvent.isEmpty()) {
      return "Latest market event: none";
    }
    MarketEvent event = marketEvent.get();
    if (!event.affects(stock)) {
      return "Latest market event: no active event for " + stock.getSymbol();
    }
    return "Latest market event: " + event.title() + " - " + event.description();
  }

  /**
   * Builds newest-first text rows for the past-events list.
   *
   * @param marketHistory past events relevant to the selected stock
   * @return rendered rows for the list view
   */
  private static List<MarketEvent> buildMarketHistoryList(List<MarketEvent> marketHistory) {
    List<MarketEvent> reversedHistory = new ArrayList<>(marketHistory);
    Collections.reverse(reversedHistory);
    return reversedHistory;
  }

  private static String formatMarketHistoryItem(MarketEvent event) {
    return "Day " + event.day() + " - " + event.title() + ": " + event.description();
  }
}
