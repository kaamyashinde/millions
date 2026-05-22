package model.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Stores and retrieves local profile account metadata.
 */
public final class UserAccountRepository {

  private final ProfileDirectories profileDirectories;
  private final JsonStorage jsonStorage;

  /**
   * Creates an account repository rooted at the supplied profiles directory.
   *
   * @param profilesRoot base directory containing all user profiles
   */
  public UserAccountRepository(Path profilesRoot) {
    this.profileDirectories = new ProfileDirectories(profilesRoot);
    this.jsonStorage = new JsonStorage();
  }

  /**
   * Returns whether an account exists for the supplied username.
   *
   * @param username raw username from the CLI
   * @return {@code true} when an account file exists
   */
  public boolean exists(String username) {
    return Files.exists(profileDirectories.accountFile(username));
  }

  /**
   * Stores the supplied account metadata.
   *
   * @param account account record to write
   */
  public void save(UserAccountRecord account) {
    jsonStorage.write(profileDirectories.accountFile(account.normalizedUsername()), account);
  }

  /**
   * Loads one account by username.
   *
   * @param username raw username from the CLI
   * @return matching account, if present
   */
  public Optional<UserAccountRecord> findByUsername(String username) {
    Path accountFile = profileDirectories.accountFile(username);
    if (!Files.exists(accountFile)) {
      return Optional.empty();
    }
    return Optional.of(jsonStorage.read(accountFile, UserAccountRecord.class));
  }

  /**
   * Lists all registered usernames sorted alphabetically.
   *
   * @return stored usernames
   */
  public List<String> listUsernames() {
    return listProfiles(UserAccountRecord::username, Comparator.naturalOrder());
  }

  /**
   * Loads all account records sorted by username.
   *
   * @return account metadata for every profile on disk
   */
  public List<UserAccountRecord> listAccounts() {
    return listProfiles(Function.identity(), Comparator.comparing(UserAccountRecord::username));
  }

  /**
   * Walks the profiles directory, deserializes each account, maps it through
   * the supplied function, and returns the results sorted by the given comparator.
   *
   * @param <T>        the target element type
   * @param mapper     transforms a deserialized account into the desired result type
   * @param comparator ordering applied to the mapped results
   * @return sorted list of mapped profile entries, or an empty list if the root does not exist
   * @throws PersistenceException if the directory cannot be listed
   */
  private <T> List<T> listProfiles(Function<UserAccountRecord, T> mapper,
      Comparator<T> comparator) {
    Path root = profileDirectories.profilesRoot();
    if (!Files.exists(root)) {
      return List.of();
    }
    try (Stream<Path> children = Files.list(root)) {
      return children
          .filter(Files::isDirectory)
          .map(path -> path.resolve("account.json"))
          .filter(Files::exists)
          .map(path -> jsonStorage.read(path, UserAccountRecord.class))
          .map(mapper)
          .sorted(comparator)
          .toList();
    } catch (IOException exception) {
      throw new PersistenceException("Could not list user profiles in " + root, exception);
    }
  }
}
