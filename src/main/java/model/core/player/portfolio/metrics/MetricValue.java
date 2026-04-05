package model.core.player.portfolio.metrics;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;

/**
 * Holds one calculated metric together with its availability state.
 */
public record MetricValue(BigDecimal value, MetricStatus status) {

  /**
   * Creates one metric value.
   *
   * @param value  numeric value when available, otherwise {@code null}
   * @param status availability state for the metric
   */
  public MetricValue {
    checkNotNull(status, "Metric status");
    if (status.isAvailable()) {
      checkNotNull(value, "Metric value");
    }
  }

  /**
   * Creates an available metric value.
   *
   * @param value numeric metric value
   * @return metric marked as available
   */
  public static MetricValue available(BigDecimal value) {
    checkNotNull(value, "Metric value");
    return new MetricValue(value, MetricStatus.AVAILABLE);
  }

  /**
   * Creates an unavailable metric value.
   *
   * @param status reason the metric is unavailable
   * @return metric marked as unavailable
   */
  public static MetricValue unavailable(MetricStatus status) {
    checkNotNull(status, "Metric status");
    if (status.isAvailable()) {
      throw new IllegalArgumentException("Unavailable metric requires a non-available status.");
    }
    return new MetricValue(null, status);
  }

  /**
   * Returns whether this metric has a numeric value.
   *
   * @return {@code true} when a number can be displayed
   */
  public boolean isAvailable() {
    return status.isAvailable();
  }
}
