package model.analysis;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import model.analysis.metric.MetricStatus;
import model.analysis.metric.MetricValue;
import model.analysis.metric.PerformanceMetrics;

/**
 * Calculates performance metrics from an ordered daily value series.
 */
public final class PerformanceAnalyzer {

  private static final int SCALE = 8;

  private PerformanceAnalyzer() {
  }

  /**
   * Calculates return, volatility, and Sharpe ratio metrics from ordered daily values.
   *
   * @param dailyValues ordered daily values, oldest to newest
   * @return available and unavailable metrics according to the amount of history present
   */
  public static PerformanceMetrics calculateMetrics(List<BigDecimal> dailyValues) {
    checkNotNull(dailyValues, "Daily values");
    if (dailyValues.size() < 2) {
      return PerformanceMetrics.unavailable(MetricStatus.INSUFFICIENT_HISTORY);
    }

    List<BigDecimal> dailyReturns = calculateDailyReturns(dailyValues);
    MetricValue returnPercent = MetricValue.available(calculateTotalReturn(dailyValues));

    if (dailyReturns.size() < 2) {
      return new PerformanceMetrics(
          returnPercent,
          MetricValue.unavailable(MetricStatus.INSUFFICIENT_HISTORY),
          MetricValue.unavailable(MetricStatus.INSUFFICIENT_HISTORY));
    }

    BigDecimal volatility = calculateVolatility(dailyReturns);
    MetricValue volatilityMetric = MetricValue.available(volatility);
    MetricValue sharpeMetric = volatility.signum() == 0
        ? MetricValue.unavailable(MetricStatus.ZERO_VOLATILITY)
        : MetricValue.available(calculateSharpeRatio(dailyReturns, volatility));

    return new PerformanceMetrics(returnPercent, volatilityMetric, sharpeMetric);
  }

  /**
   * Computes total return from the first value to the last value.
   */
  private static BigDecimal calculateTotalReturn(List<BigDecimal> dailyValues) {
    return calculateReturn(dailyValues.getFirst(), dailyValues.getLast());
  }

  /**
   * Computes one return value for each adjacent pair in the value series.
   */
  private static List<BigDecimal> calculateDailyReturns(List<BigDecimal> dailyValues) {
    List<BigDecimal> returns = new ArrayList<>();
    for (int i = 1; i < dailyValues.size(); i++) {
      returns.add(calculateReturn(dailyValues.get(i - 1), dailyValues.get(i)));
    }
    return returns;
  }

  /**
   * Computes one decimal return from a start value to an end value.
   */
  private static BigDecimal calculateReturn(BigDecimal startValue, BigDecimal endValue) {
    checkNotNull(startValue, "Start value");
    checkNotNull(endValue, "End value");
    if (startValue.signum() == 0) {
      throw new IllegalArgumentException("Start value must be non-zero.");
    }
    return endValue.subtract(startValue).divide(startValue, SCALE, RoundingMode.HALF_UP);
  }

  /**
   * Computes volatility as the population standard deviation of daily returns.
   */
  private static BigDecimal calculateVolatility(List<BigDecimal> dailyReturns) {
    double mean = dailyReturns.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .average()
        .orElseThrow();

    double variance = dailyReturns.stream()
        .mapToDouble(value -> {
          double delta = value.doubleValue() - mean;
          return delta * delta;
        })
        .average()
        .orElseThrow();

    return BigDecimal.valueOf(Math.sqrt(variance)).setScale(SCALE, RoundingMode.HALF_UP);
  }

  /**
   * Computes a simplified Sharpe ratio using zero risk-free return.
   */
  private static BigDecimal calculateSharpeRatio(
      List<BigDecimal> dailyReturns,
      BigDecimal volatility) {
    BigDecimal averageReturn = BigDecimal.valueOf(dailyReturns.stream()
        .mapToDouble(BigDecimal::doubleValue)
        .average()
        .orElseThrow());
    return averageReturn.divide(volatility, SCALE, RoundingMode.HALF_UP);
  }
}
