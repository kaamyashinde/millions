package model.persistence.profile;


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
  private static final String RUNS_DIRECTORY_NAME = "runs";
  private static final String PREFERENCES_FILE_NAME = "preferences.json";
  private static final String AVATAR_FILE_NAME = "avatar.png";

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
   * Directory storing one JSON file per saved playthrough run.
   *
   * @param username raw or canonical username
   * @return {@code runs} directory under the profile
   */
  public Path runsDirectory(String username) {
    return profileDirectory(username).resolve(RUNS_DIRECTORY_NAME);
  }

  /**
   * Path to one saved run file.
   *
   * @param username raw or canonical username
   * @param runId    run identifier (UUID string, no extension)
   * @return path to the run JSON file named with the run id and {@code .json} extension
   */
  public Path runFile(String username, String runId) {
    return runsDirectory(username).resolve(runId + ".json");
  }

  /**
   * Per-profile UI preferences (e.g. welcome screen dismissed).
   *
   * @param username raw or canonical username
   * @return preferences JSON path
   */
  public Path preferencesFile(String username) {
    return profileDirectory(username).resolve(PREFERENCES_FILE_NAME);
  }

  /**
   * Returns the path for the profile avatar image (PNG, written on upload).
   *
   * @param username raw or canonical username
   * @return avatar file path within the profile directory
   */
  public Path avatarFile(String username) {
    return profileDirectory(username).resolve(AVATAR_FILE_NAME);
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
