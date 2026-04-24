package old_view.components.chart.tool;

import java.math.BigDecimal;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import model.analysis.FibonacciRetracement;

/**
 * Two-click chart tool that draws seven horizontal Fibonacci retracement lines.
 *
 * <p>Workflow: activate → click LOW pivot → click HIGH pivot → seven dashed horizontal lines
 * appear spanning the full day range. Deactivating removes all lines and hides the legend.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public class FibonacciTool extends AbstractChartTool {

  /**
   * Two-state interaction machine: {@code IDLE} means no interaction is in progress, and
   * {@code AWAITING_SECOND} means the first pivot has been captured and the tool is waiting for
   * the second pivot click.
   */
  private enum State {
    IDLE,
    AWAITING_SECOND
  }

  /**
   * Seven hex colour strings mapped to Fibonacci retracement levels in order (0 % through 100 %).
   */
  private static final String[] COLORS = {
    "#ffffff", "#ffd700", "#ff6b6b", "#ff8c00", "#00bfff", "#98fb98", "#dddddd"
  };

  /** Current interaction state of the two-click workflow. */
  private State state = State.IDLE;

  /** Price of the first pivot click; {@code null} until the first click is registered. */
  private Double firstPrice = null;

  /**
   * Returns the display name of this tool.
   *
   * @return {@code "Fibonacci"}
   */
  @Override
  public String getName() {
    return "Fibonacci";
  }

  /**
   * Resets {@code state} to {@code AWAITING_SECOND}, clears any previous {@code firstPrice},
   * marks the tool as active, and prompts the user to select the LOW pivot.
   *
   * @param chart the chart on which the tool will operate
   */
  @Override
  public void onActivate(LineChart<Number, Number> chart) {
    state = State.AWAITING_SECOND;
    firstPrice = null;
    active.set(true);
    status.set("Select LOW pivot");
  }

  /**
   * Handles the two-click retracement workflow. The first click captures the LOW or HIGH pivot
   * price; the second click computes the Fibonacci levels from the two pivots and draws seven
   * dashed horizontal series across the full day range of the chart.
   *
   * @param chart    the target chart
   * @param price    the y-axis price value at the click position
   * @param dayIndex the x-axis day index at the click position
   */
  @Override
  public void onChartClick(LineChart<Number, Number> chart, double price, int dayIndex) {
    if (state != State.AWAITING_SECOND) {
      return;
    }

    if (firstPrice == null) {
      firstPrice = price;
      status.set("Select HIGH pivot");
    } else {
      double low = Math.min(firstPrice, price);
      double high = Math.max(firstPrice, price);

      int totalDays = chart.getData().get(0).getData().size();
      List<FibonacciRetracement.Level> levels =
          FibonacciRetracement.compute(BigDecimal.valueOf(high), BigDecimal.valueOf(low));

      for (int i = 0; i < levels.size(); i++) {
        FibonacciRetracement.Level level = levels.get(i);
        double levelPrice = level.price().doubleValue();
        String seriesName = level.name() + " \u2014 $" + level.price().toPlainString();

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        series.getData().add(new XYChart.Data<>(1, levelPrice));
        series.getData().add(new XYChart.Data<>(totalDays, levelPrice));

        final String color = COLORS[i];
        addSeries(chart, series);

        Platform.runLater(
            () -> {
              if (series.getNode() != null) {
                series
                    .getNode()
                    .setStyle(
                        "-fx-stroke: "
                            + color
                            + "; "
                            + "-fx-stroke-width: 1.5; "
                            + "-fx-stroke-dash-array: 6 4;");
              }
            });
      }

      chart.setLegendVisible(true);
      state = State.IDLE;
      status.set("");
    }
  }

  /**
   * Removes all Fibonacci series from the chart, hides the legend, and resets internal state
   * ({@code state}, {@code firstPrice}, and the active flag) to their default values.
   *
   * @param chart the chart from which series are removed
   */
  @Override
  public void onDeactivate(LineChart<Number, Number> chart) {
    clearAll(chart);
    chart.setLegendVisible(false);
    state = State.IDLE;
    firstPrice = null;
    active.set(false);
    status.set("");
  }
}
