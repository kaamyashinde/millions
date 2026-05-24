package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.core.market.Exchange;
import model.core.player.Player;
import model.analysis.performance.MetricValue;
import model.analysis.performance.PerformanceComparison;
import model.analysis.performance.PortfolioPerformanceService;
import model.core.asset.Share;

/**
 * Supplies portfolio summary, holdings, and performance metrics for the player tab.
 */
public class PortfolioController {

  private final Exchange exchange;
  private final Player player;
  private final Path avatarPath;
  private final PortfolioPerformanceService performanceService = new PortfolioPerformanceService();
  private final ObservableList<HoldingSummary> holdings = FXCollections.observableArrayList();
  private PerformanceComparison lastComparison;

  /**
   * @param exchange exchange supplying trading-day state
   * @param player active player
   * @param avatarPath profile avatar path on disk
   */
  public PortfolioController(Exchange exchange, Player player, Path avatarPath) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    checkNotNull(avatarPath, "avatarPath");
    this.exchange = exchange;
    this.player = player;
    this.avatarPath = avatarPath;
    refresh();
  }

  public Exchange getExchange() {
    return exchange;
  }

  public Player getPlayer() {
    return player;
  }

  public Path getAvatarPath() {
    return avatarPath;
  }

  public ObservableList<HoldingSummary> getHoldings() {
    return holdings;
  }

  public PerformanceComparison getLastComparison() {
    return lastComparison;
  }

  public String getPlayerName() {
    return player.getName();
  }

  public int getTradingDay() {
    return exchange.getDay();
  }

  public String getFormattedBalance() {
    return player.getMoney().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  public String getFormattedNetWorth() {
    return player.getNetWorth().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  public static String formatMetricValue(MetricValue metric, boolean percentDisplay) {
    if (!metric.isAvailable()) {
      return switch (metric.status()) {
        case NO_TRADES -> "N/A (no trades yet)";
        case INSUFFICIENT_HISTORY -> "N/A (need more history)";
        case ZERO_VOLATILITY -> "N/A (zero volatility)";
        case AVAILABLE -> throw new IllegalArgumentException("Available metrics do not need fallback text.");
      };
    }
    BigDecimal value = metric.value();
    if (percentDisplay) {
      return value.multiply(BigDecimal.valueOf(100))
          .setScale(2, RoundingMode.HALF_UP)
          .toPlainString() + "%";
    }
    return value.setScale(3, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * Reloads holdings and performance comparison from live model state.
   */
  public void refresh() {
    holdings.setAll(summarizeHoldings(player.getPortfolio().getShares()));
    lastComparison = performanceService.compareAgainstMarket(player, exchange);
  }

  private static List<HoldingSummary> summarizeHoldings(List<Share> shares) {
    Map<String, List<Share>> bySymbol = new LinkedHashMap<>();
    for (Share share : shares) {
      bySymbol.computeIfAbsent(share.getAsset().getSymbol(), _ -> new ArrayList<>()).add(share);
    }

    List<HoldingSummary> summaries = new ArrayList<>();
    for (List<Share> lots : bySymbol.values()) {
      Share first = lots.getFirst();
      BigDecimal totalQuantity = BigDecimal.ZERO;
      BigDecimal totalCost = BigDecimal.ZERO;
      for (Share lot : lots) {
        totalQuantity = totalQuantity.add(lot.getQuantity());
        totalCost = totalCost.add(lot.getQuantity().multiply(lot.getPurchasePrice()));
      }
      BigDecimal avgPurchasePrice =
          totalCost.divide(totalQuantity, 2, RoundingMode.HALF_UP);
      summaries.add(
          new HoldingSummary(
              first.getAsset().getSymbol(),
              first.getAsset().getDisplayName(),
              first.getAsset().getAssetType(),
              totalQuantity,
              avgPurchasePrice,
              first.getAsset().getSalesPrice()));
    }
    return summaries;
  }
}
