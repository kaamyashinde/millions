package view.components.chart;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import view.components.chart.tool.ChartTool;

/**
 * Toolbar that wires {@link ChartTool} strategies to a single-select analysis control.
 *
 * <p>The explicit {@link ChartToolSelection#NONE} option deactivates all tools and leaves the
 * chart without analysis overlays. Selecting any other option deactivates the current tool before
 * activating the newly selected one.
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
    this(tools, chart, ChartToolSelection.NONE, selection -> {});
  }

  /**
   * Builds the toolbar for the given tools and chart.
   *
   * @param tools list of chart tools to expose as toggle buttons
   * @param chart the chart on which the tools operate
   * @param selectedSelection selector option to activate initially
   * @param onSelectionChanged callback invoked when the user changes the selector
   */
  public AnalysisToolbar(
      List<ChartTool> tools,
      LineChart<Number, Number> chart,
      ChartToolSelection selectedSelection,
      Consumer<ChartToolSelection> onSelectionChanged) {
    super(12);

    Map<ChartToolSelection, ChartTool> toolsBySelection = indexTools(tools);
    ToggleGroup group = new ToggleGroup();
    ToggleButton noneButton = createButton(ChartToolSelection.NONE, group);

    Label selectorLabel = new Label("Analysis:");
    selectorLabel.getStyleClass().add("analysis-label");
    getStyleClass().add("analysis-toolbar");
    getChildren().addAll(selectorLabel, noneButton);

    Stream.of(ChartToolSelection.values())
        .filter(selection -> selection != ChartToolSelection.NONE)
        .filter(toolsBySelection::containsKey)
        .map(selection -> createButton(selection, group))
        .forEach(getChildren()::add);

    Observable[] deps =
        tools.stream().map(t -> (Observable) t.statusProperty()).toArray(Observable[]::new);

    Label statusLabel = new Label();
    statusLabel.getStyleClass().add("analysis-status");
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

    ChartToolSelection initialSelection =
        toolsBySelection.containsKey(selectedSelection) ? selectedSelection : ChartToolSelection.NONE;
    group.getToggles().stream()
        .filter(toggle -> toggle.getUserData() == initialSelection)
        .findFirst()
        .orElse(noneButton)
        .setSelected(true);
    activateSelection(initialSelection, toolsBySelection, chart);

    group
        .selectedToggleProperty()
        .addListener(
            (obs, oldToggle, newToggle) -> {
              if (newToggle == null) {
                (oldToggle != null ? oldToggle : noneButton).setSelected(true);
                return;
              }

              ChartToolSelection selection = (ChartToolSelection) newToggle.getUserData();
              activateSelection(selection, toolsBySelection, chart);
              onSelectionChanged.accept(selection);
            });

    getChildren().add(statusLabel);
  }

  private static Map<ChartToolSelection, ChartTool> indexTools(List<ChartTool> tools) {
    Map<ChartToolSelection, ChartTool> indexed = new EnumMap<>(ChartToolSelection.class);
    tools.forEach(
        tool -> ChartToolSelection.fromToolName(tool.getName())
            .ifPresent(selection -> indexed.put(selection, tool)));
    return indexed;
  }

  private static ToggleButton createButton(ChartToolSelection selection, ToggleGroup group) {
    ToggleButton button = new ToggleButton(selection.getLabel());
    button.setToggleGroup(group);
    button.setUserData(selection);
    button.setFocusTraversable(false);
    button.getStyleClass().add("analysis-button");
    return button;
  }

  private static void activateSelection(
      ChartToolSelection selection,
      Map<ChartToolSelection, ChartTool> toolsBySelection,
      LineChart<Number, Number> chart) {
    Stream.of(ChartToolSelection.values())
        .map(toolsBySelection::get)
        .filter(tool -> tool != null && tool.activeProperty().get())
        .forEach(tool -> tool.onDeactivate(chart));

    ChartTool selectedTool = toolsBySelection.get(selection);
    if (selectedTool != null) {
      selectedTool.onActivate(chart);
    }
  }
}
