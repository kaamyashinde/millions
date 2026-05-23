package model.core.market.pricing;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.Random;
import model.core.asset.Stock;

/** Calculates the baseline next-day price before any rare market event is applied. */
public interface DailyPriceMoveStrategy {

  /** Creates the standard daily movement strategy. */
  static DailyPriceMoveStrategy uniform(double dailySigma) {
    return (stock, random) -> {
      checkNotNull(stock, "Stock");
      checkNotNull(random, "Random");
      double factor = 1 + random.nextDouble(-dailySigma, dailySigma);
      return stock.getSalesPrice().multiply(BigDecimal.valueOf(factor));
    };
  }

  BigDecimal calculateNextPrice(Stock stock, Random random);
}
