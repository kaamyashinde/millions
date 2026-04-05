package model.core.player.portfolio.metrics.calc;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds return series from ordered daily values.
 */
public class ReturnSeriesCalculator {

  private static final int SCALE = 8;

  /**
   * Computes the total return from the first value to the last value in the series.
   *
   * @param dailyValues ordered daily values, oldest to newest
   * @return decimal return ratio such as {@code 0.05} for 5%
   */
  public BigDecimal calculateTotalReturn(List<BigDecimal> dailyValues) {
    checkNotNull(dailyValues, "Daily values");
    if (dailyValues.size() < 2) {
      throw new IllegalArgumentException("At least two daily values are required.");
    }
    return calculateReturn(dailyValues.getFirst(), dailyValues.getLast());
  }

  /**
   * Computes consecutive daily returns from an ordered value series.
   *
   * @param dailyValues ordered daily values, oldest to newest
   * @return one decimal return per day transition
   */
  public List<BigDecimal> calculateDailyReturns(List<BigDecimal> dailyValues) {
    checkNotNull(dailyValues, "Daily values");
    if (dailyValues.size() < 2) {
      throw new IllegalArgumentException("At least two daily values are required.");
    }
    List<BigDecimal> returns = new ArrayList<>();
    for (int i = 1; i < dailyValues.size(); i++) {
      returns.add(calculateReturn(dailyValues.get(i - 1), dailyValues.get(i)));
    }
    return returns;
  }

  /**
   * Computes one decimal return from a start value to an end value.
   *
   * @param startValue starting value
   * @param endValue   ending value
   * @return decimal return ratio such as {@code 0.05} for 5%
   */
  public BigDecimal calculateReturn(BigDecimal startValue, BigDecimal endValue) {
    checkNotNull(startValue, "Start value");
    checkNotNull(endValue, "End value");
    if (startValue.signum() == 0) {
      throw new IllegalArgumentException("Start value must be non-zero.");
    }
    return endValue.subtract(startValue).divide(startValue, SCALE, RoundingMode.HALF_UP);
  }
}
