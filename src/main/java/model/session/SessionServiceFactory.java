package model.session;


import model.session.auth.AuthService;
import model.session.game.GamePersistenceService;
import model.session.game.SavedRunService;
import model.session.profile.ProfilePreferencesService;
import model.session.profile.ProfileService;

import java.nio.file.Path;
import java.util.function.Supplier;
import model.persistence.game.GameStateMapper;
import model.persistence.game.GameStateRepository;
import model.persistence.market.MarketData;
import model.persistence.account.PinHashingService;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePreferencesRepository;
import model.persistence.savedrun.SavedRunMapper;
import model.persistence.savedrun.SavedRunRepository;
import model.persistence.account.UserAccountRepository;

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
