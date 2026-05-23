package model.analysis.performance;


import model.analysis.PerformanceAnalyzer;
import model.analysis.metric.MetricStatus;
import model.analysis.metric.PerformanceMetrics;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import model.core.market.Exchange;
import model.core.asset.Stock;

/**
 * Builds market benchmark metrics from the listed stocks on an exchange.
 */
public class MarketBenchmarkService {

  private static final int SCALE = 8;

  /**
   * Creates a benchmark service.
   */
  public MarketBenchmarkService() {
  }

  /**
   * Calculates benchmark metrics for the given exchange using an equal-weight stock basket.
   *
   * @param exchange exchange whose listed stocks define the benchmark
   * @return benchmark metrics, or unavailable metrics when history is too short
   */
  public PerformanceMetrics calculateForExchange(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    List<Stock> stocks = exchange.findStocks("");
    if (stocks.isEmpty() || exchange.getDay() < 2) {
      return PerformanceMetrics.unavailable(MetricStatus.INSUFFICIENT_HISTORY);
    }
    return PerformanceAnalyzer.calculateMetrics(
        buildBenchmarkDailyValues(stocks, exchange.getDay()));
  }

  /**
   * Builds an index-like daily value series that starts at 1.0 and compounds equal-weight daily
   * stock returns.
   *
   * @param stocks listed stocks included in the benchmark
   * @param tradingDays number of trading days to include
   * @return benchmark daily values, oldest to newest
   */
  public List<BigDecimal> buildBenchmarkDailyValues(List<Stock> stocks, int tradingDays) {
    checkNotNull(stocks, "Stocks");
    if (stocks.isEmpty()) {
      throw new IllegalArgumentException("At least one stock is required.");
    }
    if (tradingDays < 1) {
      throw new IllegalArgumentException("Trading days must be at least 1.");
    }

    List<BigDecimal> values = new ArrayList<>();
    BigDecimal currentValue = BigDecimal.ONE;
    values.add(currentValue);
    for (int day = 2; day <= tradingDays; day++) {
      BigDecimal averageReturn = averageDailyReturn(stocks, day);
      currentValue = currentValue.multiply(BigDecimal.ONE.add(averageReturn));
      values.add(currentValue);
    }
    return values;
  }

  private BigDecimal averageDailyReturn(List<Stock> stocks, int day) {
    BigDecimal totalReturn = BigDecimal.ZERO;
    for (Stock stock : stocks) {
      BigDecimal previous = stock.getPriceOnDay(day - 1);
      BigDecimal current = stock.getPriceOnDay(day);
      totalReturn = totalReturn.add(calculateReturn(previous, current));
    }
    return totalReturn.divide(BigDecimal.valueOf(stocks.size()), SCALE, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateReturn(BigDecimal startValue, BigDecimal endValue) {
    checkNotNull(startValue, "Start value");
    checkNotNull(endValue, "End value");
    if (startValue.signum() == 0) {
      throw new IllegalArgumentException("Start value must be non-zero.");
    }
    return endValue.subtract(startValue).divide(startValue, SCALE, RoundingMode.HALF_UP);
  }
}
