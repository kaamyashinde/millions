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
 * @since 2026-03-29
 */
public class StockChart extends LineChart<Number, Number> {

  private static final double AXIS_PADDING_RATIO = 0.10;
  private static final int TARGET_AXIS_TICKS = 5;
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
    super(buildXaxis(), buildYaxis());

    List<BigDecimal> prices = stock.getHistoricalPrices();
    int startIndex = rangeStartIndex(prices, range);
    List<BigDecimal> visiblePrices = prices.subList(startIndex, prices.size());

    setTitle(stock.getSymbol() + " — " + stock.getCompany());
    setCreateSymbols(visiblePrices.size() == 1);
    setLegendVisible(false);
    setAnimated(false);

    configureYaxis((NumberAxis) getYAxis(), visiblePrices);
    configureXaxis((NumberAxis) getXAxis(), startIndex, visiblePrices);
    getData().add(buildSeries(visiblePrices, startIndex));

    setOnMouseClicked(
        event -> {
          NumberAxis horizontalAxis = (NumberAxis) getXAxis();
          NumberAxis verticalAxis = (NumberAxis) getYAxis();
          double price =
              verticalAxis
                  .getValueForDisplay(
                      verticalAxis.sceneToLocal(event.getSceneX(), event.getSceneY()).getY())
                  .doubleValue();
          int dayIndex =
              (int)
                  Math.round(
                      horizontalAxis
                          .getValueForDisplay(
                              horizontalAxis
                                  .sceneToLocal(event.getSceneX(), event.getSceneY())
                                  .getX())
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

    NumberAxis verticalAxis = (NumberAxis) getYAxis();
    double markerLow = verticalAxis.getLowerBound();
    double markerHigh = verticalAxis.getUpperBound();

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
  private static NumberAxis buildXaxis() {
    NumberAxis axis = new NumberAxis();
    axis.setLabel("Day");
    axis.setMinorTickVisible(false);
    axis.setAutoRanging(false);
    return axis;
  }

  /**
   * Builds the Y axis labelled "Price ($)".
   *
   * @return a configured {@link NumberAxis} for the vertical axis
   */
  private static NumberAxis buildYaxis() {
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
   * Applies a fixed X-axis range around the visible trading-day window.
   *
   * @param axis axis to update
   * @param startIndex zero-based index of the first visible price in the full history
   * @param visiblePrices prices currently shown by the chart
   */
  private static void configureXaxis(
      NumberAxis axis, int startIndex, List<BigDecimal> visiblePrices) {
    if (visiblePrices.isEmpty()) {
      return;
    }

    int firstDay = startIndex + 1;
    int lastDay = startIndex + visiblePrices.size();
    AxisBounds bounds = calculateXaxisBounds(firstDay, lastDay);
    axis.setLowerBound(bounds.lowerBound());
    axis.setUpperBound(bounds.upperBound());
    axis.setTickUnit(calculateTickUnit(bounds));
  }

  /**
   * Calculates the padded X-axis bounds for the visible day range.
   *
   * @param firstDay first 1-based trading day shown
   * @param lastDay last 1-based trading day shown
   * @return lower and upper axis bounds with dynamic padding
   */
  private static AxisBounds calculateXaxisBounds(int firstDay, int lastDay) {
    double padding = calculateAxisPadding(firstDay, lastDay);
    return new AxisBounds(firstDay - padding, lastDay + padding);
  }

  /**
   * Applies a fixed Y-axis range around the visible price data.
   *
   * @param axis axis to update
   * @param visiblePrices prices currently shown by the chart
   */
  private static void configureYaxis(NumberAxis axis, List<BigDecimal> visiblePrices) {
    if (visiblePrices.isEmpty()) {
      return;
    }

    AxisBounds bounds = calculateYaxisBounds(visiblePrices);
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
  private static AxisBounds calculateYaxisBounds(List<BigDecimal> visiblePrices) {
    double min = visiblePrices.stream().mapToDouble(BigDecimal::doubleValue).min().orElse(0.0);
    double max = visiblePrices.stream().mapToDouble(BigDecimal::doubleValue).max().orElse(0.0);
    double padding = calculateAxisPadding(min, max);
    return new AxisBounds(min - padding, max + padding);
  }

  /**
   * Calculates axis padding around the visible minimum and maximum.
   *
   * @param min lowest visible value on the axis
   * @param max highest visible value on the axis
   * @return padding to apply below and above the data range
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
   * Calculates a readable axis tick interval from the rendered axis span.
   *
   * @param bounds rendered axis bounds
   * @return tick unit for the axis
   */
  private static double calculateTickUnit(AxisBounds bounds) {
    return (bounds.upperBound() - bounds.lowerBound()) / TARGET_AXIS_TICKS;
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
