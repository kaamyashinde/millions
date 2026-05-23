package model.analysis.recommendation.strategy;


import model.analysis.recommendation.RecommendationStrategy;
import model.analysis.recommendation.StockRecommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Momentum recommendation: compares the percentage change in the older half of a recent window to
 * the percentage change in the newer half. Positive acceleration suggests {@link StockRecommendation#BUY};
 * negative acceleration or sharp recent decline suggests {@link StockRecommendation#SELL}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class MomentumRecommendationStrategy implements RecommendationStrategy {

  private static final int WINDOW_SIZE = 4;
  private static final int DIVISION_SCALE = 8;
  private static final BigDecimal BUY_THRESHOLD = new BigDecimal("0.005");
  private static final BigDecimal SELL_THRESHOLD = new BigDecimal("-0.005");

  @Override
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < WINDOW_SIZE) {
      return StockRecommendation.HOLD;
    }

    int start = historicalPrices.size() - WINDOW_SIZE;
    BigDecimal w0 = historicalPrices.get(start);
    BigDecimal w1 = historicalPrices.get(start + 1);
    BigDecimal w2 = historicalPrices.get(start + 2);
    BigDecimal w3 = historicalPrices.get(start + 3);

    if (w0.signum() == 0 || w2.signum() == 0) {
      return StockRecommendation.HOLD;
    }

    BigDecimal olderPct =
        w1.subtract(w0).divide(w0, DIVISION_SCALE, RoundingMode.HALF_UP);
    BigDecimal recentPct =
        w3.subtract(w2).divide(w2, DIVISION_SCALE, RoundingMode.HALF_UP);
    BigDecimal momentum = recentPct.subtract(olderPct);

    if (momentum.compareTo(BUY_THRESHOLD) >= 0) {
      return StockRecommendation.BUY;
    }
    if (momentum.compareTo(SELL_THRESHOLD) <= 0) {
      return StockRecommendation.SELL;
    }
    return StockRecommendation.HOLD;
  }
}
