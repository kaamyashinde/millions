package model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.session.leaderboard.PlayerLeaderboardEntry;
import model.session.leaderboard.PlayerLeaderboardMetric;
import model.session.leaderboard.PlayerLeaderboardRanking;
import org.junit.jupiter.api.Test;

class PlayerLeaderboardRankingTest {

  @Test
  void bestFirstComparator_netWorth_sortsByNetWorthThenTieBreaks() {
    PlayerLeaderboardEntry alice =
        new PlayerLeaderboardEntry("alice", new BigDecimal("1000"), new BigDecimal("0.10"));
    PlayerLeaderboardEntry bob =
        new PlayerLeaderboardEntry("bob", new BigDecimal("1000"), new BigDecimal("0.20"));
    PlayerLeaderboardEntry charlie =
        new PlayerLeaderboardEntry("charlie", new BigDecimal("900"), new BigDecimal("0.50"));

    List<PlayerLeaderboardEntry> rows = new ArrayList<>(List.of(charlie, alice, bob));
    rows.sort(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH));

    assertEquals(List.of(bob, alice, charlie), rows);
  }

  @Test
  void bestFirstComparator_netWorth_breaksTiesByUsername() {
    PlayerLeaderboardEntry a =
        new PlayerLeaderboardEntry("A", new BigDecimal("100"), new BigDecimal("0.1"));
    PlayerLeaderboardEntry b =
        new PlayerLeaderboardEntry("b", new BigDecimal("100"), new BigDecimal("0.1"));

    List<PlayerLeaderboardEntry> rows = new ArrayList<>(List.of(b, a));
    rows.sort(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH));

    assertEquals(List.of(a, b), rows);
  }

  @Test
  void displayComparator_ascending_netWorth_ordersLowToHighThenSecondary() {
    PlayerLeaderboardEntry alice =
        new PlayerLeaderboardEntry("alice", new BigDecimal("1000"), new BigDecimal("0.10"));
    PlayerLeaderboardEntry bob =
        new PlayerLeaderboardEntry("bob", new BigDecimal("1000"), new BigDecimal("0.20"));
    PlayerLeaderboardEntry charlie =
        new PlayerLeaderboardEntry("charlie", new BigDecimal("900"), new BigDecimal("0.50"));

    Comparator<PlayerLeaderboardEntry> asc =
        PlayerLeaderboardRanking.displayComparator(PlayerLeaderboardMetric.NET_WORTH, true);
    List<PlayerLeaderboardEntry> rows = new ArrayList<>(List.of(bob, alice, charlie));
    rows.sort(asc);

    assertEquals(List.of(charlie, bob, alice), rows);
  }

  @Test
  void displayComparator_descending_matchesBestFirstForNetWorth() {
    PlayerLeaderboardEntry alice =
        new PlayerLeaderboardEntry("alice", new BigDecimal("1000"), new BigDecimal("0.10"));
    PlayerLeaderboardEntry bob =
        new PlayerLeaderboardEntry("bob", new BigDecimal("1000"), new BigDecimal("0.20"));

    Comparator<PlayerLeaderboardEntry> desc =
        PlayerLeaderboardRanking.displayComparator(PlayerLeaderboardMetric.NET_WORTH, false);
    List<PlayerLeaderboardEntry> rows = new ArrayList<>(List.of(alice, bob));
    rows.sort(desc);

    List<PlayerLeaderboardEntry> expected = new ArrayList<>(List.of(alice, bob));
    expected.sort(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH));
    assertEquals(expected, rows);
  }

  @Test
  void metricValue_returnsFieldForMetric() {
    PlayerLeaderboardEntry entry =
        new PlayerLeaderboardEntry("u", new BigDecimal("500"), new BigDecimal("0.25"));

    assertEquals(new BigDecimal("500"), PlayerLeaderboardRanking.metricValue(entry,
        PlayerLeaderboardMetric.NET_WORTH));
    assertEquals(new BigDecimal("0.25"), PlayerLeaderboardRanking.metricValue(entry,
        PlayerLeaderboardMetric.TOTAL_RETURN_PERCENT));
  }
}
