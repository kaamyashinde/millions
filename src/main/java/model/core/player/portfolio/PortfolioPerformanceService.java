package model.core.player.portfolio;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.core.market.Exchange;
import model.core.market.HistoricalAssetPriceService;
import model.core.market.InvestableAsset;
import model.core.player.Player;
import model.core.player.Share;
import model.core.player.portfolio.metrics.MetricStatus;
import model.core.player.portfolio.metrics.PerformanceMetrics;
import model.core.player.portfolio.metrics.calc.PerformanceMetricsCalculator;
import model.core.trading.transaction.Purchase;
import model.core.trading.transaction.Sale;
import model.core.trading.transaction.Transaction;
import model.core.trading.transactioncalculator.SaleCalculator;

/**
 * Reconstructs the player's daily net worth and compares it with the market benchmark.
 */
public class PortfolioPerformanceService {

  private final HistoricalAssetPriceService historicalAssetPriceService;
  private final PerformanceMetricsCalculator performanceMetricsCalculator;
  private final MarketBenchmarkService marketBenchmarkService;

  /**
   * Creates a service with default collaborators.
   */
  public PortfolioPerformanceService() {
    this(
        new HistoricalAssetPriceService(),
        new PerformanceMetricsCalculator(),
        new MarketBenchmarkService());
  }

  /**
   * Creates a service with injected collaborators.
   *
   * @param historicalAssetPriceService  helper used to resolve historical asset prices
   * @param performanceMetricsCalculator helper used to derive metrics from value series
   * @param marketBenchmarkService       helper used to derive benchmark metrics
   */
  public PortfolioPerformanceService(
      HistoricalAssetPriceService historicalAssetPriceService,
      PerformanceMetricsCalculator performanceMetricsCalculator,
      MarketBenchmarkService marketBenchmarkService) {
    checkNotNull(historicalAssetPriceService, "Historical asset price service");
    checkNotNull(performanceMetricsCalculator, "Performance metrics calculator");
    checkNotNull(marketBenchmarkService, "Market benchmark service");
    this.historicalAssetPriceService = historicalAssetPriceService;
    this.performanceMetricsCalculator = performanceMetricsCalculator;
    this.marketBenchmarkService = marketBenchmarkService;
  }

  /**
   * Calculates the player's portfolio metrics and market benchmark metrics side by side.
   *
   * @param player   player whose strategy should be evaluated
   * @param exchange exchange supplying day count and benchmark listings
   * @return side-by-side performance comparison
   */
  public PerformanceComparison compareAgainstMarket(Player player, Exchange exchange) {
    checkNotNull(player, "Player");
    checkNotNull(exchange, "Exchange");
    return new PerformanceComparison(
        calculatePortfolioMetrics(player, exchange),
        marketBenchmarkService.calculateForExchange(exchange));
  }

  /**
   * Calculates the player's portfolio metrics from reconstructed daily net worth.
   *
   * @param player   player whose strategy should be evaluated
   * @param exchange exchange supplying the number of trading days in scope
   * @return player metrics, or unavailable metrics when no trades exist
   */
  public PerformanceMetrics calculatePortfolioMetrics(Player player, Exchange exchange) {
    checkNotNull(player, "Player");
    checkNotNull(exchange, "Exchange");
    if (player.getTransactionArchive().isEmpty()) {
      return PerformanceMetrics.unavailable(MetricStatus.NO_TRADES);
    }
    return performanceMetricsCalculator.calculateFromDailyValues(
        buildDailyNetWorthSeries(player, exchange));
  }

  /**
   * Replays the player's transactions day by day and records end-of-day net worth.
   *
   * @param player   player whose trades should be replayed
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

  private BigDecimal applyTransaction(Transaction transaction, Portfolio replayPortfolio,
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

  private BigDecimal calculateHistoricalPortfolioValue(Portfolio replayPortfolio, int day) {
    return replayPortfolio.getShares().stream()
        .map(share -> calculateHistoricalShareValue(share, day))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal calculateHistoricalShareValue(Share share, int day) {
    BigDecimal historicalPrice = historicalAssetPriceService.getPriceOnDay(share.getAsset(), day);
    Share historicalShare = new Share(
        new HistoricalAssetSnapshot(
            share.getAsset().getSymbol(),
            share.getAsset().getDisplayName(),
            share.getAsset().getAssetType(),
            historicalPrice),
        share.getQuantity(),
        share.getPurchasePrice());
    return new SaleCalculator(historicalShare).calculateTotal();
  }

  /**
   * Lightweight historical asset wrapper so existing sale valuation logic can be reused for an
   * arbitrary trading day.
   */
  private record HistoricalAssetSnapshot(
      String symbol,
      String displayName,
      String assetType,
      BigDecimal salesPrice) implements InvestableAsset {

    @Override
    public String getSymbol() {
      return symbol;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }

    @Override
    public BigDecimal getSalesPrice() {
      return salesPrice;
    }

    @Override
    public String getAssetType() {
      return assetType;
    }
  }
}
