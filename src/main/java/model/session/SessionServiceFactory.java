package model.session;


import model.session.auth.AuthService;
import model.session.profile.ProfileService;

import java.nio.file.Path;
import java.util.function.Supplier;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;

/**
 * Creates fully wired local-profile session services for application entry points.
 */
public final class SessionServiceFactory {

  private SessionServiceFactory() {
  }

  public static Path defaultProfilesRoot() {
    return Path.of(System.getProperty("user.home"), ".millions", "profiles");
  }

  public static SessionService createLocalProfileSessionService(
      Path profilesRoot,
      Supplier<MarketData> marketDataSupplier,
      String exchangeName) {
    ProfilePaths profilePaths = new ProfilePaths(profilesRoot);
    JsonStorage jsonStorage = new JsonStorage();
    ProfileImageService profileImageService = new ProfileImageService(profilePaths);

    AuthService authService = new AuthService(
        profilePaths, jsonStorage, marketDataSupplier, exchangeName);

    ProfileService profileService = new ProfileService(
        profilePaths, jsonStorage, profileImageService);

    return new SessionService(
        authService,
        profileService,
        profilePaths,
        jsonStorage,
        marketDataSupplier);
  }
}
