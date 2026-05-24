package model.analysis.performance;


import static util.Validator.checkNotNull;

/**
 * Groups the displayed metrics for one portfolio or benchmark.
 *
 * <p>Each metric is represented by a {@link MetricValue} so unavailable states can be rendered
 * without throwing away the rest of the comparison.
 *
 * @param returnPercent total return metric
 * @param volatility simplified volatility metric
 * @param sharpeRatio Sharpe ratio metric
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 2026-04-04
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
