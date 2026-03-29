package view.components.chart;

import java.math.BigDecimal;
import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import model.Stock;

/**
 * A {@link LineChart} that visualises the full price history of a {@link Stock}.
 *
 * <p>Each data point maps a 1-based day index (X) to the closing price on that day (Y).
 *
 * @author kaamyashinde
 * @version 0.5.0
 * @since 29-03-2026
 */
public class StockChart extends LineChart<Number, Number> {

  /**
   * Creates a chart pre-populated with the price history of {@code stock}.
   *
   * @param stock the stock whose historical prices are displayed
   */
  public StockChart(Stock stock) {
    super(buildXAxis(), buildYAxis());

    setTitle(stock.getSymbol() + " \u2014 " + stock.getCompany());
    setCreateSymbols(false);
    setLegendVisible(false);
    setAnimated(false);

    getData().add(buildSeries(stock.getHistoricalPrices()));
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
   * @return a {@link XYChart.Series} ready to be added to the chart
   */
  private static XYChart.Series<Number, Number> buildSeries(List<BigDecimal> prices) {
    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    for (int i = 0; i < prices.size(); i++) {
      series.getData().add(new XYChart.Data<>(i + 1, prices.get(i).doubleValue()));
    }
    return series;
  }
}
