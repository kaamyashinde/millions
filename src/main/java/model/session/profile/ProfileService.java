package model.session.profile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import model.exception.auth.AuthenticationException;
import model.exception.auth.RegistrationValidationException;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;
import model.session.ActiveSession;
import model.session.auth.AuthService;
import model.session.validation.ValidationResult;
import model.session.validation.rules.PinValidator;

/**
 * Manages profile metadata: display names, avatars, and profile deletion.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class ProfileService {

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final ProfileImageService profileImageService;

  /**
   * Creates a profile service.
   *
   * @param profilePaths profile path resolver
   * @param jsonStorage profile JSON storage
   * @param profileImageService avatar image service
   */
  public ProfileService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      ProfileImageService profileImageService) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.profileImageService = profileImageService;
  }

  /**
   * Updates the display name for the active session and persisted profile.
   *
   * @param session active session to update
   * @param displayName new display name, or blank to reset to username
   */
  public void updateDisplayName(ActiveSession session, String displayName) {
    ProfileFile existing = jsonStorage.read(
        profilePaths.profileFile(session.normalizedUsername()), ProfileFile.class);
    String trimmed = displayName == null ? "" : displayName.trim();
    String effective;
    String stored;
    if (trimmed.isEmpty()) {
      effective = existing.username();
      stored = null;
    } else {
      effective = trimmed;
      stored = trimmed.equals(existing.username()) ? null : trimmed;
    }
    session.player().setName(effective);
    ProfileFile saved = ProfileFile.capture(
        session.player(),
        session.exchange(),
        existing.username(),
        existing.normalizedUsername(),
        existing.pinHash(),
        stored,
        existing.hasSeenWelcome());
    jsonStorage.write(profilePaths.profileFile(session.normalizedUsername()), saved);
  }

  /**
   * Saves a new avatar image for a profile.
   *
   * @param sourceImage source image chosen by the user
   * @param normalizedUsername normalized profile username
   */
  public void saveAvatarFromFile(Path sourceImage, String normalizedUsername) {
    profileImageService.saveAvatarFromFile(sourceImage, normalizedUsername);
  }

  /**
   * Deletes a profile avatar.
   *
   * @param normalizedUsername normalized profile username
   */
  public void clearAvatar(String normalizedUsername) {
    profileImageService.deleteAvatar(normalizedUsername);
  }

  /**
   * Resolves a profile avatar path.
   *
   * @param normalizedUsername normalized profile username
   * @return avatar image path
   */
  public Path avatarPath(String normalizedUsername) {
    return profileImageService.avatarPath(normalizedUsername);
  }

  /**
   * Deletes the active profile after PIN verification.
   *
   * @param username active profile username
   * @param pin PIN entered by the user
   */
  public void deleteActiveProfile(String username, char[] pin) {
    verifyDeletionPin(username, pin);
    deleteProfileDirectory(username);
  }

  /**
   * Validates PIN format and matches the stored profile PIN before deletion.
   *
   * @param username profile username (normalized path key)
   * @param pin PIN entered by the user
   * @throws RegistrationValidationException if PIN format is invalid
   * @throws AuthenticationException if PIN does not match
   */
  public void verifyDeletionPin(String username, char[] pin) {
    ValidationResult pinFormat = new PinValidator().validate("", pin, null);
    if (pinFormat instanceof ValidationResult.Failure(var error)) {
      throw new RegistrationValidationException(error);
    }
    ProfileFile profile = jsonStorage.read(
        profilePaths.profileFile(username), ProfileFile.class);
    if (!profile.matchesPin(pin)) {
      throw new AuthenticationException("Invalid PIN.");
    }
  }

  /**
   * Deletes the on-disk profile directory for the given username without PIN checks.
   *
   * @param username profile username (normalized path key)
   */
  public void deleteProfileDirectory(String username) {
    deleteProfileDirectory(profilePaths.profileDirectory(username));
  }

  private static void deleteProfileDirectory(Path dir) {
    if (!Files.exists(dir)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(dir)) {
      List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
      for (Path path : paths) {
        Files.deleteIfExists(path);
      }
    } catch (IOException exception) {
      throw new PersistenceException("Could not delete profile directory: " + dir, exception);
    }
  }

  /**
   * Deletes a non-active profile after username and PIN verification.
   *
   * @param username profile username
   * @param pin PIN entered by the user
   */
  public void deleteOtherProfile(String username, char[] pin) {
    AuthService.validateLoginInput(username, pin);
    if (!profilePaths.profileExists(username)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    ProfileFile profile = jsonStorage.read(profilePaths.profileFile(username), ProfileFile.class);
    if (!profile.matchesPin(pin)) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    deleteProfileDirectory(profilePaths.profileDirectory(username));
  }

  /**
   * Returns the avatar image service.
   *
   * @return profile image service
   */
  public ProfileImageService profileImageService() {
    return profileImageService;
  }
}
