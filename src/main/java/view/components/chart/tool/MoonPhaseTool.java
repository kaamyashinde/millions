package view.components.chart.tool;

import java.time.LocalDate;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import model.analysis.tools.MoonPhaseCalculator;
import view.theme.ThemeStyles;

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
  private static final String NEW_MOON_COLOR = "#94A3B8";

  /** Gold hex colour used for vertical dashed lines marking full-moon days. */
  private static final String FULL_MOON_COLOR = "#F59E0B";

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

    int firstDay = chart.getData().getFirst().getData().getFirst().getXValue().intValue();
    int lastDay = chart.getData().getFirst().getData().getLast().getXValue().intValue();
    Map<Integer, MoonPhaseCalculator.Phase> phases =
        MoonPhaseCalculator.compute(startDate, lastDay);

    double low = visibleLow(chart);
    double high = visibleHigh(chart);
    if (Double.compare(low, high) == 0) {
      double padding = Math.max(Math.abs(low) * 0.01, 1.0);
      low -= padding;
      high += padding;
    }
    final double markerLow = low;
    final double markerHigh = high;

    phases.entrySet().stream()
        .filter(entry -> entry.getKey() >= firstDay && entry.getKey() <= lastDay)
        .forEach(
            entry -> {
              int day = entry.getKey();
              MoonPhaseCalculator.Phase phase = entry.getValue();
              boolean isNewMoon = phase == MoonPhaseCalculator.Phase.NEW_MOON;
              String seriesName = (isNewMoon ? "New Moon" : "Full Moon") + " (Day " + day + ")";
              String color = isNewMoon ? NEW_MOON_COLOR : FULL_MOON_COLOR;

              XYChart.Series<Number, Number> series = new XYChart.Series<>();
              series.setName(seriesName);
              series.getData().add(new XYChart.Data<>(day, markerLow));
              series.getData().add(new XYChart.Data<>(day, markerHigh));

              addSeries(chart, series);

              Platform.runLater(
                  () -> {
                    if (series.getNode() != null) {
                      ThemeStyles.addStyleClasses(series.getNode(), "chart-overlay-line-dashed");
                      series.getNode().setStyle("-fx-stroke: " + color + ";");
                    }
                  });
            });

    status.set(ownedSeries.isEmpty() ? "No moon phases in this range" : "");
  }

  private static double visibleLow(LineChart<Number, Number> chart) {
    return chart.getData().getFirst().getData().stream()
        .mapToDouble(data -> data.getYValue().doubleValue())
        .min()
        .orElse(0.0);
  }

  private static double visibleHigh(LineChart<Number, Number> chart) {
    return chart.getData().getFirst().getData().stream()
        .mapToDouble(data -> data.getYValue().doubleValue())
        .max()
        .orElse(0.0);
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
