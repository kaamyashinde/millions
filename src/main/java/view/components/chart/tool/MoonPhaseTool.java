package view.components.chart.tool;

import java.time.LocalDate;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import model.analysis.MoonPhaseCalculator;

/**
 * Chart tool that auto-draws vertical markers for new-moon and full-moon days.
 *
 * <p>Activating the tool immediately calculates all phase events for the loaded price series and
 * draws a dashed vertical line at each qualifying day. No clicks are required.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public class MoonPhaseTool extends AbstractChartTool {

  /** Grey hex colour used for vertical dashed lines marking new-moon days. */
  private static final String NEW_MOON_COLOR = "#aaaaaa";

  /** Gold hex colour used for vertical dashed lines marking full-moon days. */
  private static final String FULL_MOON_COLOR = "#ffd700";

  /** Calendar date corresponding to day index 1 on the chart's x-axis. */
  private final LocalDate startDate;

  /**
   * Creates a {@code MoonPhaseTool} anchored to the given simulation start date.
   *
   * @param startDate the calendar date corresponding to day index 1 on the chart
   */
  public MoonPhaseTool(LocalDate startDate) {
    this.startDate = startDate;
  }

  /**
   * Returns the display name of this tool.
   *
   * @return {@code "Moon Phases"}
   */
  @Override
  public String getName() {
    return "Moon Phases";
  }

  /**
   * Computes all moon phase events for the loaded price series via {@link MoonPhaseCalculator},
   * reads the y-axis bounds, and draws one dashed vertical series per phase event — grey for
   * new moons and gold for full moons.
   *
   * @param chart the chart on which the moon phase markers are drawn
   */
  @Override
  public void onActivate(LineChart<Number, Number> chart) {
    active.set(true);

    int totalDays = chart.getData().get(0).getData().size();
    Map<Integer, MoonPhaseCalculator.Phase> phases =
        MoonPhaseCalculator.compute(startDate, totalDays);

    NumberAxis yAxis = (NumberAxis) chart.getYAxis();
    double low = yAxis.getLowerBound();
    double high = yAxis.getUpperBound();

    phases.forEach(
        (day, phase) -> {
          boolean isNewMoon = phase == MoonPhaseCalculator.Phase.NEW_MOON;
          String seriesName = (isNewMoon ? "New Moon" : "Full Moon") + " (Day " + day + ")";
          String color = isNewMoon ? NEW_MOON_COLOR : FULL_MOON_COLOR;

          XYChart.Series<Number, Number> series = new XYChart.Series<>();
          series.setName(seriesName);
          series.getData().add(new XYChart.Data<>(day, low));
          series.getData().add(new XYChart.Data<>(day, high));

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
        });

    status.set("");
  }

  /** No-op: this tool draws automatically on activation and requires no click interaction. */
  @Override
  public void onChartClick(LineChart<Number, Number> chart, double price, int dayIndex) {
    // intentional no-op
  }

  /**
   * Removes all moon phase series owned by this tool from the chart and resets the active flag.
   *
   * @param chart the chart from which series are removed
   */
  @Override
  public void onDeactivate(LineChart<Number, Number> chart) {
    clearAll(chart);
    active.set(false);
    status.set("");
  }
}
