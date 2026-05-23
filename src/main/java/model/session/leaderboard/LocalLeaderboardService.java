package model.session.leaderboard;


import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import model.core.player.Player;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;

/**
 * Ranks local profiles by net worth using each user's saved profile file.
 */
public final class LocalLeaderboardService {

  public record LeaderboardRow(
      String normalizedUsername,
      String displayName,
      BigDecimal netWorth,
      boolean hasAvatar) {
  }

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final Supplier<MarketData> marketDataSupplier;
  private final ProfileImageService profileImageService;

  public LocalLeaderboardService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      Supplier<MarketData> marketDataSupplier,
      ProfileImageService profileImageService) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.marketDataSupplier = marketDataSupplier;
    this.profileImageService = profileImageService;
  }

  public List<LeaderboardRow> loadRows() {
    MarketData marketData = marketDataSupplier.get();
    if (marketData == null || marketData.isEmpty()) {
      throw new IllegalStateException("Could not load market data for leaderboard.");
    }
    List<LeaderboardRow> rows = new ArrayList<>();
    for (String username : profilePaths.listUsernames(jsonStorage)) {
      ProfileFile profile = jsonStorage.read(profilePaths.profileFile(username), ProfileFile.class);
      Player player = profile.restore(marketData).player();
      String label = profile.displayName() != null && !profile.displayName().isBlank()
          ? profile.displayName().trim()
          : profile.username();
      boolean hasAvatar = Files.isRegularFile(
          profileImageService.avatarPath(profile.normalizedUsername()));
      rows.add(new LeaderboardRow(profile.normalizedUsername(), label, player.getNetWorth(), hasAvatar));
    }
    rows.sort(Comparator.comparing(LeaderboardRow::netWorth).reversed());
    return rows;
  }
}
