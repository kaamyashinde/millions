package view.components.chart;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;

/**
 * Applies CSS style classes to chart series nodes after JavaFX creates them.
 */
final class ChartSeriesStyles {

  private ChartSeriesStyles() {}

  /**
   * Adds style classes to the series line node on the JavaFX application thread.
   *
   * @param series  target series
   * @param classes CSS class names defined in {@code base.css}
   */
  static void applyStyleClasses(XYChart.Series<Number, Number> series, String... classes) {
    Platform.runLater(
        () -> {
          if (series.getNode() != null) {
            series.getNode().getStyleClass().addAll(classes);
          }
        });
  }
}
