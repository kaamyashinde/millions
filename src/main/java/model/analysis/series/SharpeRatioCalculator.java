package model.analysis.series;


import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculates a simplified Sharpe ratio using zero risk-free return.
 */
public class SharpeRatioCalculator {

  private static final int SCALE = 8;

  /**
   * Computes the Sharpe ratio from daily returns and a precomputed volatility.
   *
   * @param dailyReturns ordered daily return ratios
   * @param volatility standard deviation of the same return series
   * @return average daily return divided by volatility
   */
  public BigDecimal calculate(List<BigDecimal> dailyReturns, BigDecimal volatility) {
    checkNotNull(dailyReturns, "Daily returns");
    checkNotNull(volatility, "Volatility");
    if (dailyReturns.isEmpty()) {
      throw new IllegalArgumentException("At least one daily return is required.");
    }
    if (volatility.signum() == 0) {
      throw new IllegalArgumentException("Volatility must be non-zero.");
    }

    BigDecimal averageReturn = BigDecimal.valueOf(dailyReturns.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .average()
        .orElseThrow());
    return averageReturn.divide(volatility, SCALE, RoundingMode.HALF_UP);
  }
}
