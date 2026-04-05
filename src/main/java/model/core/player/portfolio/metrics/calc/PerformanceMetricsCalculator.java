package model.core.player.portfolio.metrics.calc;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import model.core.player.portfolio.metrics.MetricStatus;
import model.core.player.portfolio.metrics.MetricValue;
import model.core.player.portfolio.metrics.PerformanceMetrics;

/**
 * Orchestrates return, volatility, and Sharpe calculations from a daily value series.
 */
public class PerformanceMetricsCalculator {

  private final ReturnSeriesCalculator returnSeriesCalculator;
  private final VolatilityCalculator volatilityCalculator;
  private final SharpeRatioCalculator sharpeRatioCalculator;

  /**
   * Creates a calculator with default helper calculators.
   */
  public PerformanceMetricsCalculator() {
    this(new ReturnSeriesCalculator(), new VolatilityCalculator(), new SharpeRatioCalculator());
  }

  /**
   * Creates a calculator with injected helpers.
   *
   * @param returnSeriesCalculator helper used to derive returns
   * @param volatilityCalculator   helper used to derive volatility
   * @param sharpeRatioCalculator  helper used to derive the Sharpe ratio
   */
  public PerformanceMetricsCalculator(
      ReturnSeriesCalculator returnSeriesCalculator,
      VolatilityCalculator volatilityCalculator,
      SharpeRatioCalculator sharpeRatioCalculator) {
    checkNotNull(returnSeriesCalculator, "Return series calculator");
    checkNotNull(volatilityCalculator, "Volatility calculator");
    checkNotNull(sharpeRatioCalculator, "Sharpe ratio calculator");
    this.returnSeriesCalculator = returnSeriesCalculator;
    this.volatilityCalculator = volatilityCalculator;
    this.sharpeRatioCalculator = sharpeRatioCalculator;
  }

  /**
   * Calculates metrics from ordered daily values.
   *
   * @param dailyValues ordered daily values, oldest to newest
   * @return available and unavailable metrics according to the amount of history present
   */
  public PerformanceMetrics calculateFromDailyValues(List<BigDecimal> dailyValues) {
    checkNotNull(dailyValues, "Daily values");
    if (dailyValues.size() < 2) {
      return PerformanceMetrics.unavailable(MetricStatus.INSUFFICIENT_HISTORY);
    }

    List<BigDecimal> dailyReturns = returnSeriesCalculator.calculateDailyReturns(dailyValues);
    MetricValue returnPercent =
        MetricValue.available(returnSeriesCalculator.calculateTotalReturn(dailyValues));

    if (dailyReturns.size() < 2) {
      return new PerformanceMetrics(
          returnPercent,
          MetricValue.unavailable(MetricStatus.INSUFFICIENT_HISTORY),
          MetricValue.unavailable(MetricStatus.INSUFFICIENT_HISTORY));
    }

    BigDecimal volatility = volatilityCalculator.calculate(dailyReturns);
    MetricValue volatilityMetric = MetricValue.available(volatility);
    MetricValue sharpeMetric = volatility.signum() == 0
        ? MetricValue.unavailable(MetricStatus.ZERO_VOLATILITY)
        : MetricValue.available(sharpeRatioCalculator.calculate(dailyReturns, volatility));

    return new PerformanceMetrics(returnPercent, volatilityMetric, sharpeMetric);
  }
}
