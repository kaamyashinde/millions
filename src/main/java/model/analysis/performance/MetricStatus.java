package model.analysis.performance;


/**
 * Availability state for one displayed performance metric.
 */
public enum MetricStatus {
  /** Metric has a numeric value. */
  AVAILABLE,
  /** Metric cannot be computed because the player has not traded. */
  NO_TRADES,
  /** Metric cannot be computed because there are not enough observations. */
  INSUFFICIENT_HISTORY,
  /** Metric cannot be computed because volatility is zero. */
  ZERO_VOLATILITY;

  /**
   * Returns whether this status carries a usable numeric value.
   *
   * @return {@code true} when the metric can be displayed as a number
   */
  public boolean isAvailable() {
    return this == AVAILABLE;
  }
}
