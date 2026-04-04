package model.session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import model.persistence.PersistenceException;
import model.persistence.PinHashingService;
import model.persistence.ProfileImageService;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;

/**
 * Manages profile metadata: display names, avatars, and profile deletion.
 */
public final class ProfileService {

  private final UserAccountRepository userAccountRepository;
  private final ProfileImageService profileImageService;
  private final PinHashingService pinHashingService;
  private final Path profilesRoot;

  /**
   * Creates a profile service with account, image, and PIN dependencies.
   *
   * @param userAccountRepository account metadata repository
   * @param profileImageService   avatar image handling
   * @param pinHashingService     PIN verification
   * @param profilesRoot          base directory for all profile folders
   */
  public ProfileService(
      UserAccountRepository userAccountRepository,
      ProfileImageService profileImageService,
      PinHashingService pinHashingService,
      Path profilesRoot) {
    this.userAccountRepository = userAccountRepository;
    this.profileImageService = profileImageService;
    this.pinHashingService = pinHashingService;
    this.profilesRoot = profilesRoot;
  }

  /**
   * Updates the display name for a profile and applies it to the in-memory player.
   *
   * @param session     active session whose player name will be updated
   * @param displayName new name, or blank to reset to login username
   */
  public void updateDisplayName(ActiveSession session, String displayName) {
    UserAccountRecord account = userAccountRepository.findByUsername(session.username())
        .orElseThrow(() -> new IllegalStateException("Account not found."));
    String trimmed = displayName == null ? "" : displayName.trim();
    String effective;
    String stored;
    if (trimmed.isEmpty()) {
      effective = account.username();
      stored = null;
    } else {
      effective = trimmed;
      stored = trimmed.equals(account.username()) ? null : trimmed;
    }
    session.player().setName(effective);
    UserAccountRecord updated = new UserAccountRecord(
        account.username(),
        account.normalizedUsername(),
        account.saltBase64(),
        account.pinHashBase64(),
        stored);
    userAccountRepository.save(updated);
  }

  /**
   * Copies an image file into the profile directory as the avatar.
   *
   * @param sourceImage        path to PNG or JPEG
   * @param normalizedUsername profile directory key
   */
  public void saveAvatarFromFile(Path sourceImage, String normalizedUsername) {
    profileImageService.saveAvatarFromFile(sourceImage, normalizedUsername);
  }

  /**
   * Removes the avatar image for a profile.
   *
   * @param normalizedUsername profile directory key
   */
  public void clearAvatar(String normalizedUsername) {
    profileImageService.deleteAvatar(normalizedUsername);
  }

  /**
   * Resolves the avatar file path for a profile (file may be absent).
   *
   * @param normalizedUsername profile directory key
   * @return path to the avatar file
   */
  public Path avatarPath(String normalizedUsername) {
    return profileImageService.avatarPath(normalizedUsername);
  }

  /**
   * Verifies the PIN for the active profile and deletes its directory.
   *
   * @param username login username for the account
   * @param pin      PIN to verify
   * @throws AuthenticationException if the PIN is incorrect
   */
  public void deleteActiveProfile(String username, char[] pin) {
    AuthService.validatePin(pin);
    UserAccountRecord account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalStateException("Account not found."));
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid PIN.");
    }
    deleteProfileDirectory(profilesRoot.resolve(account.normalizedUsername()));
  }

  /**
   * Verifies credentials for a non-active profile and deletes its directory.
   *
   * @param username login username
   * @param pin      PIN to verify
   * @throws AuthenticationException if the username or PIN is incorrect
   */
  public void deleteOtherProfile(String username, char[] pin) {
    AuthService.validateLoginInput(username, pin);
    UserAccountRecord account = userAccountRepository.findByUsername(username)
        .orElseThrow(() -> new AuthenticationException("Invalid username or PIN."));
    if (!pinHashingService.verifyPin(pin, account.saltBase64(), account.pinHashBase64())) {
      throw new AuthenticationException("Invalid username or PIN.");
    }
    deleteProfileDirectory(profilesRoot.resolve(account.normalizedUsername()));
  }

  ProfileImageService profileImageService() {
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
