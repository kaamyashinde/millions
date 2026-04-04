package model.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import model.Exchange;
import model.Player;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.GameStateSnapshot;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfileDirectories;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;

/**
 * Coordinates registration, authentication, profile switching, and save/load operations.
 */
public final class SessionService {

  private final UserAccountRepository userAccountRepository;
  private final GameStateRepository gameStateRepository;
  private final PinHashingService pinHashingService;
  private final Supplier<MarketData> marketDataSupplier;
  private final GameStateMapper gameStateMapper;

  private ActiveSession activeSession;

  /**
   * Creates a session service with repositories and a supplier for fresh market data.
   *
   * @param userAccountRepository account metadata repository
   * @param gameStateRepository saved game-state repository
   * @param pinHashingService PIN hashing helper
   * @param marketDataSupplier supplier that returns fresh bundled market data
   * @param exchangeName default exchange name used for new profiles
   */
  public SessionService(
      UserAccountRepository userAccountRepository,
      GameStateRepository gameStateRepository,
      PinHashingService pinHashingService,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    this.userAccountRepository = userAccountRepository;
    this.gameStateRepository = gameStateRepository;
    this.pinHashingService = pinHashingService;
    this.marketDataSupplier = marketDataSupplier;
    this.gameStateMapper = new GameStateMapper(exchangeName);
  }

  /**
   * Registers a new user profile and makes it the active session.
   *
   * @param username requested username
   * @param pin numeric PIN
   * @param startingMoney initial cash balance
   * @return new active session
   */
  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    validateRegistrationInput(username, pin, startingMoney);
    String normalizedUsername = ProfileDirectories.normalizeUsername(username);
    if (userAccountRepository.exists(normalizedUsername)) {
      throw new DuplicateUsernameException("That username is already registered.");
    }

    saveActiveSession();

    String trimmedUsername = username.trim();
    String saltBase64 = pinHashingService.generateSaltBase64();
    UserAccountRecord account = new UserAccountRecord(
        trimmedUsername,
        normalizedUsername,
        saltBase64,
        pinHashingService.hashPin(pin, saltBase64));

    Exchange exchange = gameStateMapper.createFreshExchange(loadMarketData());
    Player player = new Player(trimmedUsername, startingMoney);

    userAccountRepository.save(account);
    gameStateRepository.save(
        normalizedUsername,
        gameStateMapper.toSnapshot(player, exchange));

    activeSession = new ActiveSession(trimmedUsername, normalizedUsername, player, exchange);
    return activeSession;
  }

  /**
   * Authenticates an existing profile and makes it the active session.
   *
   * @param username username to log in
   * @param pin numeric PIN
   * @return active session for the loaded user
   */
  public ActiveSession login(String username, char[] pin) {
    validateLoginInput(username, pin);
    UserAccountRecord account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    if (activeSession != null && !activeSession.normalizedUsername().equals(account.normalizedUsername())) {
      saveActiveSession();
    }

    GameStateSnapshot snapshot = gameStateRepository.load(account.normalizedUsername())
        .orElseThrow(() -> new IllegalStateException("Saved game state not found for " + account.username() + "."));
    Exchange exchange = gameStateMapper.restoreExchange(snapshot.exchange(), loadMarketData());
    Player player = gameStateMapper.restorePlayer(snapshot.player(), exchange);
    activeSession = new ActiveSession(account.username(), account.normalizedUsername(), player, exchange);
    return activeSession;
  }

  /**
   * Saves and clears the active session.
   *
   * @return {@code true} when a session was logged out
   */
  public boolean logout() {
    if (activeSession == null) {
      return false;
    }
    saveActiveSession();
    activeSession = null;
    return true;
  }

  /**
   * Persists the currently active session if one exists.
   */
  public void saveActiveSession() {
    if (activeSession == null) {
      return;
    }
    gameStateRepository.save(
        activeSession.normalizedUsername(),
        gameStateMapper.toSnapshot(activeSession.player(), activeSession.exchange()));
  }

  /**
   * Returns whether a user is currently logged in.
   *
   * @return {@code true} when an active session exists
   */
  public boolean hasActiveSession() {
    return activeSession != null;
  }

  /**
   * Returns the active session, if any.
   *
   * @return optional active session
   */
  public Optional<ActiveSession> getActiveSession() {
    return Optional.ofNullable(activeSession);
  }

  /**
   * Lists all registered usernames.
   *
   * @return alphabetically sorted usernames
   */
  public List<String> listRegisteredUsers() {
    return userAccountRepository.listUsernames();
  }

  /**
   * Lists leaderboard entries for all saved profiles, using live in-memory state for the active
   * session when available.
   *
   * @return default-ranked leaderboard entries
   */
  public List<PlayerLeaderboardEntry> listLeaderboardEntries() {
    List<PlayerLeaderboardEntry> entries = new ArrayList<>();
    for (String username : userAccountRepository.listUsernames()) {
      String normalizedUsername = ProfileDirectories.normalizeUsername(username);
      if (activeSession != null && activeSession.normalizedUsername().equals(normalizedUsername)) {
        entries.add(toLeaderboardEntry(activeSession.player()));
        continue;
      }

      GameStateSnapshot snapshot = gameStateRepository.load(normalizedUsername)
          .orElseThrow(() -> new IllegalStateException("Saved game state not found for " + username + "."));
      Exchange exchange = gameStateMapper.restoreExchange(snapshot.exchange(), loadMarketData());
      Player player = gameStateMapper.restorePlayer(snapshot.player(), exchange);
      entries.add(toLeaderboardEntry(player));
    }
    return entries.stream()
        .sorted(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH))
        .toList();
  }

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

  private MarketData loadMarketData() {
    MarketData marketData = marketDataSupplier.get();
    if (marketData == null || marketData.isEmpty()) {
      throw new IllegalStateException("Could not load market data for profile session.");
    }
    return marketData;
  }

  private static void validateRegistrationInput(String username, char[] pin, BigDecimal startingMoney) {
    if (!ProfileDirectories.isValidUsername(username)) {
      throw new IllegalArgumentException(
          "Username must be 3-32 characters using letters, numbers, underscores, or hyphens.");
    }
    validatePin(pin);
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Starting money must be non-negative.");
    }
  }

  private static void validateLoginInput(String username, char[] pin) {
    if (!ProfileDirectories.isValidUsername(username)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    validatePin(pin);
  }

  private static void validatePin(char[] pin) {
    if (pin == null || pin.length < 4 || pin.length > 8) {
      throw new IllegalArgumentException("PIN must be 4 to 8 digits.");
    }
    for (char digit : pin) {
      if (!Character.isDigit(digit)) {
        throw new IllegalArgumentException("PIN must be 4 to 8 digits.");
      }
    }
  }
}
