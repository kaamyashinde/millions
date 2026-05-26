package controller;

import static util.Validator.checkNotNull;

import java.util.List;
import java.util.Optional;
import model.analysis.recommendation.StockRecommendation;
import model.analysis.recommendation.StockRecommendationService;
import model.core.asset.Stock;
import model.core.asset.info.StockFinancialInfo;
import model.core.asset.info.StockFinancialInfoProvider;
import model.core.market.Exchange;
import model.core.market.event.MarketEvent;

/**
 * Supplies stock detail data: fundamentals, recommendation, and market events.
 *
 * <p>{@link view.pages.stocks.StockDetailView} uses this controller to keep presentation code
 * separate from {@link StockRecommendationService}, {@link StockFinancialInfoProvider}, and
 * {@link Exchange} event history.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-01
 */
public class StockDetailController {

  private final Exchange exchange;
  private final StockRecommendationService recommendationService = new StockRecommendationService();
  private final StockFinancialInfoProvider financialInfoProvider = new StockFinancialInfoProvider();

  /**
   * Creates a stock detail controller.
   *
   * @param exchange exchange supplying day and market events
   */
  public StockDetailController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
  }

  /**
   * Exposes the exchange used for stock detail data.
   *
   * @return exchange used for market event data
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Computes the current recommendation for a stock.
   *
   * @param stock stock whose price history should be analyzed
   * @return trend-based recommendation
   */
  public StockRecommendation recommend(Stock stock) {
    return recommendationService.recommend(stock);
  }

  /**
   * Looks up mock financial fundamentals for a stock.
   *
   * @param stock stock whose fundamentals should be displayed
   * @return financial information for the stock
   */
  public StockFinancialInfo financialInfo(Stock stock) {
    return financialInfoProvider.forStock(stock);
  }

  /**
   * Formats a money amount for detail labels.
   *
   * @param amount amount to format
   * @return formatted money text
   */
  public String formatMoney(java.math.BigDecimal amount) {
    return financialInfoProvider.formatMoney(amount);
  }

  /**
   * Returns the latest market event.
   *
   * @return most recent market event, if any
   */
  public Optional<MarketEvent> getLastMarketEvent() {
    return exchange.getLastMarketEvent();
  }

  /**
   * Returns event history related to a stock.
   *
   * @param stock selected stock, or {@code null}
   * @return matching market events, or an empty list when no stock is selected
   */
  public List<MarketEvent> getMarketHistory(Stock stock) {
    if (stock == null) {
      return List.of();
    }
    return exchange.getMarketEventsForStock(stock.getSymbol());
  }
}
