package model.market.marketevent.daily;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.Random;
import model.market.Stock;

/**
 * Applies the standard daily fluctuation band used by the exchange when no rare event is involved.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
 */
public class UniformDailyPriceMoveStrategy implements DailyPriceMoveStrategy {

  private final double dailySigma;

  /**
   * Creates a strategy that samples uniformly within {@code [-dailySigma, +dailySigma]}.
   *
   * @param dailySigma maximum absolute percentage move for the normal daily band
   */
  public UniformDailyPriceMoveStrategy(double dailySigma) {
    this.dailySigma = dailySigma;
  }

  /**
   * Calculates the next-day price using the stock's current price and a uniform daily move band.
   *
   * @param stock  stock whose current price is being advanced
   * @param random random source used to sample the daily move
   * @return baseline next-day price before any rare event shock
   */
  @Override
  public BigDecimal calculateNextPrice(Stock stock, Random random) {
    checkNotNull(stock, "Stock");
    checkNotNull(random, "Random");
    double factor = 1 + random.nextDouble(-dailySigma, dailySigma);
    return stock.getSalesPrice().multiply(BigDecimal.valueOf(factor));
  }
}
