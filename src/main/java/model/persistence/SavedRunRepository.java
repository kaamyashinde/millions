package model.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Persists multiple saved playthrough snapshots per user profile as separate JSON files.
 */
public final class SavedRunRepository {

  private final ProfileDirectories profileDirectories;
  private final JsonStorage jsonStorage;

  /**
   * Creates a repository rooted at the supplied profiles directory.
   *
   * @param profilesRoot base directory containing all user profiles
   */
  public SavedRunRepository(Path profilesRoot) {
    this.profileDirectories = new ProfileDirectories(profilesRoot);
    this.jsonStorage = new JsonStorage();
  }

  /**
   * Writes or overwrites one run file.
   *
   * @param username normalized username key
   * @param record   run snapshot to persist
   */
  public void save(String username, SavedRunRecord record) {
    Path path = profileDirectories.runFile(username, record.runId());
    jsonStorage.write(path, record);
  }

  /**
   * Lists all saved runs for the user, newest {@link SavedRunRecord#savedAt()} first.
   *
   * @param username raw or canonical username
   * @return sorted run snapshots
   */
  public List<SavedRunRecord> list(String username) {
    Path dir = profileDirectories.runsDirectory(username);
    if (!Files.isDirectory(dir)) {
      return List.of();
    }
    try (Stream<Path> stream = Files.list(dir)) {
      return stream
          .filter(p -> p.getFileName().toString().endsWith(".json"))
          .map(this::readOrThrow)
          .sorted(Comparator.comparing(SavedRunRecord::savedAt).reversed())
          .toList();
    } catch (IOException exception) {
      throw new PersistenceException("Could not list saved runs in " + dir, exception);
    }
  }

  /**
   * Loads one run by id.
   *
   * @param username raw or canonical username
   * @param runId    run uuid string
   * @return run when present
   */
  public Optional<SavedRunRecord> get(String username, UUID runId) {
    Path path = profileDirectories.runFile(username, runId.toString());
    if (!Files.exists(path)) {
      return Optional.empty();
    }
    return Optional.of(jsonStorage.read(path, SavedRunRecord.class));
  }

  /**
   * Deletes one run file when it exists.
   *
   * @param username raw or canonical username
   * @param runId    run id
   * @return {@code true} when a file was removed
   */
  public boolean delete(String username, UUID runId) {
    Path path = profileDirectories.runFile(username, runId.toString());
    try {
      return Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new PersistenceException("Could not delete saved run: " + path, exception);
    }
  }

  /**
   * Updates only the leaderboard eligibility flag for one run.
   *
   * @param username               profile key
   * @param runId                  run id
   * @param eligibleForLeaderboard new flag value
   * @return {@code true} when the run existed and was updated
   */
  public boolean updateLeaderboardFlag(String username, UUID runId, boolean eligibleForLeaderboard) {
    Optional<SavedRunRecord> existing = get(username, runId);
    if (existing.isEmpty()) {
      return false;
    }
    SavedRunRecord updated = withLeaderboardFlag(existing.get(), eligibleForLeaderboard);
    save(username, updated);
    return true;
  }

  private static SavedRunRecord withLeaderboardFlag(SavedRunRecord record, boolean eligible) {
    return new SavedRunRecord(
        record.schemaVersion(),
        record.runId(),
        record.savedAt(),
        record.label(),
        record.tradingDays(),
        record.cash(),
        record.holdings(),
        record.stats(),
        eligible);
  }

  private SavedRunRecord readOrThrow(Path path) {
    return jsonStorage.read(path, SavedRunRecord.class);
  }
}
