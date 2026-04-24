package old_view.components.chart.tool;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.LineChart;

/**
 * Strategy interface for chart analysis tools that can overlay visual annotations on a
 * {@link LineChart}.
 *
 * <p>Each concrete tool is an interchangeable strategy. The chart context holds a
 * {@code List<ChartTool>} and delegates mouse events to all active tools, following the Strategy
 * pattern.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public interface ChartTool {

  /**
   * Returns the display name shown on the toolbar toggle button.
   *
   * @return human-readable tool name
   */
  String getName();

  /**
   * Called when the user activates this tool. Implementations should initialise state and set the
   * first status prompt.
   *
   * @param chart the chart on which overlays will be drawn
   */
  void onActivate(LineChart<Number, Number> chart);

  /**
   * Called when the user deactivates this tool. Implementations must remove all owned series from
   * the chart and reset state.
   *
   * @param chart the chart from which overlays will be removed
   */
  void onDeactivate(LineChart<Number, Number> chart);

  /**
   * Called for every mouse click on the chart while this tool is active.
   *
   * @param chart    the chart that received the click
   * @param price    the Y-axis value (price) at the click location
   * @param dayIndex the X-axis value (day index, 1-based) nearest to the click location
   */
  void onChartClick(LineChart<Number, Number> chart, double price, int dayIndex);

  /**
   * Observable flag that reflects whether this tool is currently active.
   *
   * @return the active property
   */
  BooleanProperty activeProperty();

  /**
   * Observable status message updated during multi-click workflows (e.g. "Select LOW pivot").
   *
   * @return the status property
   */
  StringProperty statusProperty();
}
