package model.persistence.game;


import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfileDirectories;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Stores and retrieves one saved game-state snapshot per user profile.
 */
public final class GameStateRepository {

  private final ProfileDirectories profileDirectories;
  private final JsonStorage jsonStorage;

  /**
   * Creates a game-state repository rooted at the supplied profiles directory.
   *
   * @param profilesRoot base directory containing all user profiles
   */
  public GameStateRepository(Path profilesRoot) {
    this.profileDirectories = new ProfileDirectories(profilesRoot);
    this.jsonStorage = new JsonStorage();
  }

  /**
   * Stores one user's game-state snapshot.
   *
   * @param username raw or canonical username
   * @param snapshot snapshot to persist
   */
  public void save(String username, GameStateSnapshot snapshot) {
    jsonStorage.write(profileDirectories.gameStateFile(username), snapshot);
  }

  /**
   * Loads one user's saved game-state snapshot.
   *
   * @param username raw or canonical username
   * @return saved snapshot, if present
   */
  public Optional<GameStateSnapshot> load(String username) {
    Path path = profileDirectories.gameStateFile(username);
    if (!java.nio.file.Files.exists(path)) {
      return Optional.empty();
    }
    return Optional.of(jsonStorage.read(path, GameStateSnapshot.class));
  }
}
