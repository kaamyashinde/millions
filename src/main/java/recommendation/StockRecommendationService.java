package recommendation;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import model.Stock;

/**
 * Computes a simplified recommendation from recent stock price data using a pluggable {@link RecommendationStrategy}.
 *
 * <p>This service only reads historical prices. It does not mutate {@link Stock} instances or
 * participate in exchange price generation, which keeps recommendation logic separate from actual
 * price logic.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class StockRecommendationService {

  private final RecommendationStrategy strategy;

  /**
   * Creates a service that uses {@link TrendRecommendationStrategy} by default.
   */
  public StockRecommendationService() {
    this(new TrendRecommendationStrategy());
  }

  /**
   * Creates a service that delegates to the given strategy.
   *
   * @param strategy recommendation algorithm to use
   * @throws NullPointerException if {@code strategy} is null
   */
  public StockRecommendationService(RecommendationStrategy strategy) {
    checkNotNull(strategy, "Recommendation strategy");
    this.strategy = strategy;
  }

  /**
   * Computes the recommendation for the given stock from its recent historical prices.
   *
   * @param stock stock whose recent trend should be analyzed
   * @return recommendation from the configured strategy
   * @throws NullPointerException if {@code stock} is null
   */
  public StockRecommendation recommend(Stock stock) {
    checkNotNull(stock, "Stock");
    return recommend(stock.getHistoricalPrices());
  }

  /**
   * Computes the recommendation from the provided ordered price history.
   *
   * <p>If there is not enough history for the strategy, the recommendation typically defaults to
   * {@link StockRecommendation#HOLD} (see the active {@link RecommendationStrategy}).
   *
   * @param historicalPrices ordered price history, oldest to newest
   * @return recommendation from the configured strategy
   * @throws NullPointerException if {@code historicalPrices} is null
   */
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    checkNotNull(historicalPrices, "Historical prices");
    return strategy.recommend(historicalPrices);
  }
}
