package controller;

import static model.utils.Validator.checkNotNull;

import java.nio.file.Path;
import model.session.ActiveSession;
import model.exception.auth.AuthenticationException;
import model.session.SessionService;

/**
 * Handles profile display name, avatar, and deletion for the profile editor dialog.
 */
public class ProfileEditorController {

  private final SessionService sessionService;

  /**
   * @param sessionService session API for profile operations
   */
  public ProfileEditorController(SessionService sessionService) {
    checkNotNull(sessionService, "sessionService");
    this.sessionService = sessionService;
  }

  public SessionService getSessionService() {
    return sessionService;
  }

  public ActiveSession requireActiveSession() {
    return sessionService.getActiveSession()
        .orElseThrow(() -> new IllegalStateException("No active session."));
  }

  public Path avatarPath(String normalizedUsername) {
    return sessionService.avatarPath(normalizedUsername);
  }

  public void updateDisplayName(String displayName) {
    sessionService.updateDisplayName(displayName);
  }

  public void saveAvatarFromFile(Path sourceImage) {
    sessionService.saveAvatarFromFile(sourceImage);
  }

  public void clearAvatar() {
    sessionService.clearAvatar();
  }

  public void deleteActiveProfile(char[] pin) throws AuthenticationException {
    sessionService.deleteActiveProfile(pin);
  }
}
