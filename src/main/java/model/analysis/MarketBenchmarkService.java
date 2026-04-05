package model.analysis;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.core.market.Exchange;
import model.core.market.Stock;

/**
 * Builds market benchmark metrics from the listed stocks on an exchange.
 */
public class MarketBenchmarkService {

  private final HistoricalAssetPriceService historicalAssetPriceService;
  private final PerformanceMetricsCalculator performanceMetricsCalculator;
  private final ReturnSeriesCalculator returnSeriesCalculator;

  /**
   * Creates a benchmark service with default collaborators.
   */
  public MarketBenchmarkService() {
    this(
        new HistoricalAssetPriceService(),
        new PerformanceMetricsCalculator(),
        new ReturnSeriesCalculator());
  }

  /**
   * Creates a benchmark service with injected collaborators.
   *
   * @param historicalAssetPriceService  helper used to resolve historical prices
   * @param performanceMetricsCalculator helper used to derive performance metrics
   * @param returnSeriesCalculator       helper used to derive daily returns
   */
  public MarketBenchmarkService(
      HistoricalAssetPriceService historicalAssetPriceService,
      PerformanceMetricsCalculator performanceMetricsCalculator,
      ReturnSeriesCalculator returnSeriesCalculator) {
    checkNotNull(historicalAssetPriceService, "Historical asset price service");
    checkNotNull(performanceMetricsCalculator, "Performance metrics calculator");
    checkNotNull(returnSeriesCalculator, "Return series calculator");
    this.historicalAssetPriceService = historicalAssetPriceService;
    this.performanceMetricsCalculator = performanceMetricsCalculator;
    this.returnSeriesCalculator = returnSeriesCalculator;
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
    return performanceMetricsCalculator.calculateFromDailyValues(
        buildBenchmarkDailyValues(stocks, exchange.getDay()));
  }

  /**
   * Builds an index-like daily value series that starts at 1.0 and compounds equal-weight daily
   * stock returns.
   *
   * @param stocks      listed stocks included in the benchmark
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
      BigDecimal previous = historicalAssetPriceService.getStockPriceOnDay(stock, day - 1);
      BigDecimal current = historicalAssetPriceService.getStockPriceOnDay(stock, day);
      totalReturn = totalReturn.add(returnSeriesCalculator.calculateReturn(previous, current));
    }
    return totalReturn.divide(BigDecimal.valueOf(stocks.size()), 8, java.math.RoundingMode.HALF_UP);
  }
}
