package model.session;

import java.math.BigDecimal;
import java.util.List;
import model.Exchange;
import model.Player;
import model.persistence.GameStateSnapshot;
import model.persistence.PinHashingService;
import model.persistence.ProfileDirectories;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;

/**
 * Handles user registration, authentication, and credential validation.
 */
public final class AuthService {

  private final UserAccountRepository userAccountRepository;
  private final PinHashingService pinHashingService;
  private final GamePersistenceService gamePersistenceService;

  /**
   * Creates an authentication service with the supplied dependencies.
   *
   * @param userAccountRepository  account metadata repository
   * @param pinHashingService      PIN hashing and verification
   * @param gamePersistenceService game state persistence for new and restored profiles
   */
  public AuthService(
      UserAccountRepository userAccountRepository,
      PinHashingService pinHashingService,
      GamePersistenceService gamePersistenceService) {
    this.userAccountRepository = userAccountRepository;
    this.pinHashingService = pinHashingService;
    this.gamePersistenceService = gamePersistenceService;
  }

  /**
   * Registers a new user profile with validated credentials and fresh game state.
   *
   * @param username      requested username
   * @param pin           numeric PIN
   * @param startingMoney initial cash balance
   * @return new active session for the registered profile
   * @throws DuplicateUsernameException if the username is already taken
   */
  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    validateRegistrationInput(username, pin, startingMoney);
    String normalizedUsername = ProfileDirectories.normalizeUsername(username);
    if (userAccountRepository.exists(normalizedUsername)) {
      throw new DuplicateUsernameException("That username is already registered.");
    }

    String trimmedUsername = username.trim();
    String saltBase64 = pinHashingService.generateSaltBase64();
    UserAccountRecord account = new UserAccountRecord(
        trimmedUsername,
        normalizedUsername,
        saltBase64,
        pinHashingService.hashPin(pin, saltBase64));

    Exchange exchange = gamePersistenceService.createFreshExchange();
    Player player = new Player(trimmedUsername, startingMoney);

    userAccountRepository.save(account);
    gamePersistenceService.saveSession(normalizedUsername, player, exchange);

    return new ActiveSession(trimmedUsername, normalizedUsername, player, exchange);
  }

  /**
   * Authenticates an existing user and restores their game state.
   *
   * @param username username to log in
   * @param pin      numeric PIN
   * @return active session for the authenticated user
   * @throws AuthenticationException if credentials are invalid
   */
  public ActiveSession login(String username, char[] pin) {
    validateLoginInput(username, pin);
    UserAccountRecord account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid username or PIN.");
    }

    GameStateSnapshot snapshot = gamePersistenceService.loadSnapshot(account.normalizedUsername())
        .orElseThrow(() -> new IllegalStateException(
            "Saved game state not found for " + account.username() + "."));
    Exchange exchange = gamePersistenceService.restoreExchange(snapshot.exchange());
    Player player = gamePersistenceService.restorePlayer(snapshot.player(), exchange);
    applyAccountDisplayName(account, player);
    return new ActiveSession(account.username(), account.normalizedUsername(), player, exchange);
  }

  /**
   * Lists all registered usernames alphabetically.
   *
   * @return sorted usernames
   */
  public List<String> listRegisteredUsers() {
    return userAccountRepository.listUsernames();
  }

  UserAccountRepository userAccountRepository() {
    return userAccountRepository;
  }

  static void validateRegistrationInput(String username, char[] pin, BigDecimal startingMoney) {
    if (!ProfileDirectories.isValidUsername(username)) {
      throw new IllegalArgumentException(
          "Username must be 3-32 characters using letters, numbers, underscores, or hyphens.");
    }
    validatePin(pin);
    if (startingMoney == null || startingMoney.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Starting money must be non-negative.");
    }
  }

  static void validateLoginInput(String username, char[] pin) {
    if (!ProfileDirectories.isValidUsername(username)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    validatePin(pin);
  }

  static void validatePin(char[] pin) {
    if (pin == null || pin.length < 4 || pin.length > 8) {
      throw new IllegalArgumentException("PIN must be 4 to 8 digits.");
    }
    for (char digit : pin) {
      if (!Character.isDigit(digit)) {
        throw new IllegalArgumentException("PIN must be 4 to 8 digits.");
      }
    }
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
}
