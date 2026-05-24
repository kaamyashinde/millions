package view.components.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import model.core.asset.Stock;
import view.components.chart.tool.ChartTool;

/**
 * A {@link LineChart} that visualises the full price history of a {@link Stock}.
 *
 * <p>Each data point maps a 1-based day index (X) to the closing price on that day (Y).
 * Analysis tools can be registered via {@link #registerTool(ChartTool)}; the chart routes every
 * mouse click to all currently active tools.
 *
 * @author kaamyashinde
 * @version 0.6.0
 * @since 29-03-2026
 */
public class StockChart extends LineChart<Number, Number> {

  private static final double AXIS_PADDING_RATIO = 0.10;
  private static final int TARGET_Y_AXIS_TICKS = 5;
  private static final double FALLBACK_AXIS_PADDING = 1.0;

  private final List<ChartTool> tools = new ArrayList<>();
  private final Map<Integer, XYChart.Series<Number, Number>> eventMarkers = new HashMap<>();

  /**
   * Creates a chart pre-populated with the price history of {@code stock}.
   *
   * @param stock the stock whose historical prices are displayed
   */
  public StockChart(Stock stock) {
    this(stock, ChartRange.ALL);
  }

  /**
   * Creates a chart pre-populated with the selected range of {@code stock}'s price history.
   *
   * @param stock the stock whose historical prices are displayed
   * @param range selected chart range
   */
  public StockChart(Stock stock, ChartRange range) {
    super(buildXAxis(), buildYAxis());

    List<BigDecimal> prices = stock.getHistoricalPrices();
    int startIndex = rangeStartIndex(prices, range);
    List<BigDecimal> visiblePrices = prices.subList(startIndex, prices.size());

    setTitle(stock.getSymbol() + " \u2014 " + stock.getCompany());
    setCreateSymbols(visiblePrices.size() == 1);
    setLegendVisible(false);
    setAnimated(false);

    configureYAxis((NumberAxis) getYAxis(), visiblePrices);
    getData().add(buildSeries(visiblePrices, startIndex));

    setOnMouseClicked(
        event -> {
          NumberAxis xAxis = (NumberAxis) getXAxis();
          NumberAxis yAxis = (NumberAxis) getYAxis();
          double price =
              yAxis
                  .getValueForDisplay(
                      yAxis.sceneToLocal(event.getSceneX(), event.getSceneY()).getY())
                  .doubleValue();
          int dayIndex =
              (int)
                  Math.round(
                      xAxis
                          .getValueForDisplay(
                              xAxis.sceneToLocal(event.getSceneX(), event.getSceneY()).getX())
                          .doubleValue());
          List<XYChart.Data<Number, Number>> visibleData = getData().getFirst().getData();
          if (visibleData.isEmpty()) {
            return;
          }
          int firstDay = visibleData.getFirst().getXValue().intValue();
          int lastDay = visibleData.getLast().getXValue().intValue();
          dayIndex = Math.max(firstDay, Math.min(lastDay, dayIndex));
          final int clampedDay = dayIndex;
          tools.forEach(
              t -> {
                if (t.activeProperty().get()) {
                  t.onChartClick(this, price, clampedDay);
                }
              });
        });
  }

  /**
   * Registers an analysis tool with this chart. Registered tools receive mouse-click events when
   * active.
   *
   * @param tool the tool to register
   */
  public void registerTool(ChartTool tool) {
    tools.add(tool);
  }

  /**
   * Returns an unmodifiable view of the registered tools, suitable for building a toolbar.
   *
   * @return unmodifiable list of registered {@link ChartTool} instances
   */
  public List<ChartTool> getTools() {
    return Collections.unmodifiableList(tools);
  }

  /**
   * Toggles a vertical marker at the given trading day. A second call for the same day removes it.
   *
   * @param day   1-based trading day on the X axis
   * @param label series name shown in the chart legend
   */
  public void toggleEventMarker(int day, String label) {
    if (eventMarkers.containsKey(day)) {
      XYChart.Series<Number, Number> existing = eventMarkers.remove(day);
      getData().remove(existing);
      if (eventMarkers.isEmpty() && tools.stream().noneMatch(t -> t.activeProperty().get())) {
        setLegendVisible(false);
      }
      return;
    }

    if (getData().isEmpty()) {
      return;
    }

    List<XYChart.Data<Number, Number>> visibleData = getData().getFirst().getData();
    if (visibleData.isEmpty()) {
      return;
    }

    int firstDay = visibleData.getFirst().getXValue().intValue();
    int lastDay = visibleData.getLast().getXValue().intValue();
    if (day < firstDay || day > lastDay) {
      return;
    }

    NumberAxis yAxis = (NumberAxis) getYAxis();
    double markerLow = yAxis.getLowerBound();
    double markerHigh = yAxis.getUpperBound();

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName(label);
    series.getData().add(new XYChart.Data<>(day, markerLow));
    series.getData().add(new XYChart.Data<>(day, markerHigh));

    getData().add(series);
    eventMarkers.put(day, series);
    setLegendVisible(true);

    ChartSeriesStyles.applyStyleClasses(series, "event-marker-line");
  }

