package model.analysis.recommendation;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Algorithms for computing a {@link StockRecommendation} from ordered historical prices.
 *
 * <p>Injected into {@link StockRecommendationService}, following the same swappable pattern as
 * market strategies on {@link model.core.market.Exchange}.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public enum RecommendationStrategy {

  /** Compares the start of a recent lookback window to the latest price. */
  TREND,

  /** Compares percentage change in the older half of a window to the newer half. */
  MOMENTUM,

  /** Compares the latest price to a simple moving average over a recent window. */
  MEAN_REVERSION;

  private static final int LOOKBACK_PRICE_POINTS = 4;
  private static final int DIVISION_SCALE = 8;
  private static final BigDecimal TREND_BUY_THRESHOLD = new BigDecimal("0.015");
  private static final BigDecimal TREND_SELL_THRESHOLD = new BigDecimal("-0.015");
  private static final BigDecimal MOMENTUM_BUY_THRESHOLD = new BigDecimal("0.005");
  private static final BigDecimal MOMENTUM_SELL_THRESHOLD = new BigDecimal("-0.005");
  private static final BigDecimal MEAN_REVERSION_THRESHOLD = new BigDecimal("0.015");

  /**
   * Computes the recommendation from the provided ordered price history.
   *
   * @param historicalPrices ordered price history, oldest to newest
   * @return recommendation for the stock
   */
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    return switch (this) {
      case TREND -> recommendTrend(historicalPrices);
      case MOMENTUM -> recommendMomentum(historicalPrices);
      case MEAN_REVERSION -> recommendMeanReversion(historicalPrices);
    };
  }

  private static StockRecommendation recommendTrend(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < 2) {
      return StockRecommendation.HOLD;
    }

    BigDecimal startPrice = historicalPrices.get(recentStartIndex(historicalPrices, LOOKBACK_PRICE_POINTS));
    BigDecimal endPrice = historicalPrices.getLast();
    if (startPrice.signum() == 0) {
      return StockRecommendation.HOLD;
    }

    return classify(
        pctChange(startPrice, endPrice), TREND_BUY_THRESHOLD, TREND_SELL_THRESHOLD);
  }

  private static StockRecommendation recommendMomentum(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < LOOKBACK_PRICE_POINTS) {
      return StockRecommendation.HOLD;
    }

    int start = historicalPrices.size() - LOOKBACK_PRICE_POINTS;
    BigDecimal w0 = historicalPrices.get(start);
    BigDecimal w1 = historicalPrices.get(start + 1);
    BigDecimal w2 = historicalPrices.get(start + 2);
    BigDecimal w3 = historicalPrices.get(start + 3);

    if (w0.signum() == 0 || w2.signum() == 0) {
      return StockRecommendation.HOLD;
    }

    BigDecimal momentum = pctChange(w2, w3).subtract(pctChange(w0, w1));
    return classify(momentum, MOMENTUM_BUY_THRESHOLD, MOMENTUM_SELL_THRESHOLD);
  }

  private static StockRecommendation recommendMeanReversion(List<BigDecimal> historicalPrices) {
    if (historicalPrices.size() < 2) {
      return StockRecommendation.HOLD;
    }

    int startIndex = recentStartIndex(historicalPrices, LOOKBACK_PRICE_POINTS);
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

    BigDecimal deviation = pctChange(average, historicalPrices.getLast());
    if (deviation.compareTo(MEAN_REVERSION_THRESHOLD.negate()) <= 0) {
      return StockRecommendation.BUY;
    }
    if (deviation.compareTo(MEAN_REVERSION_THRESHOLD) >= 0) {
      return StockRecommendation.SELL;
    }
    return StockRecommendation.HOLD;
  }

  private static BigDecimal pctChange(BigDecimal from, BigDecimal to) {
    return to.subtract(from).divide(from, DIVISION_SCALE, RoundingMode.HALF_UP);
  }

  private static StockRecommendation classify(
      BigDecimal metric, BigDecimal buyThreshold, BigDecimal sellThreshold) {
    if (metric.compareTo(buyThreshold) >= 0) {
      return StockRecommendation.BUY;
    }
    if (metric.compareTo(sellThreshold) <= 0) {
      return StockRecommendation.SELL;
    }
    return StockRecommendation.HOLD;
  }

  private static int recentStartIndex(List<BigDecimal> historicalPrices, int lookback) {
    return Math.max(0, historicalPrices.size() - lookback);
  }
}
