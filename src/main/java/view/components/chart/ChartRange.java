package view.components.chart;

import java.util.OptionalInt;

/**
 * Time ranges available for stock chart history views.
 */
public enum ChartRange {
  /** Last simulated trading day. */
  ONE_DAY("1D", 1),
  /** Last five simulated trading days. */
  FIVE_DAYS("5D", 5),
  /** Last thirty simulated trading days. */
  ONE_MONTH("1M", 30),
  /** Last ninety simulated trading days. */
  THREE_MONTHS("3M", 90),
  /** Last 180 simulated trading days. */
  SIX_MONTHS("6M", 180),
  /** Current simulated year to date. */
  YEAR_TO_DATE("YTD"),
  /** Last 365 simulated trading days. */
  ONE_YEAR("1Y", 365),
  /** Last five simulated years. */
  FIVE_YEARS("5Y", 1825),
  /** Full available price history. */
  ALL("All");

  private final String label;
  private final Integer dayWindow;

  ChartRange(String label) {
    this(label, null);
  }

  ChartRange(String label, Integer dayWindow) {
    this.label = label;
    this.dayWindow = dayWindow;
  }

  /**
   * Returns the compact selector label.
   *
   * @return short label shown in the chart range selector
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the bounded day window for this range.
   *
   * @return number of recent simulated trading days to show, or empty for unlimited ranges
   */
  public OptionalInt getDayWindow() {
    return dayWindow == null ? OptionalInt.empty() : OptionalInt.of(dayWindow);
  }
}
