package controller;

import model.session.SessionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.session.leaderboard.PlayerLeaderboardEntry;
import model.session.leaderboard.PlayerLeaderboardMetric;
import model.session.leaderboard.PlayerLeaderboardRanking;

/**
 * Manages auth-screen player comparison leaderboard sorting and rank display.
 *
 * <p>The source rows remain unchanged while {@link #applySort(PlayerLeaderboardMetric, boolean)}
 * rebuilds the displayed list and rank lookup for the selected metric.
 *
 * @author kaamyashinde
 * @contributor kevindmazali
 * @version 1.0.0
 * @since 2026-05-02
 */
public class PlayerLeaderboardController {

  private final List<PlayerLeaderboardEntry> sourceEntries;
  private final ObservableList<PlayerLeaderboardEntry> displayedEntries =
      FXCollections.observableArrayList();
  private final Map<String, Integer> rankByUsername = new HashMap<>();
  private PlayerLeaderboardMetric activeMetric = PlayerLeaderboardMetric.NET_WORTH;
  private boolean ascending;

  /**
   * Creates a sortable player leaderboard.
   *
   * @param entries saved-player leaderboard entries from {@link SessionService}
   */
  public PlayerLeaderboardController(List<PlayerLeaderboardEntry> entries) {
    this.sourceEntries = new ArrayList<>(entries);
    applySort(activeMetric, ascending);
  }

  /**
   * Exposes the entries in the current sort order.
   *
   * @return observable entries in the current display order
   */
  public ObservableList<PlayerLeaderboardEntry> getDisplayedEntries() {
    return displayedEntries;
  }

  /**
   * Exposes the one-based rank lookup for displayed usernames.
   *
   * @return map from username to one-based rank in the current display order
   */
  public Map<String, Integer> getRankByUsername() {
    return rankByUsername;
  }

  /**
   * Returns the metric currently controlling the sort order.
   *
   * @return metric currently used for sorting
   */
  public PlayerLeaderboardMetric getActiveMetric() {
    return activeMetric;
  }

  /**
   * Returns whether rows are displayed in ascending order.
   *
   * @return true when the displayed entries are reversed into ascending order
   */
  public boolean isAscending() {
    return ascending;
  }

  /**
   * Re-sorts displayed rows by the given metric.
   *
   * @param metric net worth or return percent
   * @param ascending whether sort is ascending
   */
  public void applySort(PlayerLeaderboardMetric metric, boolean ascending) {
    this.activeMetric = metric;
    this.ascending = ascending;
    List<PlayerLeaderboardEntry> sorted = new ArrayList<>(sourceEntries);
    sorted.sort(PlayerLeaderboardRanking.bestFirstComparator(metric));
    if (ascending) {
      java.util.Collections.reverse(sorted);
    }
    displayedEntries.setAll(sorted);
    rankByUsername.clear();
    IntStream.range(0, sorted.size())
        .forEach(i -> rankByUsername.put(sorted.get(i).username(), i + 1));
  }

  /**
   * Looks up a user's current rank.
   *
   * @param username username to find
   * @return one-based rank, or {@code 0} when the user is not displayed
   */
  public int getRankFor(String username) {
    return rankByUsername.getOrDefault(username, 0);
  }
}
