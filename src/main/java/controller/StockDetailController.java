package controller;

import static model.utils.Validator.checkNotNull;

import java.util.List;
import java.util.Optional;
import model.Exchange;
import model.Stock;
import model.marketevent.MarketEvent;
import model.stockinfo.StockFinancialInfo;
import model.stockinfo.StockFinancialInfoProvider;
import model.recommendation.StockRecommendation;
import model.recommendation.StockRecommendationService;

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
