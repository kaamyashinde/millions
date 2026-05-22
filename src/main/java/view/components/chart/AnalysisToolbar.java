package view.components.chart;

import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import view.components.chart.tool.ChartTool;

/**
 * Toolbar that wires a list of {@link ChartTool} strategies to toggle buttons and a combined status
 * label.
 *
 * <p>Selecting a button calls {@link ChartTool#onActivate}; deselecting calls
 * {@link ChartTool#onDeactivate}. The status label aggregates all non-empty tool status messages,
 * separated by {@code " | "}.
 *
 * @author kaamyashinde
 * @version 0.1.0
 * @since 30-03-2026
 */
public class AnalysisToolbar extends HBox {

  /**
   * Builds the toolbar for the given tools and chart.
   *
   * @param tools list of chart tools to expose as toggle buttons
   * @param chart the chart on which the tools operate
   */
  public AnalysisToolbar(List<ChartTool> tools, LineChart<Number, Number> chart) {
    super(12);

    for (ChartTool tool : tools) {
      ToggleButton btn = new ToggleButton(tool.getName());
      btn.setStyle(
          "-fx-background-color: #111827;"
              + "-fx-text-fill: #CBD5E1;"
              + "-fx-border-color: #334155;"
              + "-fx-border-radius: 4;"
              + "-fx-background-radius: 4;"
              + "-fx-cursor: hand;");

      btn.selectedProperty()
          .addListener(
              (obs, oldVal, newVal) -> {
                if (newVal) {
                  tool.onActivate(chart);
                } else {
                  tool.onDeactivate(chart);
                }
              });

      // Keep button in sync if active state is changed programmatically
      tool.activeProperty()
          .addListener((obs, oldVal, newVal) -> btn.setSelected(newVal));

      getChildren().add(btn);
    }

    Observable[] deps =
        tools.stream().map(t -> (Observable) t.statusProperty()).toArray(Observable[]::new);

    Label statusLabel = new Label();
    statusLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
    statusLabel
        .textProperty()
        .bind(
            Bindings.createStringBinding(
                () ->
                    tools.stream()
                        .map(t -> t.statusProperty().get())
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(" | ")),
                deps));

    getChildren().add(statusLabel);
  }
}
