package model.persistence.profile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;

/**
 * Resolves profile directory paths and username normalization.
 */
public final class ProfilePaths {

  private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{3,32}");
  private static final String PROFILE_FILE_NAME = "profile.json";
  private static final String AVATAR_FILE_NAME = "avatar.png";
  private static final String MARKET_DATA_FILE_NAME = "market-data.csv";

  private final Path profilesRoot;

  /**
   * Creates a profile path resolver.
   *
   * @param profilesRoot root directory containing profile folders
   */
  public ProfilePaths(Path profilesRoot) {
    this.profilesRoot = profilesRoot;
  }

  /**
   * Checks whether a username satisfies profile naming rules.
   *
   * @param username username to validate
   * @return {@code true} when the username can be used for a profile
   */
  public static boolean isValidUsername(String username) {
    return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
  }

  /**
   * Normalizes a username for file-system paths.
   *
   * @param username username entered by the user
   * @return lowercase normalized username
   * @throws IllegalArgumentException if the username is invalid
   */
  public static String normalizeUsername(String username) {
    if (!isValidUsername(username)) {
      throw new IllegalArgumentException(
          "Username must be 3-32 characters using letters, numbers, underscores, or hyphens.");
    }
    return username.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Returns the root profile directory.
   *
   * @return profile root path
   */
  public Path profilesRoot() {
    return profilesRoot;
  }

  /**
   * Resolves a profile directory.
   *
   * @param username raw or normalized username
   * @return profile directory path
   */
  public Path profileDirectory(String username) {
    return profilesRoot.resolve(normalizeUsername(username));
  }

  /**
   * Resolves a profile JSON file.
   *
   * @param username raw or normalized username
   * @return profile JSON path
   */
  public Path profileFile(String username) {
    return profileDirectory(username).resolve(PROFILE_FILE_NAME);
  }

  /**
   * Resolves a profile avatar file.
   *
   * @param username raw or normalized username
   * @return avatar PNG path
   */
  public Path avatarFile(String username) {
    return profileDirectory(username).resolve(AVATAR_FILE_NAME);
  }

  /**
   * Per-profile market data CSV (stocks and funds) used when creating and restoring sessions.
   *
   * @param username raw or canonical username
   * @return path to {@code market-data.csv} in the profile directory
   */
  public Path marketDataFile(String username) {
    return profileDirectory(username).resolve(MARKET_DATA_FILE_NAME);
  }

  /**
   * Checks whether a profile JSON file exists.
   *
   * @param username raw or normalized username
   * @return {@code true} when the profile exists
   */
  public boolean profileExists(String username) {
    return Files.exists(profileFile(username));
  }

  /**
   * Lists saved profile usernames.
   *
   * @param jsonStorage storage used to read profile metadata
   * @return sorted usernames for valid profile directories
   */
  public List<String> listUsernames(JsonStorage jsonStorage) {
    if (!Files.isDirectory(profilesRoot)) {
      return List.of();
    }
    List<String> names = new ArrayList<>();
    try (Stream<Path> children = Files.list(profilesRoot)) {
      for (Path dir : children.filter(Files::isDirectory).sorted().toList()) {
        if (!isValidUsername(dir.getFileName().toString())) {
          continue;
        }
        Path file = dir.resolve(PROFILE_FILE_NAME);
        if (!Files.exists(file)) {
          continue;
        }
        ProfileFile profile = jsonStorage.read(file, ProfileFile.class);
        names.add(profile.username());
      }
    } catch (IOException exception) {
      throw new PersistenceException("Could not list profiles in " + profilesRoot, exception);
    }
    return names.stream().sorted(Comparator.naturalOrder()).toList();
  }
}
