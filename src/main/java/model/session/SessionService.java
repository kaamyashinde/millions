package model.session;


import model.session.auth.AuthService;
import model.session.leaderboard.LocalLeaderboardService;
import model.session.leaderboard.PlayerLeaderboardEntry;
import model.session.leaderboard.PlayerLeaderboardMetric;
import model.session.leaderboard.PlayerLeaderboardRanking;
import model.exception.profile.ProfileInUseException;
import model.session.profile.ProfileService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import model.core.player.Player;
import model.persistence.ProfileFile;
import model.trading.transaction.Transaction;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;

/**
 * Coordinates authentication, profile management, and persistence for the active session.
 */
public final class SessionService {

  private final AuthService authService;
  private final ProfileService profileService;
  private final ProfilePaths profilePaths;
  private final JsonStorage jsonStorage;

  private ActiveSession activeSession;

  public SessionService(
      AuthService authService,
      ProfileService profileService,
      ProfilePaths profilePaths,
      JsonStorage jsonStorage) {
    this.authService = authService;
    this.profileService = profileService;
    this.profilePaths = profilePaths;
    this.jsonStorage = jsonStorage;
  }

  public ActiveSession register(String username, char[] pin, BigDecimal startingMoney) {
    return register(username, pin, startingMoney, Optional.empty());
  }

  public ActiveSession register(
      String username,
      char[] pin,
      BigDecimal startingMoney,
      Optional<Path> marketDataSource) {
    saveActiveSession();
    activeSession = authService.register(username, pin, startingMoney, marketDataSource);
    return activeSession;
  }

  public ActiveSession login(String username, char[] pin) {
    ActiveSession newSession = authService.login(username, pin);
    if (activeSession != null
        && !activeSession.normalizedUsername().equals(newSession.normalizedUsername())) {
      saveActiveSession();
    }
    activeSession = newSession;
    return activeSession;
  }

  public boolean logout() {
    if (activeSession == null) {
      return false;
    }
    saveActiveSession();
    activeSession = null;
    return true;
  }

  public void saveActiveSession() {
    if (activeSession == null) {
      return;
    }
    Path path = profilePaths.profileFile(activeSession.normalizedUsername());
    ProfileFile existing = jsonStorage.read(path, ProfileFile.class);
    ProfileFile updated = ProfileFile.capture(
        activeSession.player(),
        activeSession.exchange(),
        existing.username(),
        existing.normalizedUsername(),
        existing.pinHash(),
        existing.displayName(),
        existing.hasSeenWelcome());
    jsonStorage.write(path, updated);
  }

  public boolean hasActiveSession() {
    return activeSession != null;
  }

  public Optional<ActiveSession> getActiveSession() {
    return Optional.ofNullable(activeSession);
  }

  public List<String> listRegisteredUsers() {
    return authService.listRegisteredUsers();
  }

  public List<PlayerLeaderboardEntry> listLeaderboardEntries() {
    List<PlayerLeaderboardEntry> entries = new ArrayList<>();
    for (String username : authService.listRegisteredUsers()) {
      String normalized = ProfilePaths.normalizeUsername(username);
      if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
        entries.add(toLeaderboardEntry(activeSession.player()));
        continue;
      }
      ProfileFile profile = authService.loadProfileOrThrow(username);
      Player player = profile
          .restore(authService.marketDataFileService().loadForProfile(normalized))
          .player();
      if (profile.displayName() != null && !profile.displayName().isBlank()) {
        try {
          player.setName(profile.displayName().trim());
        } catch (IllegalArgumentException ignored) {
          // keep restored name
        }
      }
      entries.add(toLeaderboardEntry(player));
    }
    return entries.stream()
        .sorted(PlayerLeaderboardRanking.bestFirstComparator(PlayerLeaderboardMetric.NET_WORTH))
        .toList();
  }

  public boolean hasSeenWelcome() {
    ProfileFile profile = jsonStorage.read(
        profilePaths.profileFile(requireActiveSession().normalizedUsername()),
        ProfileFile.class);
    return profile.hasSeenWelcome();
  }

  public void markWelcomeSeen() {
    saveActiveSession();
    ActiveSession session = requireActiveSession();
    ProfileFile existing = jsonStorage.read(
        profilePaths.profileFile(session.normalizedUsername()), ProfileFile.class);
    jsonStorage.write(
        profilePaths.profileFile(session.normalizedUsername()),
        existing.withWelcomeSeen());
  }

  public void updateDisplayName(String displayName) {
    ActiveSession session = requireActiveSession();
    profileService.updateDisplayName(session, displayName);
  }

  public void saveAvatarFromFile(Path sourceImage) {
    profileService.saveAvatarFromFile(sourceImage, requireActiveSession().normalizedUsername());
  }

  public void clearAvatar() {
    profileService.clearAvatar(requireActiveSession().normalizedUsername());
  }

  public void deleteActiveProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    String username = session.username();
    activeSession = null;
    profileService.deleteActiveProfile(username, pin);
  }

  /**
   * Liquidates all holdings, clears savings plans, deletes the active profile, and ends the session.
   *
   * @param pin PIN confirming the action
   * @return summary of liquidation before profile removal
   */
  public ExitGameResult exitGameAndDeleteProfile(char[] pin) {
    ActiveSession session = requireActiveSession();
    String username = session.username();
    Player player = session.player();
    Set<String> symbols = new LinkedHashSet<>();
    player.getPortfolio().getShares().stream()
        .map(share -> share.getAsset().getSymbol())
        .forEach(symbols::add);
    int symbolsSold = symbols.size();
    profileService.verifyDeletionPin(username, pin);
    List<Transaction> transactions = session.exchange().sellAllHoldings(player);
    player.clearRegularSavingsPlans();
    BigDecimal finalCash = player.getMoney();
    activeSession = null;
    profileService.deleteProfileDirectory(username);
    return new ExitGameResult(symbolsSold, transactions.size(), finalCash);
  }

  public void deleteProfile(String username, char[] pin) {
    String normalized = ProfilePaths.normalizeUsername(username);
    if (activeSession != null && activeSession.normalizedUsername().equals(normalized)) {
      throw new ProfileInUseException("Log out before deleting this profile.");
    }
    profileService.deleteOtherProfile(username, pin);
  }

  public Path avatarPath(String normalizedUsername) {
    return profileService.avatarPath(normalizedUsername);
  }

  public LocalLeaderboardService leaderboardService() {
    return new LocalLeaderboardService(
        profilePaths,
        jsonStorage,
        authService.marketDataFileService(),
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
