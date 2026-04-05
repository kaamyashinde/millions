package model.analysis.recommendation;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import model.market.Stock;

/**
 * Computes a trend-based recommendation from recent stock price data.
 *
 * <p>Compares the start of a recent lookback window to the latest price. If the relative change
 * exceeds positive or negative thresholds, returns {@link StockRecommendation#BUY} or
 * {@link StockRecommendation#SELL}; otherwise {@link StockRecommendation#HOLD}.
 *
 * <p>This service only reads historical prices. It does not mutate {@link Stock} instances or
 * participate in exchange price generation.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class StockRecommendationService {

  private static final int LOOKBACK_PRICE_POINTS = 4;
  private static final BigDecimal BUY_THRESHOLD = new BigDecimal("0.015");
  private static final BigDecimal SELL_THRESHOLD = new BigDecimal("-0.015");
  private static final int DIVISION_SCALE = 8;

  /**
   * Computes the recommendation for the given stock from its recent historical prices.
   *
   * @param stock stock whose recent trend should be analyzed
   * @return recommendation for the stock
   * @throws NullPointerException if {@code stock} is null
   */
  public StockRecommendation recommend(Stock stock) {
    checkNotNull(stock, "Stock");
    return recommend(stock.getHistoricalPrices());
  }

  /**
   * Computes the recommendation from the provided ordered price history.
   *
   * <p>Returns {@link StockRecommendation#HOLD} when history has fewer than two prices or when the
   * lookback start price is zero.
   *
   * @param historicalPrices ordered price history, oldest to newest
   * @return recommendation based on recent price trend
   * @throws NullPointerException if {@code historicalPrices} is null
   */
  public StockRecommendation recommend(List<BigDecimal> historicalPrices) {
    checkNotNull(historicalPrices, "Historical prices");
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

  private BigDecimal recentStartPrice(List<BigDecimal> historicalPrices) {
    int startIndex = Math.max(0, historicalPrices.size() - LOOKBACK_PRICE_POINTS);
    return historicalPrices.get(startIndex);
  }
}
