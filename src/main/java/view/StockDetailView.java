package view;

import java.math.BigDecimal;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Stock;
import model.marketevent.MarketEvent;
import recommendation.StockRecommendation;
import recommendation.StockRecommendationService;
import view.components.chart.StockChart;
import view.components.recommendation.StockRecommendationLabel;

/**
 * Dedicated stock detail view showing summary data, trend-based recommendation, and price history.
 *
 * <p>The recommendation is computed through {@link StockRecommendationService}, keeping expert
 * advice presentation separate from the stock's actual price-update logic.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class StockDetailView extends BorderPane {

  private final StockRecommendationService recommendationService = new StockRecommendationService();

  private final Label titleLabel = new Label("Stock details");
  private final Label subtitleLabel = new Label("Select a stock to inspect its latest trend.");
  private final Label dayLabel = new Label("Trading day: -");
  private final Label latestPriceLabel = new Label("Latest price: -");
  private final Label marketEventLabel = new Label("Latest market event: none");
  private final Label basisLabel = new Label("Recommendation basis: recent price trend");
  private final StockRecommendationLabel recommendationLabel =
      new StockRecommendationLabel(StockRecommendation.HOLD);
  private final Label placeholderLabel =
      new Label("Choose a stock from the list to view chart and recommendation details.");
  private final VBox recommendationBox;
  private final VBox content = new VBox(16);

  private Stock selectedStock;
  private Optional<MarketEvent> selectedMarketEvent = Optional.empty();

  /**
   * Builds an initially empty stock detail view.
   */
  public StockDetailView() {
    setPadding(new Insets(16));
    setPrefWidth(430);

    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
    subtitleLabel.setWrapText(true);
    marketEventLabel.setWrapText(true);
    basisLabel.setWrapText(true);
    placeholderLabel.setWrapText(true);

    VBox header = new VBox(6, titleLabel, subtitleLabel, dayLabel, latestPriceLabel, marketEventLabel);
    recommendationBox = new VBox(8, new Label("Recommendation"), recommendationLabel, basisLabel);
    recommendationBox.setStyle(
        "-fx-background-color: #f6f7fb;"
            + "-fx-border-color: #d7dce5;"
            + "-fx-background-radius: 12;"
            + "-fx-border-radius: 12;"
            + "-fx-padding: 14;");

    content.getChildren().addAll(recommendationBox, placeholderLabel);
    VBox.setVgrow(content, Priority.ALWAYS);

    setTop(header);
    setCenter(content);
    showStock(null, 0);
  }

  /**
   * Displays details for the selected stock and refreshes the recommendation and chart.
   *
   * @param stock selected stock, or {@code null} to show the empty state
   * @param tradingDay current exchange trading day
   */
  public void showStock(Stock stock, int tradingDay) {
    showStock(stock, tradingDay, Optional.empty());
  }

  /**
   * Displays details for the selected stock and the latest market event relevant to the current day.
   *
   * @param stock selected stock, or {@code null} to show the empty state
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if one occurred on the current day
   */
  public void showStock(Stock stock, int tradingDay, Optional<MarketEvent> marketEvent) {
    selectedStock = stock;
    selectedMarketEvent = marketEvent;
    dayLabel.setText("Trading day: " + (tradingDay > 0 ? tradingDay : "-"));

    if (stock == null) {
      titleLabel.setText("Stock details");
      subtitleLabel.setText("Select a stock to inspect its latest trend.");
      latestPriceLabel.setText("Latest price: -");
      marketEventLabel.setText("Latest market event: none");
      recommendationLabel.setRecommendation(StockRecommendation.HOLD);
      placeholderLabel.setText("Choose a stock from the list to view chart and recommendation details.");
      content.getChildren().setAll(recommendationBox, placeholderLabel);
      return;
    }

    titleLabel.setText(stock.getSymbol() + " · " + stock.getCompany());
    subtitleLabel.setText("Single-stock detail view with trend recommendation.");
    latestPriceLabel.setText("Latest price: " + formatLatestPrice(stock));
    marketEventLabel.setText(buildMarketEventText(stock, marketEvent));
    recommendationLabel.setRecommendation(recommendationService.recommend(stock));

    if (stock.getHistoricalPrices().isEmpty()) {
      placeholderLabel.setText("No price history is available for this stock yet.");
      content.getChildren().setAll(recommendationBox, placeholderLabel);
      return;
    }

    StockChart chart = new StockChart(stock);
    chart.setMinHeight(280);
    VBox.setVgrow(chart, Priority.ALWAYS);
    content.getChildren().setAll(recommendationBox, chart);
  }

  /**
   * Refreshes the view for the currently selected stock.
   *
   * @param tradingDay current exchange trading day
   */
  public void refresh(int tradingDay) {
    showStock(selectedStock, tradingDay, selectedMarketEvent);
  }

  /**
   * Refreshes the view while also updating the latest market event context.
   *
   * @param tradingDay current exchange trading day
   * @param marketEvent latest market event, if one occurred on the current day
   */
  public void refresh(int tradingDay, Optional<MarketEvent> marketEvent) {
    showStock(selectedStock, tradingDay, marketEvent);
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
   * Formats the latest price if one exists.
   *
   * @param stock stock whose latest price should be shown
   * @return formatted latest price or placeholder text
   */
  private static String formatLatestPrice(Stock stock) {
    if (stock.getHistoricalPrices().isEmpty()) {
      return "-";
    }
    BigDecimal latestPrice = stock.getSalesPrice();
    return latestPrice.toPlainString();
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
}
