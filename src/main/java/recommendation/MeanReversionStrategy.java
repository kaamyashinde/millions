package recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Mean-reversion recommendation: compares the latest price to a simple moving average over a recent
 * window. Prices far below the average suggest {@link StockRecommendation#BUY}; far above suggest
 * {@link StockRecommendation#SELL}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class MeanReversionStrategy implements RecommendationStrategy {

  private static final int LOOKBACK_PRICE_POINTS = 4;
  private static final BigDecimal BUY_THRESHOLD = new BigDecimal("0.015");
  private static final BigDecimal SELL_THRESHOLD = new BigDecimal("0.015");
  private static final int DIVISION_SCALE = 8;

  @Override
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < 2) {
      return StockRecommendation.HOLD;
    }

    int startIndex = Math.max(0, historicalPrices.size() - LOOKBACK_PRICE_POINTS);
    List<BigDecimal> window = historicalPrices.subList(startIndex, historicalPrices.size());

    BigDecimal sum = BigDecimal.ZERO;
    for (BigDecimal price : window) {
      sum = sum.add(price);
    }
    BigDecimal average =
        sum.divide(BigDecimal.valueOf(window.size()), DIVISION_SCALE, RoundingMode.HALF_UP);
    if (average.signum() == 0) {
      return StockRecommendation.HOLD;
    }

    BigDecimal latest = historicalPrices.getLast();
    BigDecimal deviation =
        latest.subtract(average).divide(average, DIVISION_SCALE, RoundingMode.HALF_UP);

    if (deviation.compareTo(BUY_THRESHOLD.negate()) <= 0) {
      return StockRecommendation.BUY;
    }
    if (deviation.compareTo(SELL_THRESHOLD) >= 0) {
      return StockRecommendation.SELL;
    }
    return StockRecommendation.HOLD;
  }
}
