package model.analysis.performance;


/**
 * Availability state for one displayed performance metric.
 */
public enum MetricStatus {
  AVAILABLE,
  NO_TRADES,
  INSUFFICIENT_HISTORY,
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
