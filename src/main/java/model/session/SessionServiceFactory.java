package model.session;

import java.nio.file.Path;
import java.util.function.Supplier;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfileImageService;
import model.persistence.ProfilePreferencesRepository;
import model.persistence.SavedRunMapper;
import model.persistence.SavedRunRepository;
import model.persistence.UserAccountRepository;

/**
 * Creates fully wired local-profile session services for application entry points.
 */
public final class SessionServiceFactory {

  private SessionServiceFactory() {
  }

  /**
   * Returns the default root directory used for local profile storage.
   *
   * @return profile storage root under the user's home directory
   */
  public static Path defaultProfilesRoot() {
    return Path.of(System.getProperty("user.home"), ".millions", "profiles");
  }

  /**
   * Creates a local-profile session service backed by JSON persistence.
   *
   * @param profilesRoot       base directory containing all user profiles
   * @param marketDataSupplier supplier for fresh bundled market data
   * @param exchangeName       exchange name used for newly created profiles
   * @return fully wired session service
   */
  public static SessionService createLocalProfileSessionService(
      Path profilesRoot,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    UserAccountRepository userAccountRepository = new UserAccountRepository(profilesRoot);
    PinHashingService pinHashingService = new PinHashingService();

    GamePersistenceService gamePersistenceService = new GamePersistenceService(
        new GameStateRepository(profilesRoot),
        new GameStateMapper(exchangeName),
        marketDataSupplier);

    AuthService authService = new AuthService(
        userAccountRepository, pinHashingService, gamePersistenceService);

    ProfileService profileService = new ProfileService(
        userAccountRepository,
        new ProfileImageService(profilesRoot),
        pinHashingService,
        profilesRoot);

    SavedRunService savedRunService = new SavedRunService(
        new SavedRunRepository(profilesRoot), new SavedRunMapper());

    ProfilePreferencesService profilePreferencesService = new ProfilePreferencesService(
        new ProfilePreferencesRepository(profilesRoot));

    return new SessionService(
        authService,
        profileService,
        gamePersistenceService,
        savedRunService,
        profilePreferencesService);
  }
}
