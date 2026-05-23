package model.session.auth;


import model.exception.auth.AuthenticationException;
import model.exception.auth.DuplicateUsernameException;
import model.exception.auth.RegistrationValidationException;

import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.profile.ProfilePaths;
import model.session.ActiveSession;
import model.session.validation.rules.PinValidator;
import model.session.validation.RegistrationValidator;
import model.session.validation.rules.StartingMoneyValidator;
import model.session.validation.rules.UsernameValidator;
import model.session.validation.ValidationResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import model.core.market.Exchange;
import model.core.player.Player;

/**
 * Handles user registration, authentication, and credential validation.
 */
public final class AuthService {

  private static final RegistrationValidator REGISTRATION_CHAIN =
      new UsernameValidator().then(new PinValidator()).then(new StartingMoneyValidator());

  private static final RegistrationValidator LOGIN_CREDENTIALS =
      new UsernameValidator().then(new PinValidator());

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final Supplier<MarketData> marketDataSupplier;
  private final String exchangeName;

  public AuthService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.marketDataSupplier = marketDataSupplier;
    this.exchangeName = exchangeName;
  }

  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
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
    MarketData marketData = requireMarketData();
    Exchange exchange = ProfileFile.createFreshExchange(marketData, exchangeName);
    Player player = new Player(trimmedUsername, startingMoney);

    ProfileFile profile = ProfileFile.capture(
        player,
        exchange,
        trimmedUsername,
        normalizedUsername,
        pinHash,
        null,
        false,
        List.of());
    jsonStorage.write(profilePaths.profileFile(normalizedUsername), profile);

    return new ActiveSession(trimmedUsername, normalizedUsername, player, exchange);
  }

  public ActiveSession login(String username, char[] pin) {
    validateLoginInput(username, pin);
    ProfileFile profile = loadProfile(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!profile.matchesPin(pin)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }

    ProfileFile.RestoredSession restored = profile.restore(requireMarketData());
    applyDisplayName(profile, restored.player());
    return new ActiveSession(
        profile.username(), profile.normalizedUsername(), restored.player(), restored.exchange());
  }

  public List<String> listRegisteredUsers() {
    return profilePaths.listUsernames(jsonStorage);
  }

  public ProfileFile loadProfileOrThrow(String username) {
    return loadProfile(username)
        .orElseThrow(() -> new IllegalStateException("Profile not found: " + username));
  }

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

  private MarketData requireMarketData() {
    MarketData marketData = marketDataSupplier.get();
    if (marketData == null || marketData.isEmpty()) {
      throw new IllegalStateException("Could not load market data for profile session.");
    }
    return marketData;
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
