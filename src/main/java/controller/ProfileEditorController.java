package controller;

import static util.Validator.checkNotNull;

import java.nio.file.Path;
import model.exception.auth.AuthenticationException;
import model.session.ActiveSession;
import model.session.SessionService;

/**
 * Handles profile display name, avatar, and deletion for the profile editor dialog.
 *
 * <p>This controller keeps {@link view.dialogs.ProfileEditorDialog} focused on JavaFX controls
 * while profile state changes remain delegated to {@link SessionService}.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-03
 */
public class ProfileEditorController {

  private final SessionService sessionService;

  /**
   * Creates a profile editor controller.
   *
   * @param sessionService session API for profile operations
   */
  public ProfileEditorController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  /**
   * Exposes the session service used for profile operations.
   *
   * @return session API used for profile operations
   */
  public SessionService getSessionService() {
    return sessionService;
  }

  /**
   * Returns the active session or fails if there is none.
   *
   * @return active session
   * @throws IllegalStateException if no profile is currently active
   */
  public ActiveSession requireActiveSession() {
    return sessionService.getActiveSession()
        .orElseThrow(() -> new IllegalStateException("No active session."));
  }

  /**
   * Resolves the avatar path for a normalized username.
   *
   * @param normalizedUsername username normalized for persistence
   * @return path where the avatar image is stored
   */
  public Path avatarPath(String normalizedUsername) {
    return sessionService.avatarPath(normalizedUsername);
  }

  /**
   * Updates the active profile display name.
   *
   * @param displayName new display name
   */
  public void updateDisplayName(String displayName) {
    sessionService.updateDisplayName(displayName);
  }

  /**
   * Stores a new avatar for the active profile.
   *
   * @param sourceImage source image chosen by the user
   */
  public void saveAvatarFromFile(Path sourceImage) {
    sessionService.saveAvatarFromFile(sourceImage);
  }

  /**
   * Removes the active profile avatar.
   */
  public void clearAvatar() {
    sessionService.clearAvatar();
  }

  /**
   * Deletes the active profile after PIN confirmation.
   *
   * @param pin PIN entered by the user
   * @throws AuthenticationException if the PIN is wrong
   */
  public void deleteActiveProfile(char[] pin) throws AuthenticationException {
    sessionService.deleteActiveProfile(pin);
  }
}
