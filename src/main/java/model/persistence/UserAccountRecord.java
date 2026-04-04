package model.persistence;

/**
 * Persisted account metadata for one local profile.
 *
 * @param username display username shown in the CLI
 * @param normalizedUsername canonical case-insensitive username key
 * @param saltBase64 random salt used for PIN hashing
 * @param pinHashBase64 PBKDF2 hash of the PIN
 */
public record UserAccountRecord(
    String username,
    String normalizedUsername,
    String saltBase64,
    String pinHashBase64
) {
}
