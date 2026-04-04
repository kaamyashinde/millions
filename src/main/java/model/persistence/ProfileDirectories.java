package model.persistence;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Resolves profile file locations and applies username normalization rules.
 */
public final class ProfileDirectories {

  private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]{3,32}");
  private static final String ACCOUNT_FILE_NAME = "account.json";
  private static final String GAME_STATE_FILE_NAME = "game-state.json";

  private final Path profilesRoot;

  /**
   * Creates a resolver rooted at the supplied profiles directory.
   *
   * @param profilesRoot base directory containing all profile folders
   */
  public ProfileDirectories(Path profilesRoot) {
    this.profilesRoot = profilesRoot;
  }

  /**
   * Validates the user-facing username format.
   *
   * @param username raw username from the CLI
   * @return {@code true} when the username is allowed
   */
  public static boolean isValidUsername(String username) {
    return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
  }

  /**
   * Builds the canonical case-insensitive username key used for directories.
   *
   * @param username raw username
   * @return normalized key safe for file paths
   */
  public static String normalizeUsername(String username) {
    if (!isValidUsername(username)) {
      throw new IllegalArgumentException(
          "Username must be 3-32 characters using letters, numbers, underscores, or hyphens.");
    }
    return username.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Returns the directory that stores one user's files.
   *
   * @param username raw or canonical username
   * @return profile directory path
   */
  public Path profileDirectory(String username) {
    return profilesRoot.resolve(normalizeUsername(username));
  }

  /**
   * Returns the account metadata file path for one user.
   *
   * @param username raw or canonical username
   * @return account JSON path
   */
  public Path accountFile(String username) {
    return profileDirectory(username).resolve(ACCOUNT_FILE_NAME);
  }

  /**
   * Returns the saved game-state file path for one user.
   *
   * @param username raw or canonical username
   * @return game-state JSON path
   */
  public Path gameStateFile(String username) {
    return profileDirectory(username).resolve(GAME_STATE_FILE_NAME);
  }

  /**
   * Returns the root directory containing all profiles.
   *
   * @return profiles root path
   */
  public Path profilesRoot() {
    return profilesRoot;
  }
}
