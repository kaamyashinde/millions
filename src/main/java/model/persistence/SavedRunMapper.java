package model.persistence;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.analysis.MetricValue;
import model.analysis.PerformanceComparison;
import model.analysis.PerformanceMetrics;
import model.analysis.PortfolioPerformanceService;
import model.core.market.Exchange;
import model.core.market.Share;
import model.core.player.Player;
import model.core.player.levels.PlayerLevel;
import model.core.trading.transactioncalculator.SaleCalculator;

/**
 * Builds {@link SavedRunRecord} snapshots from live {@link Player} and {@link Exchange} state.
 */
public final class SavedRunMapper {

  private final PortfolioPerformanceService performanceService;

  /**
   * Creates a mapper with the default performance service.
   */
  public SavedRunMapper() {
    this(new PortfolioPerformanceService());
  }

  /**
   * Creates a mapper with an injected performance service (tests).
   *
   * @param performanceService portfolio metrics calculator
   */
  public SavedRunMapper(PortfolioPerformanceService performanceService) {
    checkNotNull(performanceService, "performanceService");
    this.performanceService = performanceService;
  }

  /**
   * Captures the current run state for persistence.
   *
   * @param player                 active player
   * @param exchange               active exchange
   * @param label                  optional label (may be null)
   * @param eligibleForLeaderboard initial leaderboard flag
   * @return new record with a fresh run id and timestamp
   */
  public SavedRunRecord toSavedRun(
      Player player,
      Exchange exchange,
      String label,
      boolean eligibleForLeaderboard) {
    checkNotNull(player, "player");
    checkNotNull(exchange, "exchange");
    String runId = UUID.randomUUID().toString();
    String savedAt = Instant.now().toString();
    String safeLabel = label == null ? "" : label.trim();

    List<SavedRunRecord.SavedRunHoldingSnapshot> holdings = new ArrayList<>();
    for (Share share : player.getPortfolio().getShares()) {
      BigDecimal marketNet = new SaleCalculator(share).calculateTotal();
      holdings.add(
          new SavedRunRecord.SavedRunHoldingSnapshot(
              share.getAsset().getSymbol(),
              share.getAsset().getDisplayName(),
              share.getQuantity(),
              share.getPurchasePrice(),
              marketNet));
    }

    PerformanceComparison comparison = performanceService.compareAgainstMarket(player, exchange);
    SavedRunRecord.SavedRunStatsSnapshot stats = toStatsSnapshot(player, comparison);

    return new SavedRunRecord(
        SavedRunRecord.CURRENT_SCHEMA_VERSION,
        runId,
        savedAt,
        safeLabel,
        exchange.getDay(),
        player.getMoney(),
        holdings,
        stats,
        eligibleForLeaderboard);
  }

  private static SavedRunRecord.SavedRunStatsSnapshot toStatsSnapshot(
      Player player, PerformanceComparison comparison) {
    PlayerLevel level = player.getPlayerLevel();
    PerformanceMetrics portfolio = comparison.portfolio();
    PerformanceMetrics benchmark = comparison.benchmark();
    return new SavedRunRecord.SavedRunStatsSnapshot(
        player.getNetWorth(),
        player.getStartingMoney(),
        level.name(),
        metricDecimal(portfolio.returnPercent()),
        metricDecimal(portfolio.volatility()),
        metricDecimal(portfolio.sharpeRatio()),
        metricDecimal(benchmark.returnPercent()),
        metricDecimal(benchmark.volatility()),
        metricDecimal(benchmark.sharpeRatio()));
  }

  private static BigDecimal metricDecimal(MetricValue metricValue) {
    return metricValue.isAvailable() ? metricValue.value() : null;
  }
}
