package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.session.PlayerLeaderboardEntry;
import model.session.PlayerLeaderboardMetric;
import model.session.PlayerLeaderboardRanking;

/**
 * Manages auth-screen player comparison leaderboard sorting and rank display.
 */
public class PlayerLeaderboardController {

  private final List<PlayerLeaderboardEntry> sourceEntries;
  private final ObservableList<PlayerLeaderboardEntry> displayedEntries =
      FXCollections.observableArrayList();
  private final Map<String, Integer> rankByUsername = new HashMap<>();
  private PlayerLeaderboardMetric activeMetric = PlayerLeaderboardMetric.NET_WORTH;
  private boolean ascending;

  /**
   * @param entries saved-player leaderboard entries from {@link model.session.SessionService}
   */
  public PlayerLeaderboardController(List<PlayerLeaderboardEntry> entries) {
    this.sourceEntries = new ArrayList<>(entries);
    applySort(activeMetric, ascending);
  }

  public ObservableList<PlayerLeaderboardEntry> getDisplayedEntries() {
    return displayedEntries;
  }

  public Map<String, Integer> getRankByUsername() {
    return rankByUsername;
  }

  public PlayerLeaderboardMetric getActiveMetric() {
    return activeMetric;
  }

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
    for (int i = 0; i < sorted.size(); i++) {
      rankByUsername.put(sorted.get(i).username(), i + 1);
    }
  }

  public int getRankFor(String username) {
    return rankByUsername.getOrDefault(username, 0);
  }
}
