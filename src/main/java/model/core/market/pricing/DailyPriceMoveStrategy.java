package model.core.market.pricing;


import java.math.BigDecimal;
import java.util.Random;
import model.core.asset.Stock;

/**
 * Calculates the baseline next-day price for a stock before any rare market event is applied.
 *
 * @author OpenAI
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface DailyPriceMoveStrategy {

  /**
   * Calculates the stock price after the normal day-to-day fluctuation.
   *
   * @param stock stock whose current price is being advanced
   * @param random random source used by the strategy
   * @return the next baseline price before any market-event shock
   */
  BigDecimal calculateNextPrice(Stock stock, Random random);
}
