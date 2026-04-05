package model.session.leaderboard;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Comparator helpers for leaderboard ordering and rank calculation.
 */
public final class PlayerLeaderboardRanking {

  private PlayerLeaderboardRanking() {
  }

  /**
   * Comparator used to calculate best-first leaderboard ranks for the supplied metric.
   *
   * @param metric active ranking metric
   * @return comparator ordering strongest players first
   */
  public static Comparator<PlayerLeaderboardEntry> bestFirstComparator(
      PlayerLeaderboardMetric metric) {
    return Comparator
        .comparing(
            (PlayerLeaderboardEntry entry) -> metricValue(entry, metric),
            Comparator.reverseOrder())
        .thenComparing(
            entry -> metricValue(entry, secondaryMetric(metric)),
            Comparator.reverseOrder())
        .thenComparing(PlayerLeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(PlayerLeaderboardEntry::username);
  }

  /**
   * Comparator used for display ordering.
   *
   * @param metric    active display metric
   * @param ascending whether the primary metric should be shown low-to-high
   * @return comparator matching the requested display order
   */
  public static Comparator<PlayerLeaderboardEntry> displayComparator(
      PlayerLeaderboardMetric metric,
      boolean ascending) {
    if (!ascending) {
      return bestFirstComparator(metric);
    }
    return Comparator
        .comparing((PlayerLeaderboardEntry entry) -> metricValue(entry, metric),
            BigDecimal::compareTo)
        .thenComparing(
            entry -> metricValue(entry, secondaryMetric(metric)),
            Comparator.reverseOrder())
        .thenComparing(PlayerLeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(PlayerLeaderboardEntry::username);
  }

  /**
   * Returns the numeric metric value used for sorting.
   *
   * @param entry  leaderboard entry
   * @param metric chosen metric
   * @return numeric value for that metric
   */
  public static BigDecimal metricValue(PlayerLeaderboardEntry entry,
      PlayerLeaderboardMetric metric) {
    return switch (metric) {
      case NET_WORTH -> entry.netWorth();
      case TOTAL_RETURN_PERCENT -> entry.totalReturnPercent();
    };
  }

  private static PlayerLeaderboardMetric secondaryMetric(PlayerLeaderboardMetric metric) {
    return metric == PlayerLeaderboardMetric.NET_WORTH
        ? PlayerLeaderboardMetric.TOTAL_RETURN_PERCENT
        : PlayerLeaderboardMetric.NET_WORTH;
  }
}
