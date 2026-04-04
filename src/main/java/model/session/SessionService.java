package model.session;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;
import model.Exchange;
import model.Player;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.GameStateSnapshot;
import model.persistence.MarketData;
import model.persistence.PersistenceException;
import model.persistence.PinHashingService;
import model.persistence.ProfileDirectories;
import model.persistence.ProfileImageService;
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
  private final ProfileImageService profileImageService;
  private final Path profilesRoot;

  private ActiveSession activeSession;

  /**
   * Creates a session service with repositories and a supplier for fresh market data.
   *
   * @param userAccountRepository account metadata repository
   * @param gameStateRepository saved game-state repository
   * @param pinHashingService PIN hashing helper
   * @param marketDataSupplier supplier that returns fresh bundled market data
   * @param exchangeName default exchange name used for new profiles
   * @param profilesRoot base directory containing all profile folders (same path as repositories)
   */
  public SessionService(
      UserAccountRepository userAccountRepository,
      GameStateRepository gameStateRepository,
      PinHashingService pinHashingService,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName,
      Path profilesRoot) {
    this.userAccountRepository = userAccountRepository;
    this.gameStateRepository = gameStateRepository;
    this.pinHashingService = pinHashingService;
    this.marketDataSupplier = marketDataSupplier;
    this.gameStateMapper = new GameStateMapper(exchangeName);
    this.profileImageService = new ProfileImageService(profilesRoot);
    this.profilesRoot = profilesRoot;
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
    applyAccountDisplayName(account, player);
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
   * Updates the display name for the active profile and persists account + game state.
   *
   * @param displayName new name, or blank to reset to login username
   */
  public void updateDisplayName(String displayName) {
    ActiveSession session = requireActiveSession();
    UserAccountRecord account = userAccountRepository.findByUsername(session.username())
        .orElseThrow(() -> new IllegalStateException("Account not found."));
    String trimmed = displayName == null ? "" : displayName.trim();
    String effective;
    String stored;
    if (trimmed.isEmpty()) {
      effective = account.username();
      stored = null;
    } else {
      effective = trimmed;
      stored = trimmed.equals(account.username()) ? null : trimmed;
    }
    session.player().setName(effective);
    UserAccountRecord updated = new UserAccountRecord(
        account.username(),
        account.normalizedUsername(),
        account.saltBase64(),
        account.pinHashBase64(),
        stored);
    userAccountRepository.save(updated);
    saveActiveSession();
  }

  /**
   * Copies an image file into the active profile as the avatar.
   *
   * @param sourceImage path to PNG or JPEG
   */
  public void saveAvatarFromFile(Path sourceImage) {
    ActiveSession session = requireActiveSession();
    profileImageService.saveAvatarFromFile(sourceImage, session.normalizedUsername());
  }

  /** Removes the avatar image for the active profile. */
  public void clearAvatar() {
    ActiveSession session = requireActiveSession();
    profileImageService.deleteAvatar(session.normalizedUsername());
  }

  /**
   * Deletes the active profile after PIN verification and clears the session.
   *
   * @param pin PIN for the current user
   */
  public void deleteActiveProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    UserAccountRecord account = userAccountRepository.findByUsername(session.username())
        .orElseThrow(() -> new IllegalStateException("Account not found."));
    validatePin(pin);
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid PIN.");
    }
    String normalized = session.normalizedUsername();
    activeSession = null;
    deleteProfileDirectory(profilesRoot.resolve(normalized));
  }

  /**
   * Deletes a profile after PIN verification. The profile must not be the active session.
   *
   * @param username profile login name
   * @param pin PIN
   * @throws ProfileInUseException when that user is logged in
   */
  public void deleteProfile(String username, char[] pin) {
    validateLoginInput(username, pin);
    String normalized = ProfileDirectories.normalizeUsername(username);
    UserAccountRecord account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
      throw new ProfileInUseException("Log out before deleting this profile.");
    }
    Path dir = profilesRoot.resolve(normalized);
    deleteProfileDirectory(dir);
  }

  /**
   * Resolves the avatar file path for a normalized username (file may be absent).
   */
  public Path avatarPath(String normalizedUsername) {
    return profileImageService.avatarPath(normalizedUsername);
  }

  /**
   * Builds a leaderboard view over all local profiles.
   */
  public LocalLeaderboardService leaderboardService() {
    return new LocalLeaderboardService(
        userAccountRepository,
        gameStateRepository,
        gameStateMapper,
        marketDataSupplier,
        profileImageService);
  }

  private ActiveSession requireActiveSession() {
    if (activeSession == null) {
      throw new IllegalStateException("No active session.");
    }
    return activeSession;
  }

  private static void applyAccountDisplayName(UserAccountRecord account, Player player) {
    if (account.displayName() != null && !account.displayName().isBlank()) {
      try {
        player.setName(account.displayName().trim());
      } catch (IllegalArgumentException ignored) {
        // keep snapshot name when stored value is invalid
      }
    }
  }

  private static void deleteProfileDirectory(Path dir) {
    if (!Files.exists(dir)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(dir)) {
      List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
      for (Path path : paths) {
        Files.deleteIfExists(path);
      }
    } catch (IOException exception) {
      throw new PersistenceException("Could not delete profile directory: " + dir, exception);
    }
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
