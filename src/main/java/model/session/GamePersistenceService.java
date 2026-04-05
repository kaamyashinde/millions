package model.session;

import java.util.Optional;
import java.util.function.Supplier;
import model.core.market.Exchange;
import model.core.player.Player;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.GameStateSnapshot;
import model.persistence.MarketData;
import model.persistence.ProfileImageService;
import model.persistence.UserAccountRepository;

/**
 * Persists and restores game-state snapshots for user profiles.
 */
public final class GamePersistenceService {

  private final GameStateRepository gameStateRepository;
  private final GameStateMapper gameStateMapper;
  private final Supplier<MarketData> marketDataSupplier;

  /**
   * Creates a game persistence service backed by a JSON repository and mapper.
   *
   * @param gameStateRepository saved game-state repository
   * @param gameStateMapper     domain-to-snapshot converter
   * @param marketDataSupplier  supplier for fresh bundled market data
   */
  public GamePersistenceService(
      GameStateRepository gameStateRepository,
      GameStateMapper gameStateMapper,
      Supplier<MarketData> marketDataSupplier) {
    this.gameStateRepository = gameStateRepository;
    this.gameStateMapper = gameStateMapper;
    this.marketDataSupplier = marketDataSupplier;
  }

  /**
   * Persists the current player and exchange state as a game-state snapshot.
   *
   * @param normalizedUsername profile directory key
   * @param player             active player state
   * @param exchange           active exchange state
   */
  public void saveSession(String normalizedUsername, Player player, Exchange exchange) {
    gameStateRepository.save(normalizedUsername, gameStateMapper.toSnapshot(player, exchange));
  }

  /**
   * Loads the saved game-state snapshot for a profile.
   *
   * @param normalizedUsername profile directory key
   * @return snapshot when present
   */
  public Optional<GameStateSnapshot> loadSnapshot(String normalizedUsername) {
    return gameStateRepository.load(normalizedUsername);
  }

  /**
   * Creates a fresh exchange populated with current bundled market data.
   *
   * @return new exchange at day 1 with empty history
   */
  public Exchange createFreshExchange() {
    return gameStateMapper.createFreshExchange(loadMarketData());
  }

  /**
   * Restores an exchange from a persisted snapshot using current bundled market data.
   *
   * @param exchangeSnapshot stored exchange state
   * @return restored exchange instance
   */
  public Exchange restoreExchange(GameStateSnapshot.ExchangeSnapshot exchangeSnapshot) {
    return gameStateMapper.restoreExchange(exchangeSnapshot, loadMarketData());
  }

  /**
   * Restores a player from a persisted snapshot using assets from the restored exchange.
   *
   * @param playerSnapshot stored player state
   * @param exchange       restored exchange that owns the referenced assets
   * @return restored player instance
   */
  public Player restorePlayer(GameStateSnapshot.PlayerSnapshot playerSnapshot, Exchange exchange) {
    return gameStateMapper.restorePlayer(playerSnapshot, exchange);
  }

  /**
   * Builds a leaderboard view service using this service's repositories and market data.
   *
   * @param userAccountRepository account metadata repository
   * @param profileImageService   avatar image service
   * @return new leaderboard service instance
   */
  LocalLeaderboardService createLeaderboardService(
      UserAccountRepository userAccountRepository,
      ProfileImageService profileImageService) {
    return new LocalLeaderboardService(
        userAccountRepository,
        gameStateRepository,
        gameStateMapper,
        marketDataSupplier,
        profileImageService);
  }

  private MarketData loadMarketData() {
    MarketData marketData = marketDataSupplier.get();
    if (marketData == null || marketData.isEmpty()) {
      throw new IllegalStateException("Could not load market data for profile session.");
    }
    return marketData;
  }
}
