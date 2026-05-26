package model.session;

import java.nio.file.Path;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;

/**
 * Persists the mutable game state for an active session.
 */
final class SessionPersistenceService {

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;

  /**
   * @param profilePaths profile path resolver
   * @param jsonStorage JSON persistence adapter
   */
  SessionPersistenceService(ProfilePaths profilePaths, JsonStorage jsonStorage) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
  }

  /**
   * Saves the player's current state while preserving profile metadata.
   *
   * @param session active session to save
   */
  void save(ActiveSession session) {
    if (session == null) {
      return;
    }
    Path path = profilePaths.profileFile(session.normalizedUsername());
    ProfileFile existing = jsonStorage.read(path, ProfileFile.class);
    ProfileFile updated = ProfileFile.capture(
        session.player(),
        session.exchange(),
        existing.username(),
        existing.normalizedUsername(),
        existing.pinHash(),
        existing.displayName(),
        existing.hasSeenWelcome());
    jsonStorage.write(path, updated);
  }
}
