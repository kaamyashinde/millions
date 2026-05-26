package model.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import model.core.player.Player;
import model.persistence.ProfileFile;
import model.persistence.profile.ProfilePaths;
import model.session.auth.AuthService;
import model.session.leaderboard.PlayerLeaderboardEntry;
import model.session.leaderboard.PlayerLeaderboardMetric;
import model.session.leaderboard.PlayerLeaderboardRanking;

/**
 * Builds leaderboard entries from saved profiles and the live active session.
 */
final class SessionLeaderboardService {

  private final AuthService authService;

  /**
   * Creates a leaderboard service backed by profile data.
   *
   * @param authService authentication/profile loader used to enumerate profiles
   */
  SessionLeaderboardService(AuthService authService) {
    this.authService = authService;
  }

  /**
   * Lists profiles ranked by net worth, using unsaved live state for the active user.
   *
   * @param activeSession current active session, or {@code null}
   * @return leaderboard entries sorted best first
   */
  List<PlayerLeaderboardEntry> listEntries(ActiveSession activeSession) {
    List<PlayerLeaderboardEntry> entries = new ArrayList<>();
    for (String username : authService.listRegisteredUsers()) {
      String normalized = ProfilePaths.normalizeUsername(username);
      if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
        entries.add(toLeaderboardEntry(activeSession.player()));
        continue;
      }
      entries.add(toLeaderboardEntry(restoreLeaderboardPlayer(username, normalized)));
    }
    return entries.stream()
        .sorted(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH))
        .toList();
  }

  /**
   * Restores the saved profile state used for leaderboard calculations.
   *
   * @param username display username from the profile list
   * @param normalized normalized username used for market-data lookup
   * @return restored player with display name applied when valid
   */
  private Player restoreLeaderboardPlayer(String username, String normalized) {
    ProfileFile profile = authService.loadProfileOrThrow(username);
    Player player = profile
        .restore(authService.marketDataFileService().loadForProfile(normalized))
        .player();
    applyDisplayName(profile, player);
    return player;
  }

  /**
   * Applies a saved display name without failing leaderboard loading on invalid legacy values.
   *
   * @param profile saved profile metadata
   * @param player player to relabel
   */
  private static void applyDisplayName(ProfileFile profile, Player player) {
    if (profile.displayName() != null && !profile.displayName().isBlank()) {
      try {
        player.setName(profile.displayName().trim());
      } catch (IllegalArgumentException ignored) {
        // keep restored name
      }
    }
  }

  /**
   * Converts player state into the leaderboard entry model.
   *
   * @param player player to summarize
   * @return leaderboard entry
   */
  private static PlayerLeaderboardEntry toLeaderboardEntry(Player player) {
    BigDecimal netWorth = player.getNetWorth();
    BigDecimal startingMoney = player.getStartingMoney();
    BigDecimal totalReturnPercent = BigDecimal.ZERO;
    if (startingMoney.compareTo(BigDecimal.ZERO) != 0) {
      totalReturnPercent = netWorth.subtract(startingMoney)
          .divide(startingMoney, 8, RoundingMode.HALF_UP);
    }
    return new PlayerLeaderboardEntry(player.getName(), netWorth, totalReturnPercent);
  }
}