  /**
   * Removes all event markers from this chart.
   */
  public void clearEventMarkers() {
    if (eventMarkers.isEmpty()) {
      return;
    }
    getData().removeAll(eventMarkers.values());
    eventMarkers.clear();
    if (tools.stream().noneMatch(t -> t.activeProperty().get())) {
      setLegendVisible(false);
    }
  }

  /**
   * Builds the X axis labelled "Day" with minor ticks hidden.
   *
   * @return a configured {@link NumberAxis} for the horizontal axis
   */
  private static NumberAxis buildXAxis() {
    NumberAxis axis = new NumberAxis();
    axis.setLabel("Day");
    axis.setMinorTickVisible(false);
    return axis;
  }

  /**
   * Builds the Y axis labelled "Price ($)".
   *
   * @return a configured {@link NumberAxis} for the vertical axis
   */
  private static NumberAxis buildYAxis() {
    NumberAxis axis = new NumberAxis();
    axis.setLabel("Price ($)");
    axis.setAutoRanging(false);
    return axis;
  }

  /**
   * Converts a list of closing prices into a chart series using 1-based day indices on the X axis.
   *
   * @param visiblePrices visible daily closing prices for the selected range
   * @param startIndex zero-based index of the first visible price in the full history
   * @return a {@link XYChart.Series} ready to be added to the chart
   */
  private static XYChart.Series<Number, Number> buildSeries(
      List<BigDecimal> visiblePrices, int startIndex) {
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    for (int i = 0; i < visiblePrices.size(); i++) {
      series
          .getData()
          .add(new XYChart.Data<>(startIndex + i + 1, visiblePrices.get(i).doubleValue()));
    }
    return series;
  }

  /**
   * Applies a fixed Y-axis range around the visible price data.
   *
   * @param axis axis to update
   * @param visiblePrices prices currently shown by the chart
   */
  private static void configureYAxis(NumberAxis axis, List<BigDecimal> visiblePrices) {
    if (visiblePrices.isEmpty()) {
      return;
    }

    AxisBounds bounds = calculateYAxisBounds(visiblePrices);
    axis.setLowerBound(bounds.lowerBound());
    axis.setUpperBound(bounds.upperBound());
    axis.setTickUnit(calculateTickUnit(bounds));
  }

  /**
   * Calculates the padded Y-axis bounds for the visible price range.
   *
   * @param visiblePrices prices currently shown by the chart
   * @return lower and upper axis bounds with dynamic padding
   */
  private static AxisBounds calculateYAxisBounds(List<BigDecimal> visiblePrices) {
    double min = visiblePrices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0.0);
    double max = visiblePrices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(0.0);
    double padding = calculateAxisPadding(min, max);
    return new AxisBounds(min - padding, max + padding);
  }

  /**
   * Calculates the Y-axis padding around the visible minimum and maximum.
   *
   * @param min lowest visible price
   * @param max highest visible price
   * @return padding to apply above and below the data range
   */
  private static double calculateAxisPadding(double min, double max) {
    double spread = max - min;
    if (spread > 0) {
      return spread * AXIS_PADDING_RATIO;
    }

    double flatPricePadding = Math.abs(max) * AXIS_PADDING_RATIO;
    return flatPricePadding > 0 ? flatPricePadding : FALLBACK_AXIS_PADDING;
  }

  /**
   * Calculates a readable Y-axis tick interval from the rendered axis span.
   *
   * @param bounds rendered axis bounds
   * @return tick unit for the Y-axis
   */
  private static double calculateTickUnit(AxisBounds bounds) {
    return (bounds.upperBound() - bounds.lowerBound()) / TARGET_Y_AXIS_TICKS;
  }

  /**
   * Finds the first price index included in a chart range.
   *
   * @param prices complete ordered price history
   * @param range selected chart range
   * @return zero-based index for the first visible price
   */
  private static int rangeStartIndex(List<BigDecimal> prices, ChartRange range) {
    int visibleDays = range.getDayWindow().orElse(prices.size());
    return Math.max(0, prices.size() - visibleDays);
  }

  private record AxisBounds(double lowerBound, double upperBound) {}
}
