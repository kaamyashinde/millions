package model.session;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import model.Exchange;
import model.Player;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.GameStateSnapshot;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfileDirectories;
import model.persistence.ProfilePreferences;
import model.persistence.ProfilePreferencesRepository;
import model.persistence.SavedRunMapper;
import model.persistence.SavedRunRecord;
import model.persistence.SavedRunRepository;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;

/**
 * Coordinates registration, authentication, profile switching, and save/load operations.
 */
public final class SessionService {

  private final UserAccountRepository userAccountRepository;
  private final GameStateRepository gameStateRepository;
  private final SavedRunRepository savedRunRepository;
  private final ProfilePreferencesRepository profilePreferencesRepository;
  private final PinHashingService pinHashingService;
  private final Supplier<MarketData> marketDataSupplier;
  private final GameStateMapper gameStateMapper;
  private final SavedRunMapper savedRunMapper = new SavedRunMapper();

  private ActiveSession activeSession;

  /**
   * Creates a session service with repositories and a supplier for fresh market data.
   *
   * @param userAccountRepository account metadata repository
   * @param gameStateRepository saved game-state repository
   * @param savedRunRepository saved playthrough snapshots per profile
   * @param profilePreferencesRepository per-profile UI preferences
   * @param pinHashingService PIN hashing helper
   * @param marketDataSupplier supplier that returns fresh bundled market data
   * @param exchangeName default exchange name used for new profiles
   */
  public SessionService(
      UserAccountRepository userAccountRepository,
      GameStateRepository gameStateRepository,
      SavedRunRepository savedRunRepository,
      ProfilePreferencesRepository profilePreferencesRepository,
      PinHashingService pinHashingService,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    this.userAccountRepository = userAccountRepository;
    this.gameStateRepository = gameStateRepository;
    this.savedRunRepository = savedRunRepository;
    this.profilePreferencesRepository = profilePreferencesRepository;
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
   * Persists the active game, then saves a snapshot of the current run for later comparison.
   *
   * @param label optional name for the run
   * @return the persisted run record
   */
  public SavedRunRecord saveCurrentRun(String label) {
    ActiveSession session = requireActiveSession();
    saveActiveSession();
    SavedRunRecord record = savedRunMapper.toSavedRun(
        session.player(), session.exchange(), label, false);
    savedRunRepository.save(session.normalizedUsername(), record);
    return record;
  }

  /**
   * Lists saved runs for the logged-in profile (newest first).
   *
   * @return run snapshots
   */
  public List<SavedRunRecord> listSavedRuns() {
    ActiveSession session = requireActiveSession();
    return savedRunRepository.list(session.normalizedUsername());
  }

  /**
   * Deletes one saved run for the current profile.
   *
   * @param runId run identifier
   * @return {@code true} when a run file was removed
   */
  public boolean deleteSavedRun(UUID runId) {
    ActiveSession session = requireActiveSession();
    return savedRunRepository.delete(session.normalizedUsername(), runId);
  }

  /**
   * Updates leaderboard eligibility for one saved run.
   *
   * @param runId                  run identifier
   * @param eligibleForLeaderboard new value
   * @return {@code true} when the run existed and was updated
   */
  public boolean setRunLeaderboardEligible(UUID runId, boolean eligibleForLeaderboard) {
    ActiveSession session = requireActiveSession();
    return savedRunRepository.updateLeaderboardFlag(
        session.normalizedUsername(), runId, eligibleForLeaderboard);
  }

  /**
   * Whether the current profile has already seen the welcome dialog.
   *
   * @return {@code true} when welcome was dismissed previously
   */
  public boolean hasSeenWelcome() {
    ActiveSession session = requireActiveSession();
    return profilePreferencesRepository.load(session.normalizedUsername()).hasSeenWelcome();
  }

  /**
   * Marks the welcome dialog as seen for the current profile.
   */
  public void markWelcomeSeen() {
    ActiveSession session = requireActiveSession();
    profilePreferencesRepository.save(
        session.normalizedUsername(), new ProfilePreferences(true));
  }

  private ActiveSession requireActiveSession() {
    if (activeSession == null) {
      throw new IllegalStateException("No active session.");
    }
    return activeSession;
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
