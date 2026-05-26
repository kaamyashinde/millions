package model.session;


import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.exception.profile.ProfileInUseException;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;
import model.session.auth.AuthService;
import model.session.leaderboard.LocalLeaderboardService;
import model.session.leaderboard.PlayerLeaderboardEntry;
import model.session.profile.ProfileService;

/**
 * Coordinates authentication, profile management, and persistence for the active session.
 *
 * <p>AI assistance note: Codex was used as inspiration when planning the responsibility split
 * for this class; the final implementation was reviewed and adapted by the group.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class SessionService {

  private final AuthService authService;
  private final ProfileService profileService;
  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;
  private final SessionPersistenceService sessionPersistenceService;
  private final WelcomePreferenceService welcomePreferenceService;
  private final ExitGameService exitGameService;
  private final SessionLeaderboardService sessionLeaderboardService;

  private ActiveSession activeSession;

  /**
   * Creates a session service from authentication, profile, and storage collaborators.
   *
   * @param authService authentication service
   * @param profileService profile metadata service
   * @param profilePaths profile path resolver
   * @param jsonStorage profile JSON storage
   */
  public SessionService(
      AuthService authService,
      ProfileService profileService,
      ProfilePaths profilePaths,
      JsonStorage jsonStorage) {
    this.authService = authService;
    this.profileService = profileService;
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
    this.sessionPersistenceService = new SessionPersistenceService(profilePaths, jsonStorage);
    this.welcomePreferenceService = new WelcomePreferenceService(profilePaths, jsonStorage);
    this.exitGameService = new ExitGameService(profileService);
    this.sessionLeaderboardService = new SessionLeaderboardService(authService);
  }

  /**
   * Registers a profile using default market data.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @param startingMoney starting cash balance
   * @return active session for the new profile
   */
  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    return register(username, pin, startingMoney, Optional.empty());
  }

  /**
   * Registers a profile using optional custom market data.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @param startingMoney starting cash balance
   * @param marketDataSource optional custom market-data CSV source
   * @return active session for the new profile
   */
  public ActiveSession register(
      String username,
      char[] pin,
      BigDecimal startingMoney,
      Optional<Path> marketDataSource) {
    saveActiveSession();
    activeSession = authService.register(username, pin, startingMoney, marketDataSource);
    return activeSession;
  }

  /**
   * Logs in to an existing profile.
   *
   * @param username username entered by the player
   * @param pin PIN entered by the player
   * @return active session for the restored profile
   */
  public ActiveSession login(String username, char[] pin) {
    ActiveSession newSession = authService.login(username, pin);
    if (activeSession != null
        && !activeSession.normalizedUsername().equals(newSession.normalizedUsername())) {
      saveActiveSession();
    }
    activeSession = newSession;
    return activeSession;
  }

  /**
   * Saves and clears the active session.
   *
   * @return {@code true} when a session was logged out
   */
  public boolean logout() {
    if (activeSession == null) {
      return false;
    }
    saveActiveSession();
    activeSession = null;
    return true;
  }

  /**
   * Persists the active session if one is loaded.
   */
  public void saveActiveSession() {
    sessionPersistenceService.save(activeSession);
  }

  /**
   * Returns whether a session is currently active.
   *
   * @return {@code true} when a profile is logged in
   */
  public boolean hasActiveSession() {
    return activeSession != null;
  }

  /**
   * Returns the active session, if any.
   *
   * @return optional active session
   */
  public Optional<ActiveSession> getActiveSession() {
    return Optional.ofNullable(activeSession);
  }

  /**
   * Lists registered local users.
   *
   * @return registered usernames
   */
  public List<String> listRegisteredUsers() {
    return authService.listRegisteredUsers();
  }

  /**
   * Lists leaderboard entries for saved profiles.
   *
   * @return entries sorted by net worth descending
   */
  public List<PlayerLeaderboardEntry> listLeaderboardEntries() {
    return sessionLeaderboardService.listEntries(activeSession);
  }

  /**
   * Returns whether the active profile has seen the welcome screen.
   *
   * @return {@code true} when welcome has already been acknowledged
   */
  public boolean hasSeenWelcome() {
    return welcomePreferenceService.hasSeenWelcome(requireActiveSession());
  }

  /**
   * Marks the active profile as having seen the welcome screen.
   */
  public void markWelcomeSeen() {
    saveActiveSession();
    welcomePreferenceService.markWelcomeSeen(requireActiveSession());
  }

  /**
   * Updates the active profile display name.
   *
   * @param displayName new display name
   */
  public void updateDisplayName(String displayName) {
    ActiveSession session = requireActiveSession();
    profileService.updateDisplayName(session, displayName);
  }

  /**
   * Saves a new avatar for the active profile.
   *
   * @param sourceImage source image chosen by the user
   */
  public void saveAvatarFromFile(Path sourceImage) {
    profileService.saveAvatarFromFile(sourceImage, requireActiveSession().normalizedUsername());
  }

  /**
   * Removes the active profile avatar.
   */
  public void clearAvatar() {
    profileService.clearAvatar(requireActiveSession().normalizedUsername());
  }

  /**
   * Deletes the active profile after PIN verification.
   *
   * @param pin PIN entered by the user
   */
  public void deleteActiveProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    String username = session.username();
    activeSession = null;
    profileService.deleteActiveProfile(username, pin);
  }

  /**
   * Liquidates all holdings, clears savings plans, deletes the active profile, and ends the
   * session.
   *
   * @param pin PIN confirming the action
   * @return summary of liquidation before profile removal
   */
  public ExitGameResult exitGameAndDeleteProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    ExitGameResult result = exitGameService.exitAndDelete(session, pin);
    activeSession = null;
    return result;
  }

  /**
   * Deletes an inactive profile after credential verification.
   *
   * @param username profile username
   * @param pin PIN entered by the user
   */
  public void deleteProfile(String username, char[] pin) {
    String normalized = ProfilePaths.normalizeUsername(username);
    if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
      throw new ProfileInUseException("Log out before deleting this profile.");
    }
    profileService.deleteOtherProfile(username, pin);
  }

  /**
   * Resolves a profile avatar path.
   *
   * @param normalizedUsername normalized profile username
   * @return avatar path for the profile
   */
  public Path avatarPath(String normalizedUsername) {
    return profileService.avatarPath(normalizedUsername);
  }

  /**
   * Creates a leaderboard service over the same profile storage.
   *
   * @return local leaderboard service
   */
  public LocalLeaderboardService leaderboardService() {
    return new LocalLeaderboardService(
        profilePaths,
        jsonStorage,
        authService.marketDataFileService());
  }

  private ActiveSession requireActiveSession() {
    if (activeSession == null) {
      throw new IllegalStateException("No active session.");
    }
    return activeSession;
  }

}
