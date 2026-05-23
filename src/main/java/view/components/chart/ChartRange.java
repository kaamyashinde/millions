package view.components.chart;

import java.util.OptionalInt;

/**
 * Time ranges available for stock chart history views.
 */
public enum ChartRange {
  ONE_DAY("1D", 1),
  FIVE_DAYS("5D", 5),
  ONE_MONTH("1M", 30),
  THREE_MONTHS("3M", 90),
  SIX_MONTHS("6M", 180),
  YEAR_TO_DATE("YTD"),
  ONE_YEAR("1Y", 365),
  FIVE_YEARS("5Y", 1825),
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
   * @return short label shown in the chart range selector
   */
  public String getLabel() {
    return label;
  }

  /**
   * @return number of recent simulated trading days to show, or empty for unlimited ranges
   */
  public OptionalInt getDayWindow() {
    return dayWindow == null ? OptionalInt.empty() : OptionalInt.of(dayWindow);
  }
}
