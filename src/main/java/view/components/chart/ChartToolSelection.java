package view.components.chart;

import java.util.Arrays;
import java.util.Optional;

/**
 * Single-select options available in the stock chart analysis toolbar.
 */
public enum ChartToolSelection {
  NONE("None"),
  FIBONACCI("Fibonacci"),
  ELLIOTT_WAVE("Elliott Wave"),
  MOON_PHASES("Moon Phases");

  private final String label;

  ChartToolSelection(String label) {
    this.label = label;
  }

  /**
   * Returns the user-facing label shown in the analysis selector.
   *
   * @return display label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Finds the selector option whose label matches a chart tool name.
   *
   * @param toolName name returned by a chart tool
   * @return matching selector option, or empty when the name is unknown
   */
  public static Optional<ChartToolSelection> fromToolName(String toolName) {
    return Arrays.stream(values())
        .filter(selection -> selection != NONE)
        .filter(selection -> selection.label.equals(toolName))
        .findFirst();
  }
}
