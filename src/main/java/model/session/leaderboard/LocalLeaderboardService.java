package model.session.leaderboard;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.core.player.Player;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketDataFileService;
import model.persistence.profile.ProfilePaths;

/**
 * Ranks local profiles by net worth using each user's saved profile file.
 *
 * <p>Profiles are restored with their saved market data before ranking so net worth matches the
 * persisted game state.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class LocalLeaderboardService {

  /**
   * One row in the local leaderboard.
   *
   * @param normalizedUsername normalized profile key
   * @param displayName player display name shown in the leaderboard
   * @param netWorth restored player net worth
   * @param level restored player level display name
   */
  public record LeaderboardRow(
      String normalizedUsername,
      String displayName,
      BigDecimal netWorth,
      String level) {
  }

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final MarketDataFileService marketDataFileService;

  /**
   * Creates a local leaderboard service.
   *
   * @param profilePaths profile path resolver
   * @param jsonStorage JSON storage used to read profiles
   * @param marketDataFileService market data loader for each profile
   */
  public LocalLeaderboardService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      MarketDataFileService marketDataFileService) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.marketDataFileService = marketDataFileService;
  }

  /**
   * Loads and ranks saved local profiles.
   *
   * @return leaderboard rows sorted by net worth descending
   */
  public List<LeaderboardRow> loadRows() {
    List<LeaderboardRow> rows = new ArrayList<>();
    for (String username : profilePaths.listUsernames(jsonStorage)) {
      ProfileFile profile = jsonStorage.read(profilePaths.profileFile(username), ProfileFile.class);
      Player player = profile
          .restore(marketDataFileService.loadForProfile(profile.normalizedUsername()))
          .player();
      String label = profile.displayName() != null && !profile.displayName().isBlank()
          ? profile.displayName().trim()
          : profile.username();
      String level = player.getPlayerLevel().displayName();
      rows.add(new LeaderboardRow(profile.normalizedUsername(), label, player.getNetWorth(), level));
    }
    rows.sort(Comparator.comparing(LeaderboardRow::netWorth).reversed());
    return rows;
  }
}
