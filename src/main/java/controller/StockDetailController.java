package controller;

import static util.Validator.checkNotNull;

import java.util.List;
import java.util.Optional;
import model.core.market.Exchange;
import model.core.asset.Stock;
import model.core.market.event.MarketEvent;
import model.core.asset.info.StockFinancialInfo;
import model.core.asset.info.StockFinancialInfoProvider;
import model.analysis.recommendation.StockRecommendation;
import model.analysis.recommendation.StockRecommendationService;

/**
 * Supplies stock detail data: fundamentals, recommendation, and market events.
 */
public class StockDetailController {

  private final Exchange exchange;
  private final StockRecommendationService recommendationService = new StockRecommendationService();
  private final StockFinancialInfoProvider financialInfoProvider = new StockFinancialInfoProvider();

  /**
   * @param exchange exchange supplying day and market events
   */
  public StockDetailController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public StockRecommendation recommend(Stock stock) {
    return recommendationService.recommend(stock);
  }

  public StockFinancialInfo financialInfo(Stock stock) {
    return financialInfoProvider.forStock(stock);
  }

  public String formatMoney(java.math.BigDecimal amount) {
    return financialInfoProvider.formatMoney(amount);
  }

  public Optional<MarketEvent> getLastMarketEvent() {
    return exchange.getLastMarketEvent();
  }

  public List<MarketEvent> getMarketHistory(Stock stock) {
    if (stock == null) {
      return List.of();
    }
    return exchange.getMarketEventsForStock(stock.getSymbol());
  }
}
