package model.session.profile;


import model.session.ActiveSession;
import model.session.auth.AuthService;
import model.exception.auth.AuthenticationException;
import model.exception.auth.RegistrationValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import model.exception.persistence.PersistenceException;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;
import model.session.validation.rules.PinValidator;
import model.session.validation.ValidationResult;

/**
 * Manages profile metadata: display names, avatars, and profile deletion.
 */
public final class ProfileService {

  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final ProfileImageService profileImageService;

  public ProfileService(
      ProfilePaths profilePaths,
      JsonStorage jsonStorage,
      ProfileImageService profileImageService) {
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.profileImageService = profileImageService;
  }

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

  public void saveAvatarFromFile(Path sourceImage, String normalizedUsername) {
    profileImageService.saveAvatarFromFile(sourceImage, normalizedUsername);
  }

  public void clearAvatar(String normalizedUsername) {
    profileImageService.deleteAvatar(normalizedUsername);
  }

  public Path avatarPath(String normalizedUsername) {
    return profileImageService.avatarPath(normalizedUsername);
  }

  public void deleteActiveProfile(String username, char[] pin) {
    ValidationResult pinFormat = new PinValidator().validate("", pin, null);
    if (pinFormat instanceof ValidationResult.Failure(var error)) {
      throw new RegistrationValidationException(error);
    }
    ProfileFile profile = jsonStorage.read(
        profilePaths.profileFile(username), ProfileFile.class);
    if (!profile.matchesPin(pin)) {
      throw new AuthenticationException("Invalid PIN.");
    }
    deleteProfileDirectory(profilePaths.profileDirectory(username));
  }

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

  public ProfileImageService profileImageService() {
    return profileImageService;
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
}
