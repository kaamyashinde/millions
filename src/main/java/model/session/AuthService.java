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
import model.session.validation.PinValidator;
import model.session.validation.RegistrationValidator;
import model.session.validation.StartingMoneyValidator;
import model.session.validation.UsernameValidator;
import model.session.validation.ValidationResult;

/**
 * Handles user registration, authentication, and credential validation.
 */
public final class AuthService {

  private static final RegistrationValidator REGISTRATION_CHAIN =
      new UsernameValidator().then(new PinValidator()).then(new StartingMoneyValidator());

  private static final RegistrationValidator LOGIN_CREDENTIALS =
      new UsernameValidator().then(new PinValidator());

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
   * @throws RegistrationValidationException if username, PIN, or starting money is invalid
   */
  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    ValidationResult registration = REGISTRATION_CHAIN.validate(username, pin, startingMoney);
    if (registration instanceof ValidationResult.Failure(var error)) {
      throw new RegistrationValidationException(error);
    }
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

  static void validateLoginInput(String username, char[] pin) {
    ValidationResult credentials = LOGIN_CREDENTIALS.validate(username, pin, null);
    if (credentials instanceof ValidationResult.Failure) {
      throw new AuthenticationException("Invalid username or PIN.");
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
