package old_view.components.chart.tool;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

/**
 * Template-method base class for chart analysis tools.
 *
 * <p>Manages the shared lifecycle skeleton — series ownership, active flag, and status text —
 * allowing concrete subclasses to focus solely on their domain logic ({@code onActivate},
 * {@code onDeactivate}, {@code onChartClick}).
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public abstract class AbstractChartTool implements ChartTool {

  /** Series added by this tool; tracked so they can be removed cleanly on deactivation. */
  protected final List<XYChart.Series<Number, Number>> ownedSeries = new ArrayList<>();

  /** Whether this tool is currently active. */
  protected final SimpleBooleanProperty active = new SimpleBooleanProperty(false);

  /** Current user-facing status prompt (e.g. "Select LOW pivot"). */
  protected final SimpleStringProperty status = new SimpleStringProperty("");

  /**
   * Returns the observable boolean flag that tracks whether this tool is currently activated.
   *
   * @return the active property
   */
  @Override
  public BooleanProperty activeProperty() {
    return active;
  }

  /**
   * Returns the observable string property containing the current user-facing status prompt.
   *
   * @return the status property
   */
  @Override
  public StringProperty statusProperty() {
    return status;
  }

  /**
   * Adds {@code series} to the chart and registers it for later cleanup.
   *
   * @param chart  the target chart
   * @param series the series to add
   */
  protected void addSeries(LineChart<Number, Number> chart, XYChart.Series<Number, Number> series) {
    chart.getData().add(series);
    ownedSeries.add(series);
  }

  /**
   * Removes all series owned by this tool from the chart and clears the tracking list.
   *
   * @param chart the chart from which series are removed
   */
  protected void clearAll(LineChart<Number, Number> chart) {
    chart.getData().removeAll(ownedSeries);
    ownedSeries.clear();
  }
}
