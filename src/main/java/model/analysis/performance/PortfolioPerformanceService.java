package model.analysis.performance;


import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.market.Exchange;
import model.core.player.Player;
import model.core.player.Portfolio;
import model.trading.calculator.SaleCalculator;
import model.trading.transaction.Purchase;
import model.trading.transaction.Sale;
import model.trading.transaction.Transaction;

/**
 * Reconstructs the player's daily net worth and compares it with the market benchmark.
 */
public class PortfolioPerformanceService {

  private static final int SCALE = 8;

  /**
   * Calculates the player's portfolio metrics and market benchmark metrics side by side.
   *
   * @param player player whose strategy should be evaluated
   * @param exchange exchange supplying day count and benchmark listings
   * @return side-by-side performance comparison
   */
  public PerformanceComparison compareAgainstMarket(Player player, Exchange exchange) {
    checkNotNull(player, "Player");
    checkNotNull(exchange, "Exchange");
    return new PerformanceComparison(
        calculatePortfolioMetrics(player, exchange),
        calculateBenchmarkMetrics(exchange));
  }

  /**
   * Calculates the player's portfolio metrics from reconstructed daily net worth.
   *
   * @param player player whose strategy should be evaluated
   * @param exchange exchange supplying the number of trading days in scope
   * @return player metrics, or unavailable metrics when no trades exist
   */
  public PerformanceMetrics calculatePortfolioMetrics(Player player, Exchange exchange) {
    checkNotNull(player, "Player");
    checkNotNull(exchange, "Exchange");
    if (player.getTransactionArchive().isEmpty()) {
      return PerformanceMetrics.unavailable(MetricStatus.NO_TRADES);
    }
    return PerformanceAnalyzer.calculateMetrics(buildDailyNetWorthSeries(player, exchange));
  }

  /**
   * Calculates the market benchmark metrics from exchange stock history.
   */
  private PerformanceMetrics calculateBenchmarkMetrics(Exchange exchange) {
    List<Stock> stocks = exchange.findStocks("");
    if (stocks.isEmpty() || exchange.getDay() < 2) {
      return PerformanceMetrics.unavailable(MetricStatus.INSUFFICIENT_HISTORY);
    }
    return PerformanceAnalyzer.calculateMetrics(
        buildBenchmarkDailyValues(stocks, exchange.getDay()));
  }

  /**
   * Builds an equal-weight market value series from listed stocks.
   */
  private List<BigDecimal> buildBenchmarkDailyValues(List<Stock> stocks, int tradingDays) {
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

  /**
   * Calculates the average stock return for one market day.
   */
  private BigDecimal averageDailyReturn(List<Stock> stocks, int day) {
    BigDecimal totalReturn = BigDecimal.ZERO;
    for (Stock stock : stocks) {
      BigDecimal previous = stock.getPriceOnDay(day - 1);
      BigDecimal current = stock.getPriceOnDay(day);
      totalReturn = totalReturn.add(calculateReturn(previous, current));
    }
    return totalReturn.divide(BigDecimal.valueOf(stocks.size()), SCALE, RoundingMode.HALF_UP);
  }

  /**
   * Computes one decimal return from a start value to an end value.
   */
  private BigDecimal calculateReturn(BigDecimal startValue, BigDecimal endValue) {
    checkNotNull(startValue, "Start value");
    checkNotNull(endValue, "End value");
    if (startValue.signum() == 0) {
      throw new IllegalArgumentException("Start value must be non-zero.");
    }
    return endValue.subtract(startValue).divide(startValue, SCALE, RoundingMode.HALF_UP);
  }

  /**
   * Replays the player's transactions day by day and records end-of-day net worth.
   *
   * @param player player whose trades should be replayed
   * @param exchange exchange supplying the day range
   * @return ordered end-of-day net worth series
   */
  public List<BigDecimal> buildDailyNetWorthSeries(Player player, Exchange exchange) {
    checkNotNull(player, "Player");
    checkNotNull(exchange, "Exchange");

    List<Transaction> transactions =
        player.getTransactionArchive().getTransactions(exchange.getDay());
    Portfolio replayPortfolio = new Portfolio();
    BigDecimal cash = player.getStartingMoney();
    int transactionIndex = 0;
    List<BigDecimal> dailyValues = new ArrayList<>();

    for (int day = 1; day <= exchange.getDay(); day++) {
      while (transactionIndex < transactions.size()
          && transactions.get(transactionIndex).getDay() == day) {
        cash = applyTransaction(transactions.get(transactionIndex), replayPortfolio, cash);
        transactionIndex++;
      }
      BigDecimal holdingsValue = calculateHistoricalPortfolioValue(replayPortfolio, day);
      dailyValues.add(cash.add(holdingsValue));
    }

    return dailyValues;
  }

  /**
   * Applies one historical transaction to the replayed portfolio and cash balance.
   */
  private BigDecimal applyTransaction(
      Transaction transaction,
      Portfolio replayPortfolio,
      BigDecimal cash) {
    if (transaction instanceof Purchase purchase) {
      replayPortfolio.addShare(purchase.getShare());
      return cash.subtract(purchase.getCalculator().calculateTotal());
    }
    if (transaction instanceof Sale sale) {
      if (!replayPortfolio.removeFifoSliceForSale(sale.getShare())) {
        throw new IllegalStateException("Could not replay historical sale from portfolio state.");
      }
      return cash.add(sale.getCalculator().calculateTotal());
    }
    throw new IllegalArgumentException(
        "Unsupported transaction type: " + transaction.getClass().getSimpleName());
  }

  /**
   * Calculates the replayed portfolio liquidation value for one day.
   */
  private BigDecimal calculateHistoricalPortfolioValue(Portfolio replayPortfolio, int day) {
    return replayPortfolio.getShares().stream()
        .map(share -> calculateHistoricalShareValue(share, day))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates one historical share position using that day's asset price.
   */
  private BigDecimal calculateHistoricalShareValue(Share share, int day) {
    BigDecimal historicalPrice = share.getAsset().getPriceOnDay(day);
    return new SaleCalculator(share, historicalPrice).calculateTotal();
  }
}
