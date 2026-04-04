package model.session;

import java.nio.file.Path;
import java.util.function.Supplier;
import model.persistence.GameStateRepository;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfilePreferencesRepository;
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
   * @param profilesRoot base directory containing all user profiles
   * @param marketDataSupplier supplier for fresh bundled market data
   * @param exchangeName exchange name used for newly created profiles
   * @return fully wired session service
   */
  public static SessionService createLocalProfileSessionService(
      Path profilesRoot,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    return new SessionService(
        new UserAccountRepository(profilesRoot),
        new GameStateRepository(profilesRoot),
        new SavedRunRepository(profilesRoot),
        new ProfilePreferencesRepository(profilesRoot),
        new PinHashingService(),
        marketDataSupplier,
        exchangeName);
  }
}
