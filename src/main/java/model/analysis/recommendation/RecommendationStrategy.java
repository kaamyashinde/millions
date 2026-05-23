package model.analysis.recommendation;


import model.core.market.Exchange;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy for computing a {@link StockRecommendation} from ordered historical prices.
 *
 * <p>Implementations are interchangeable and can be injected into {@link StockRecommendationService},
 * following the same pattern as market strategies on {@link model.core.market.Exchange}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface RecommendationStrategy {

  /**
   * Computes the recommendation from the provided ordered price history.
   *
   * @param historicalPrices ordered price history, oldest to newest
   * @return recommendation for the stock
   */
  StockRecommendation recommend(List<BigDecimal> historicalPrices);
}
