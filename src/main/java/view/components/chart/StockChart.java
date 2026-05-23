package view.components.chart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private final List<ChartTool> tools = new ArrayList<>();

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

    setTitle(stock.getSymbol() + " \u2014 " + stock.getCompany());
    setCreateSymbols(visiblePrices(stock.getHistoricalPrices(), range).size() == 1);
    setLegendVisible(false);
    setAnimated(false);

    getData().add(buildSeries(stock.getHistoricalPrices(), range));

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
    return axis;
  }

  /**
   * Converts a list of closing prices into a chart series using 1-based day indices on the X axis.
   *
   * @param prices the ordered list of daily closing prices
   * @param range selected chart range
   * @return a {@link XYChart.Series} ready to be added to the chart
   */
  private static XYChart.Series<Number, Number> buildSeries(
      List<BigDecimal> prices, ChartRange range) {
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    int startIndex = rangeStartIndex(prices, range);
    List<BigDecimal> visiblePrices = prices.subList(startIndex, prices.size());
    for (int i = 0; i < visiblePrices.size(); i++) {
      series
          .getData()
          .add(new XYChart.Data<>(startIndex + i + 1, visiblePrices.get(i).doubleValue()));
    }
    return series;
  }

  private static List<BigDecimal> visiblePrices(List<BigDecimal> prices, ChartRange range) {
    return prices.subList(rangeStartIndex(prices, range), prices.size());
  }

  private static int rangeStartIndex(List<BigDecimal> prices, ChartRange range) {
    int visibleDays = range.getDayWindow().orElse(prices.size());
    return Math.max(0, prices.size() - visibleDays);
  }
}
