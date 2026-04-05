package model.session.leaderboard;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import model.core.market.Exchange;
import model.core.player.Player;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.GameStateSnapshot;
import model.persistence.MarketData;
import model.persistence.ProfileImageService;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;

/**
 * Ranks local profiles by net worth using each user's saved game state.
 */
public final class LocalLeaderboardService {

  /**
   * One row in the local leaderboard.
   *
   * @param normalizedUsername profile directory key
   * @param displayName        name shown in UI
   * @param netWorth           total net worth from saved state
   * @param hasAvatar          whether an avatar image file exists
   */
  public record LeaderboardRow(
      String normalizedUsername,
      String displayName,
      BigDecimal netWorth,
      boolean hasAvatar
  ) {

  }

  private final UserAccountRepository userAccountRepository;
  private final GameStateRepository gameStateRepository;
  private final GameStateMapper gameStateMapper;
  private final Supplier<MarketData> marketDataSupplier;
  private final ProfileImageService profileImageService;

  public LocalLeaderboardService(
      UserAccountRepository userAccountRepository,
      GameStateRepository gameStateRepository,
      GameStateMapper gameStateMapper,
      Supplier<MarketData> marketDataSupplier,
      ProfileImageService profileImageService) {
    this.userAccountRepository = userAccountRepository;
    this.gameStateRepository = gameStateRepository;
    this.gameStateMapper = gameStateMapper;
    this.marketDataSupplier = marketDataSupplier;
    this.profileImageService = profileImageService;
  }

  /**
   * Loads all profiles with saved game state, sorted by net worth descending.
   */
  public List<LeaderboardRow> loadRows() {
    MarketData marketData = marketDataSupplier.get();
    if (marketData == null || marketData.isEmpty()) {
      throw new IllegalStateException("Could not load market data for leaderboard.");
    }
    List<LeaderboardRow> rows = new ArrayList<>();
    for (UserAccountRecord account : userAccountRepository.listAccounts()) {
      Optional<GameStateSnapshot> snapshot = gameStateRepository.load(account.normalizedUsername());
      if (snapshot.isEmpty()) {
        continue;
      }
      Exchange exchange = gameStateMapper.restoreExchange(snapshot.get().exchange(), marketData);
      Player player = gameStateMapper.restorePlayer(snapshot.get().player(), exchange);
      BigDecimal netWorth = player.getNetWorth();
      String label = account.displayName() != null && !account.displayName().isBlank()
          ? account.displayName().trim()
          : account.username();
      boolean hasAvatar =
          Files.isRegularFile(profileImageService.avatarPath(account.normalizedUsername()));
      rows.add(new LeaderboardRow(account.normalizedUsername(), label, netWorth, hasAvatar));
    }
    rows.sort(Comparator.comparing(LeaderboardRow::netWorth).reversed());
    return rows;
  }
}
