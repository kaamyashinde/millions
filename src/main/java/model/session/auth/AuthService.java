package model.session.auth;


import model.exception.auth.AuthenticationException;
import model.exception.auth.DuplicateUsernameException;
import model.exception.auth.RegistrationValidationException;

import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.market.MarketDataFileService;
import model.persistence.profile.ProfilePaths;
import model.session.ActiveSession;
import model.session.validation.rules.PinValidator;
import model.session.validation.RegistrationValidator;
import model.session.validation.rules.StartingMoneyValidator;
import model.session.validation.rules.UsernameValidator;
import model.session.validation.ValidationResult;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.core.market.Exchange;
import model.core.player.Player;

/**
 * Handles user registration, authentication, and credential validation.
 *
 * <p>The service coordinates profile file paths, JSON persistence, market-data installation, and
 * session restoration. Validation is delegated to {@link RegistrationValidator} chains so
 * registration and login share the same input rules.
 *
 * <p>AI assistance note: Cursor was used as inspiration when planning the responsibility split
 * for this service; the final implementation was reviewed and adapted by the group.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class AuthService {

  private static final RegistrationValidator REGISTRATION_CHAIN =
      new UsernameValidator().then(new PinValidator()).then(new StartingMoneyValidator());

  private static final RegistrationValidator LOGIN_CREDENTIALS =
      new UsernameValidator().then(new PinValidator());

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final MarketDataFileService marketDataFileService;
  private final String exchangeName;

  /**
   * Creates an authentication service with the persistence collaborators it needs.
   *
   * @param profilePaths profile file and market-data path resolver
   * @param jsonStorage JSON storage abstraction for profile files
   * @param marketDataFileService market data installer and loader
   * @param exchangeName display name for newly created exchanges
   */
  public AuthService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      MarketDataFileService marketDataFileService,
      String exchangeName) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.marketDataFileService = marketDataFileService;
    this.exchangeName = exchangeName;
  }

  /**
   * Registers a new local profile and starts an active session for it.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @param startingMoney initial cash balance
   * @param marketDataSource optional custom market-data CSV source
   * @return active session for the newly registered profile
   * @throws RegistrationValidationException if the registration fields are invalid
   * @throws DuplicateUsernameException if the normalized username already exists
   */
  public ActiveSession register(
      String username,
      char[] pin,
      BigDecimal startingMoney,
      Optional<Path> marketDataSource) {
    ValidationResult registration = REGISTRATION_CHAIN.validate(username, pin, startingMoney);
    if (registration instanceof ValidationResult.Failure(var error)) {
      throw new RegistrationValidationException(error);
    }
    String normalizedUsername = ProfilePaths.normalizeUsername(username);
    if (profilePaths.profileExists(normalizedUsername)) {
      throw new DuplicateUsernameException("That username is already registered.");
    }

    String trimmedUsername = username.trim();
    String pinHash = ProfileFile.hashPin(normalizedUsername, pin);
    MarketData marketData = marketDataSource
        .map(source -> marketDataFileService.importFromFile(source, normalizedUsername))
        .orElseGet(() -> marketDataFileService.installDefault(normalizedUsername));
    Exchange exchange = ProfileFile.createFreshExchange(marketData, exchangeName);
    Player player = new Player(trimmedUsername, startingMoney);

    ProfileFile profile = ProfileFile.capture(
        player,
        exchange,
        trimmedUsername,
        normalizedUsername,
        pinHash,
        null,
        false);
    jsonStorage.write(profilePaths.profileFile(normalizedUsername), profile);

    return new ActiveSession(trimmedUsername, normalizedUsername, player, exchange);
  }

  /**
   * Restores an existing local profile after validating credentials.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @return restored active session
   * @throws AuthenticationException if credentials are invalid or the profile cannot be found
   */
  public ActiveSession login(String username, char[] pin) {
    validateLoginInput(username, pin);
    ProfileFile profile = loadProfile(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!profile.matchesPin(pin)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }

    MarketData marketData = marketDataFileService.loadForProfile(profile.normalizedUsername());
    ProfileFile.RestoredSession restored = profile.restore(marketData);
    applyDisplayName(profile, restored.player());
    return new ActiveSession(
        profile.username(), profile.normalizedUsername(), restored.player(), restored.exchange());
  }

  /**
   * Lists usernames for profiles saved on this machine.
   *
   * @return registered usernames from profile storage
   */
  public List<String> listRegisteredUsers() {
    return profilePaths.listUsernames(jsonStorage);
  }

  /**
   * Loads a profile or fails when it is missing.
   *
   * @param username profile username or normalized username
   * @return saved profile file
   * @throws IllegalStateException if the profile file is missing
   */
  public ProfileFile loadProfileOrThrow(String username) {
    return loadProfile(username)
        .orElseThrow(() -> new IllegalStateException("Profile not found: " + username));
  }

  /**
   * Returns the market data service used by this authentication service.
   *
   * @return market data installer and loader
   */
  public MarketDataFileService marketDataFileService() {
    return marketDataFileService;
  }

  /**
   * Validates login input before attempting to read profile files.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @throws AuthenticationException if either field is invalid
   */
  public static void validateLoginInput(String username, char[] pin) {
    ValidationResult credentials = LOGIN_CREDENTIALS.validate(username, pin, null);
    if (credentials instanceof ValidationResult.Failure) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
  }

  private java.util.Optional<ProfileFile> loadProfile(String username) {
    if (!profilePaths.profileExists(username)) {
      return java.util.Optional.empty();
    }
    return java.util.Optional.of(
        jsonStorage.read(profilePaths.profileFile(username), ProfileFile.class));
  }

  private static void applyDisplayName(ProfileFile profile, Player player) {
    if (profile.displayName() != null && !profile.displayName().isBlank()) {
      try {
        player.setName(profile.displayName().trim());
      } catch (IllegalArgumentException ignored) {
        // keep restored player name
      }
    }
  }
}
