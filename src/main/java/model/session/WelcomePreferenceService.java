package model.session;

import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;

/**
 * Reads and updates profile-scoped welcome-screen preferences.
 */
final class WelcomePreferenceService {

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;

  /**
   * @param profilePaths profile path resolver
   * @param jsonStorage JSON persistence adapter
   */
  WelcomePreferenceService(ProfilePaths profilePaths, JsonStorage jsonStorage) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
  }

  /**
   * Checks whether the active profile has seen the welcome screen.
   *
   * @param session active session
   * @return {@code true} when the welcome flag is set
   */
  boolean hasSeenWelcome(ActiveSession session) {
    ProfileFile profile = jsonStorage.read(
        profilePaths.profileFile(session.normalizedUsername()),
        ProfileFile.class);
    return profile.hasSeenWelcome();
  }

  /**
   * Marks the active profile as having seen the welcome screen.
   *
   * @param session active session
   */
  void markWelcomeSeen(ActiveSession session) {
    ProfileFile existing = jsonStorage.read(
        profilePaths.profileFile(session.normalizedUsername()),
        ProfileFile.class);
    jsonStorage.write(
        profilePaths.profileFile(session.normalizedUsername()),
        existing.withWelcomeSeen());
  }
}
