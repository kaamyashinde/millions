package model.core.player.portfolio.metrics.calc;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculates simplified volatility as the population standard deviation of daily returns.
 */
public class VolatilityCalculator {

  private static final int SCALE = 8;

  /**
   * Computes volatility from the provided daily return series.
   *
   * @param dailyReturns ordered daily return ratios
   * @return population standard deviation of the returns
   */
  public BigDecimal calculate(List<BigDecimal> dailyReturns) {
    checkNotNull(dailyReturns, "Daily returns");
    if (dailyReturns.size() < 2) {
      throw new IllegalArgumentException("At least two daily returns are required.");
    }

    double mean = dailyReturns.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .average()
        .orElseThrow();

    double variance = dailyReturns.stream()
        .mapToDouble(val -> {
          double delta = val.doubleValue() - mean;
          return delta * delta;
        })
        .average()
        .orElseThrow();

    return BigDecimal.valueOf(Math.sqrt(variance)).setScale(SCALE, RoundingMode.HALF_UP);
  }
}
