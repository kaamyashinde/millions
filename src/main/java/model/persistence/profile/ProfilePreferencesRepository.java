package model.persistence.profile;


import model.persistence.io.JsonStorage;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes {@link ProfilePreferences} for one local profile.
 */
public final class ProfilePreferencesRepository {

  private final ProfileDirectories profileDirectories;
  private final JsonStorage jsonStorage;

  /**
   * @param profilesRoot base directory containing all user profiles
   */
  public ProfilePreferencesRepository(Path profilesRoot) {
    this.profileDirectories = new ProfileDirectories(profilesRoot);
    this.jsonStorage = new JsonStorage();
  }

  /**
   * Loads preferences or returns defaults when the file is absent.
   *
   * @param username raw or canonical username
   * @return preferences snapshot
   */
  public ProfilePreferences load(String username) {
    Path path = profileDirectories.preferencesFile(username);
    if (!Files.exists(path)) {
      return ProfilePreferences.initial();
    }
    return jsonStorage.read(path, ProfilePreferences.class);
  }

  /**
   * Persists preferences for one user.
   *
   * @param username    raw or canonical username
   * @param preferences preferences to store
   */
  public void save(String username, ProfilePreferences preferences) {
    jsonStorage.write(profileDirectories.preferencesFile(username), preferences);
  }
}
