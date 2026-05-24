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
 *
 * <p>The controller converts {@link Player} portfolio lots into {@link HoldingSummary} rows and
 * compares the player against the market through {@link PortfolioPerformanceService}.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-30
 */
public class PortfolioController {

  private final Exchange exchange;
  private final Player player;
  private final Path avatarPath;
  private final PortfolioPerformanceService performanceService = new PortfolioPerformanceService();
  private final ObservableList<HoldingSummary> holdings = FXCollections.observableArrayList();
  private PerformanceComparison lastComparison;

  /**
   * Creates a portfolio controller and loads the initial summary state.
   *
   * @param exchange exchange supplying trading-day state
   * @param player active player whose portfolio is displayed
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

  /**
   * Exposes the exchange used for prices and benchmark comparison.
   *
   * @return exchange used for current prices and benchmark comparison
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Exposes the active player represented by this controller.
   *
   * @return active player represented by this controller
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Returns the active profile avatar path.
   *
   * @return avatar image path for the active profile
   */
  public Path getAvatarPath() {
    return avatarPath;
  }

  /**
   * Exposes holdings summarized for the portfolio table.
   *
   * @return observable holding rows for the portfolio table
   */
  public ObservableList<HoldingSummary> getHoldings() {
    return holdings;
  }

  /**
   * Returns the latest cached portfolio comparison.
   *
   * @return most recent portfolio-versus-market comparison
   */
  public PerformanceComparison getLastComparison() {
    return lastComparison;
  }

  /**
   * Returns the active player's display name.
   *
   * @return display name of the active player
   */
  public String getPlayerName() {
    return player.getName();
  }

  /**
   * Returns the current exchange trading day.
   *
   * @return current exchange trading day
   */
  public int getTradingDay() {
    return exchange.getDay();
  }

  /**
   * Formats the active player's cash balance.
   *
   * @return cash balance formatted with two decimal places
   */
  public String getFormattedBalance() {
    return player.getMoney().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * Formats the active player's net worth.
   *
   * @return net worth formatted with two decimal places
   */
  public String getFormattedNetWorth() {
    return player.getNetWorth().setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  /**
   * Formats a portfolio metric for display.
   *
   * @param metric metric value and availability status
   * @param percentDisplay true when the value should be rendered as a percentage
   * @return formatted metric text or a status-specific unavailable message
   */
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
