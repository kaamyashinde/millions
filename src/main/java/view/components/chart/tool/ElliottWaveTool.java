package view.components.chart.tool;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import view.theme.ThemeStyles;

/**
 * Nine-click chart tool that labels and draws Elliott Wave impulse and corrective segments.
 *
 * <p>The user clicks nine points in sequence: origin + eight wave turning points. After the ninth
 * click all eight segments are drawn at once, impulse waves (1–5) in blue and corrective waves
 * (A–C) in red. The legend is enabled to show wave labels.
 *
 * <p>AI assistance note: Codex was used as inspiration when planning the visual analysis structure
 * for this tool; the final implementation was reviewed and adapted by the group.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 2026-03-30
 */
public class ElliottWaveTool extends AbstractChartTool {

  /**
   * Nine sequential status prompts (one per expected click) that guide the user from the wave
   * origin through each impulse and corrective turning point.
   */
  private static final String[] STATUS_MESSAGES = {
    "Click wave origin (1/9)",
    "Click Wave 1 peak (2/9)",
    "Click Wave 2 trough (3/9)",
    "Click Wave 3 peak (4/9)",
    "Click Wave 4 trough (5/9)",
    "Click Wave 5 peak (6/9)",
    "Click Wave A trough (7/9)",
    "Click Wave B peak (8/9)",
    "Click Wave C trough (9/9)"
  };

  /**
   * Eight wave labels — impulse waves 1–5 followed by corrective waves A–C — used as series names
   * in the chart legend.
   */
  private static final String[] WAVE_NAMES = {
    "Wave 1", "Wave 2", "Wave 3", "Wave 4", "Wave 5", "Wave A", "Wave B", "Wave C"
  };

  /** Collected click points; each entry is {@code [dayIndex, price]}. */
  private final List<double[]> points = new ArrayList<>();

  /** Whether the currently drawn overlay is an automatic preview. */
  private boolean previewDrawn = false;

  /**
   * Creates an Elliott Wave chart tool.
   */
  public ElliottWaveTool() {
  }

  /**
   * Returns the display name of this tool.
   *
   * @return {@code "Elliott Wave"}
   */
  @Override
  public String getName() {
    return "Elliott Wave";
  }

  /**
   * Marks the tool as active, clears any previously collected click points, and displays the
   * first status prompt asking the user to click the wave origin.
   *
   * @param chart the chart on which the tool will operate
   */
  @Override
  public void onActivate(LineChart<Number, Number> chart) {
    active.set(true);
    points.clear();
    previewDrawn = false;
    List<double[]> previewPoints = previewPoints(chart);
    if (previewPoints.size() < 2) {
      status.set("Elliott Wave needs at least 2 price points");
      return;
    }
    drawSegments(chart, previewPoints);
    previewDrawn = true;
    chart.setLegendVisible(true);
    status.set("Click wave origin to redraw Elliott Wave");
  }

  /**
   * Accumulates click points up to a maximum of nine. Each click advances the status prompt to
   * the next wave turning point. On the ninth click, all eight wave segments are drawn and the
   * chart legend is enabled.
   *
   * @param chart    the target chart
   * @param price    the y-axis price value at the click position
   * @param dayIndex the x-axis day index at the click position
   */
  @Override
  public void onChartClick(LineChart<Number, Number> chart, double price, int dayIndex) {
    if (points.size() >= 9) {
      return;
    }

    if (previewDrawn) {
      clearAll(chart);
      chart.setLegendVisible(false);
      previewDrawn = false;
    }

    points.add(new double[] {dayIndex, price});
    int clickCount = points.size();

    if (clickCount < 9) {
      status.set(STATUS_MESSAGES[clickCount]);
    } else {
      drawSegments(chart, points);
      chart.setLegendVisible(true);
      status.set("");
    }
  }

  /**
   * Removes all Elliott Wave series from the chart, hides the legend, and resets the collected
   * points list and the active flag to their default values.
   *
   * @param chart the chart from which series are removed
   */
  @Override
  public void onDeactivate(LineChart<Number, Number> chart) {
    clearAll(chart);
    chart.setLegendVisible(false);
    points.clear();
    previewDrawn = false;
    active.set(false);
    status.set("");
  }

  /**
   * Iterates over consecutive point-to-point segments, assigns
   * {@link #IMPULSE_COLOR} to the first five (waves 1–5) and {@link #CORRECTIVE_COLOR} to the
   * remaining three (waves A–C), and adds and styles each series via {@code Platform.runLater}.
   *
   * @param chart the chart on which the wave segments are drawn
   * @param segmentPoints ordered points to connect
   */
  private void drawSegments(LineChart<Number, Number> chart, List<double[]> segmentPoints) {
    int segmentCount = Math.min(segmentPoints.size() - 1, WAVE_NAMES.length);
    for (int i = 0; i < segmentCount; i++) {
      double[] from = segmentPoints.get(i);
      double[] to = segmentPoints.get(i + 1);

      XYChart.Series<Number, Number> series = new XYChart.Series<>();
      series.setName(WAVE_NAMES[i]);
      series.getData().add(new XYChart.Data<>(from[0], from[1]));
      series.getData().add(new XYChart.Data<>(to[0], to[1]));

      addSeries(chart, series);
      applyStyleClasses(series, "chart-overlay-solid-thick");
    }
  }

  /**
   * Picks up to nine evenly spaced price points for the automatic Elliott Wave preview.
   *
   * @param chart the chart on which the wave segments are drawn
   * @return ordered preview points
   */
  private static List<double[]> previewPoints(LineChart<Number, Number> chart) {
    List<XYChart.Data<Number, Number>> data = chart.getData().getFirst().getData();
    if (data.size() <= 9) {
      return data.stream()
          .map(
              point ->
                  new double[] {
                    point.getXValue().doubleValue(), point.getYValue().doubleValue()
                  })
          .toList();
    }
    List<double[]> selected = new ArrayList<>();
    int maxIndex = data.size() - 1;
    for (int i = 0; i < 9; i++) {
      int index = (int) Math.round(i * maxIndex / 8.0);
      XYChart.Data<Number, Number> point = data.get(index);
      selected.add(new double[] {point.getXValue().doubleValue(), point.getYValue().doubleValue()});
    }
    return selected;
  }
}
