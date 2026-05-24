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
 */
public final class LocalLeaderboardService {

  public record LeaderboardRow(
      String normalizedUsername,
      String displayName,
      BigDecimal netWorth,
      String level) {
  }

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final MarketDataFileService marketDataFileService;

  public LocalLeaderboardService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      MarketDataFileService marketDataFileService) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.marketDataFileService = marketDataFileService;
  }

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
