package model.session;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import model.Exchange;
import model.Player;
import model.persistence.GameStateSnapshot;
import model.persistence.ProfileDirectories;
import model.persistence.SavedRunRecord;

/**
 * Thin facade that coordinates sub-services and owns the active session lifecycle.
 *
 * <p>All public methods preserve the same signatures as the original monolithic service
 * so that view and CLI callers remain unchanged.
 */
public final class SessionService {

  private final AuthService authService;
  private final ProfileService profileService;
  private final GamePersistenceService gamePersistenceService;
  private final SavedRunService savedRunService;
  private final ProfilePreferencesService profilePreferencesService;

  private ActiveSession activeSession;

  /**
   * Creates a session service facade backed by five focused sub-services.
   *
   * @param authService               registration and login
   * @param profileService            display name, avatar, and profile deletion
   * @param gamePersistenceService    save/load game state snapshots
   * @param savedRunService           saved playthrough snapshots
   * @param profilePreferencesService per-profile UI preferences
   */
  public SessionService(
      AuthService authService,
      ProfileService profileService,
      GamePersistenceService gamePersistenceService,
      SavedRunService savedRunService,
      ProfilePreferencesService profilePreferencesService) {
    this.authService = authService;
    this.profileService = profileService;
    this.gamePersistenceService = gamePersistenceService;
    this.savedRunService = savedRunService;
    this.profilePreferencesService = profilePreferencesService;
  }

