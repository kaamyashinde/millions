package model.session;


import model.session.auth.AuthService;
import model.session.profile.ProfileService;

import java.nio.file.Path;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketDataFileService;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;

/**
 * Creates fully wired local-profile session services for application entry points.
 */
public final class SessionServiceFactory {

  private SessionServiceFactory() {
  }

  /**
   * Returns the default local profile root.
   *
   * @return default profile directory under the user's home folder
   */
  public static Path defaultProfilesRoot() {
    return Path.of(System.getProperty("user.home"), ".millions", "profiles");
  }

  /**
   * Creates a local-profile session service with per-profile market-data files.
   *
   * @param profilesRoot              base directory containing all user profiles
   * @param defaultMarketDataResource classpath resource path for the default CSV
   * @param defaultResourceAnchor       class used to load the default CSV resource
   * @param exchangeName                exchange name used for newly created profiles
   * @return fully wired session service
   */
  public static SessionService createLocalProfileSessionService(
      Path profilesRoot,
      String defaultMarketDataResource,
      Class<?> defaultResourceAnchor,
      String exchangeName) {
    ProfilePaths profilePaths = new ProfilePaths(profilesRoot);
    JsonStorage jsonStorage = new JsonStorage();
    ProfileImageService profileImageService = new ProfileImageService(profilePaths);
    MarketDataFileService marketDataFileService = new MarketDataFileService(
        profilePaths, defaultResourceAnchor, defaultMarketDataResource);

    AuthService authService = new AuthService(
        profilePaths, jsonStorage, marketDataFileService, exchangeName);

    ProfileService profileService = new ProfileService(
        profilePaths, jsonStorage, profileImageService);

    return new SessionService(
        authService,
        profileService,
        profilePaths,
        jsonStorage);
  }
}
