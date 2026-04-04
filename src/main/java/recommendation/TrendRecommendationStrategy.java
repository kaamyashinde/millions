package recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Trend-based recommendation: compares the start of a recent lookback window to the latest price.
 *
 * <p>If the relative change exceeds positive or negative thresholds, returns {@link StockRecommendation#BUY}
 * or {@link StockRecommendation#SELL}; otherwise {@link StockRecommendation#HOLD}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class TrendRecommendationStrategy implements RecommendationStrategy {

  private static final int LOOKBACK_PRICE_POINTS = 4;
  private static final BigDecimal BUY_THRESHOLD = new BigDecimal("0.015");
  private static final BigDecimal SELL_THRESHOLD = new BigDecimal("-0.015");
  private static final int DIVISION_SCALE = 8;

  @Override
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < 2) {
      return StockRecommendation.HOLD;
    }

    BigDecimal startPrice = recentStartPrice(historicalPrices);
    BigDecimal endPrice = historicalPrices.getLast();
    if (startPrice.signum() == 0) {
      return StockRecommendation.HOLD;
    }

    BigDecimal trendRatio =
        endPrice.subtract(startPrice).divide(startPrice, DIVISION_SCALE, RoundingMode.HALF_UP);
    if (trendRatio.compareTo(BUY_THRESHOLD) >= 0) {
      return StockRecommendation.BUY;
    }
    if (trendRatio.compareTo(SELL_THRESHOLD) <= 0) {
      return StockRecommendation.SELL;
    }
    return StockRecommendation.HOLD;
  }

  /**
   * Returns the oldest price inside the recent lookback window used for trend comparison.
   *
   * @param historicalPrices ordered price history, oldest to newest
   * @return starting price for the recent trend window
   */
  private BigDecimal recentStartPrice(List<BigDecimal> historicalPrices) {
    int startIndex = Math.max(0, historicalPrices.size() - LOOKBACK_PRICE_POINTS);
    return historicalPrices.get(startIndex);
  }
}
