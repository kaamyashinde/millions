package model.analysis;

import static model.utils.Validator.checkNotNull;

/**
 * Groups the displayed metrics for one portfolio or benchmark.
 */
public record PerformanceMetrics(
    MetricValue returnPercent,
    MetricValue volatility,
    MetricValue sharpeRatio) {

  /**
   * Creates one metrics bundle.
   *
   * @param returnPercent total return metric
   * @param volatility simplified volatility metric
   * @param sharpeRatio Sharpe ratio metric
   */
  public PerformanceMetrics {
    checkNotNull(returnPercent, "Return percent");
    checkNotNull(volatility, "Volatility");
    checkNotNull(sharpeRatio, "Sharpe ratio");
  }

  /**
   * Creates a metrics bundle where every metric shares the same unavailable status.
   *
   * @param status unavailable status to apply to all metrics
   * @return unavailable metrics bundle
   */
  public static PerformanceMetrics unavailable(MetricStatus status) {
    return new PerformanceMetrics(
        MetricValue.unavailable(status),
        MetricValue.unavailable(status),
        MetricValue.unavailable(status));
  }
}