  /**
   * Registers a new user profile and makes it the active session.
   *
   * @param username      requested username
   * @param pin           numeric PIN
   * @param startingMoney initial cash balance
   * @return new active session
   */
  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    saveActiveSession();
    activeSession = authService.register(username, pin, startingMoney);
    return activeSession;
  }

  /**
   * Authenticates an existing profile and makes it the active session.
   *
   * @param username username to log in
   * @param pin      numeric PIN
   * @return active session for the loaded user
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
   * Persists the currently active session if one exists.
   */
  public void saveActiveSession() {
    if (activeSession == null) {
      return;
    }
    gamePersistenceService.saveSession(
        activeSession.normalizedUsername(),
        activeSession.player(),
        activeSession.exchange());
  }

  /**
   * Returns whether a user is currently logged in.
   *
   * @return {@code true} when an active session exists
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
   * Lists all registered usernames.
   *
   * @return alphabetically sorted usernames
   */
  public List<String> listRegisteredUsers() {
    return authService.listRegisteredUsers();
  }

  /**
   * Lists leaderboard entries for all saved profiles, using live in-memory state for the active
   * session when available.
   *
   * @return default-ranked leaderboard entries
   */
  public List<PlayerLeaderboardEntry> listLeaderboardEntries() {
    List<PlayerLeaderboardEntry> entries = new ArrayList<>();
    for (String username : authService.listRegisteredUsers()) {
      String normalizedUsername = ProfileDirectories.normalizeUsername(username);
      if (activeSession != null
          && activeSession.normalizedUsername().equals(normalizedUsername)) {
        entries.add(toLeaderboardEntry(activeSession.player()));
        continue;
      }

      GameStateSnapshot snapshot = gamePersistenceService.loadSnapshot(normalizedUsername)
          .orElseThrow(() -> new IllegalStateException(
              "Saved game state not found for " + username + "."));
      Exchange exchange = gamePersistenceService.restoreExchange(snapshot.exchange());
      Player player = gamePersistenceService.restorePlayer(snapshot.player(), exchange);
      entries.add(toLeaderboardEntry(player));
    }
    return entries.stream()
        .sorted(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH))
        .toList();
  }

  /**
   * Persists the active game, then saves a snapshot of the current run for later comparison.
   *
   * @param label optional name for the run
   * @return the persisted run record
   */
  public SavedRunRecord saveCurrentRun(String label) {
    ActiveSession session = requireActiveSession();
    saveActiveSession();
    return savedRunService.saveCurrentRun(
        session.normalizedUsername(), session.player(), session.exchange(), label);
  }

  /**
   * Lists saved runs for the logged-in profile (newest first).
   *
   * @return run snapshots
   */
  public List<SavedRunRecord> listSavedRuns() {
    return savedRunService.listSavedRuns(requireActiveSession().normalizedUsername());
  }

  /**
   * Deletes one saved run for the current profile.
   *
   * @param runId run identifier
   * @return {@code true} when a run file was removed
   */
  public boolean deleteSavedRun(UUID runId) {
    return savedRunService.deleteSavedRun(requireActiveSession().normalizedUsername(), runId);
  }

  /**
   * Updates leaderboard eligibility for one saved run.
   *
   * @param runId                  run identifier
   * @param eligibleForLeaderboard new value
   * @return {@code true} when the run existed and was updated
   */
  public boolean setRunLeaderboardEligible(UUID runId, boolean eligibleForLeaderboard) {
    return savedRunService.setRunLeaderboardEligible(
        requireActiveSession().normalizedUsername(), runId, eligibleForLeaderboard);
  }

  /**
   * Whether the current profile has already seen the welcome dialog.
   *
   * @return {@code true} when welcome was dismissed previously
   */
  public boolean hasSeenWelcome() {
    return profilePreferencesService.hasSeenWelcome(
        requireActiveSession().normalizedUsername());
  }

  /**
   * Marks the welcome dialog as seen for the current profile.
   */
  public void markWelcomeSeen() {
    profilePreferencesService.markWelcomeSeen(requireActiveSession().normalizedUsername());
  }

  /**
   * Updates the display name for the active profile and persists account + game state.
   *
   * @param displayName new name, or blank to reset to login username
   */
  public void updateDisplayName(String displayName) {
    ActiveSession session = requireActiveSession();
    profileService.updateDisplayName(session, displayName);
    saveActiveSession();
  }

  /**
   * Copies an image file into the active profile as the avatar.
   *
   * @param sourceImage path to PNG or JPEG
   */
  public void saveAvatarFromFile(Path sourceImage) {
    profileService.saveAvatarFromFile(sourceImage, requireActiveSession().normalizedUsername());
  }

  /** Removes the avatar image for the active profile. */
  public void clearAvatar() {
    profileService.clearAvatar(requireActiveSession().normalizedUsername());
  }

  /**
   * Deletes the active profile after PIN verification and clears the session.
   *
   * @param pin PIN for the current user
   */
  public void deleteActiveProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    String username = session.username();
    activeSession = null;
    profileService.deleteActiveProfile(username, pin);
  }

  /**
   * Deletes a profile after PIN verification. The profile must not be the active session.
   *
   * @param username profile login name
   * @param pin      PIN
   * @throws ProfileInUseException when that user is logged in
   */
  public void deleteProfile(String username, char[] pin) {
    String normalized = ProfileDirectories.normalizeUsername(username);
    if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
      throw new ProfileInUseException("Log out before deleting this profile.");
    }
    profileService.deleteOtherProfile(username, pin);
  }

  /**
   * Resolves the avatar file path for a normalized username (file may be absent).
   *
   * @param normalizedUsername profile directory key
   * @return path to the avatar file
   */
  public Path avatarPath(String normalizedUsername) {
    return profileService.avatarPath(normalizedUsername);
  }

  /**
   * Builds a leaderboard view over all local profiles.
   *
   * @return new leaderboard service instance
   */
  public LocalLeaderboardService leaderboardService() {
    return gamePersistenceService.createLeaderboardService(
        authService.userAccountRepository(),
        profileService.profileImageService());
  }

  private ActiveSession requireActiveSession() {
    if (activeSession == null) {
      throw new IllegalStateException("No active session.");
    }
    return activeSession;
  }

  private static PlayerLeaderboardEntry toLeaderboardEntry(Player player) {
    BigDecimal netWorth = player.getNetWorth();
    BigDecimal startingMoney = player.getStartingMoney();
    BigDecimal totalReturnPercent = BigDecimal.ZERO;
    if (startingMoney.compareTo(BigDecimal.ZERO) != 0) {
      totalReturnPercent = netWorth.subtract(startingMoney)
          .divide(startingMoney, 8, RoundingMode.HALF_UP);
    }
    return new PlayerLeaderboardEntry(player.getName(), netWorth, totalReturnPercent);
  }
}
