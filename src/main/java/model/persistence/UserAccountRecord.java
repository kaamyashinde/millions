package model.persistence;

/**
 * Persisted account metadata for one local profile.
 *
 * @param username display username shown in the CLI
 * @param normalizedUsername canonical case-insensitive username key
 * @param saltBase64 random salt used for PIN hashing
 * @param pinHashBase64 PBKDF2 hash of the PIN
 * @param displayName optional profile display name; when null, UI uses {@code username}
 */
public record UserAccountRecord(
    String username,
    String normalizedUsername,
    String saltBase64,
    String pinHashBase64,
    String displayName
) {

  /**
   * Account record without a custom display name (legacy and default).
   */
  public UserAccountRecord(
      String username,
      String normalizedUsername,
      String saltBase64,
      String pinHashBase64) {
    this(username, normalizedUsername, saltBase64, pinHashBase64, null);
  }
}
