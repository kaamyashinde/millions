package model.core.market.pricing;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.Random;
import model.core.asset.Stock;

/**
 * Calculates the baseline next-day price before any rare market event is applied.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface DailyPriceMoveStrategy {

  /**
   * Creates the standard uniform daily movement strategy.
   *
   * @param dailySigma maximum absolute daily movement factor
   * @return strategy that samples uniformly from {@code -dailySigma} to {@code dailySigma}
   */
  static DailyPriceMoveStrategy uniform(double dailySigma) {
    return (stock, random) -> {
      checkNotNull(stock, "Stock");
      checkNotNull(random, "Random");
      double factor = 1 + random.nextDouble(-dailySigma, dailySigma);
      return stock.getSalesPrice().multiply(BigDecimal.valueOf(factor));
    };
  }

  /**
   * Calculates the next baseline price for one stock.
   *
   * @param stock stock whose latest sales price is used as the base
   * @param random random source for stochastic strategies
   * @return next price before rare market-event shocks
   */
  BigDecimal calculateNextPrice(Stock stock, Random random);
}
