package model.session;


import model.exception.auth.AuthenticationException;
import model.exception.auth.DuplicateUsernameException;
import model.exception.auth.RegistrationValidationException;
import model.exception.profile.ProfileInUseException;
import model.session.leaderboard.PlayerLeaderboardEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;
import model.session.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration tests for {@link SessionService} profile and session persistence on disk.
 * JSON parsing unit tests belong in {@link model.persistence.io.JsonProfileReaderWriterTest}.
 */
class SessionServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void register_rejectsDuplicateUsernameIgnoringCase() {
    SessionService sessionService = createSessionService();

    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));

    assertThrows(
        DuplicateUsernameException.class,
        () -> sessionService.register("alice", "5678".toCharArray(), new BigDecimal("500.00")));
  }

  @Test
  void register_rejectsInvalidUsernameWithTypedValidationError() {
    SessionService sessionService = createSessionService();

    RegistrationValidationException thrown = assertThrows(
        RegistrationValidationException.class,
        () -> sessionService.register("ab", "1234".toCharArray(), new BigDecimal("100.00")));

    assertEquals(ValidationError.INVALID_USERNAME, thrown.error());
  }

  @Test
  void login_rejectsWrongPin() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.logout();

    assertThrows(
        AuthenticationException.class,
        () -> sessionService.login("Alice", "9999".toCharArray()));
  }

  @Test
  void login_rejectsInvalidInputAndMissingProfiles() {
    SessionService sessionService = createSessionService();

    assertThrows(AuthenticationException.class, () -> sessionService.login("ab", "12".toCharArray()));
    assertThrows(AuthenticationException.class, () -> sessionService.login("Missing", "1234".toCharArray()));
  }

  @Test
  void login_sameActiveUserKeepsSessionAndReturnsLoadedProfile() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));

    ActiveSession loggedIn = sessionService.login("Alice", "1234".toCharArray());

    assertEquals("Alice", loggedIn.username());
    assertTrue(sessionService.hasActiveSession());
    assertTrue(sessionService.getActiveSession().isPresent());
  }

  @Test
  void logoutAndSaveActiveSession_areNoopsWithoutActiveSession() {
    SessionService sessionService = createSessionService();

    assertFalse(sessionService.logout());
    sessionService.saveActiveSession();
    assertFalse(sessionService.hasActiveSession());
    assertTrue(sessionService.getActiveSession().isEmpty());
    assertEquals(List.of(), sessionService.listRegisteredUsers());
  }

  @Test
  void switchingUsers_restoresCorrectSavedStateWithoutLeakage() {
    SessionService sessionService = createSessionService();

    ActiveSession alice = sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    alice.exchange().buy("AAPL", new BigDecimal("1.0"), alice.player());
    alice.exchange().advance();
    sessionService.saveActiveSession();

    ActiveSession bob = sessionService.register("Bob", "5678".toCharArray(), new BigDecimal("2000.00"));
    bob.player().addMoney(new BigDecimal("25.00"));
    bob.exchange().advance(2);
    sessionService.saveActiveSession();

    ActiveSession reloadedAlice = sessionService.login("alice", "1234".toCharArray());
    assertEquals("Alice", reloadedAlice.username());
    assertEquals(2, reloadedAlice.exchange().getDay());
    assertEquals(1, reloadedAlice.player().getPortfolio().getShares().size());
    assertEquals("AAPL", reloadedAlice.player().getPortfolio().getShares().getFirst().getAsset().getSymbol());

    ActiveSession reloadedBob = sessionService.login("Bob", "5678".toCharArray());
    assertEquals("Bob", reloadedBob.username());
    assertEquals(3, reloadedBob.exchange().getDay());
    assertTrue(reloadedBob.player().getPortfolio().getShares().isEmpty(), "Bob should not see Alice's holdings");
    assertEquals(new BigDecimal("2025.00"), reloadedBob.player().getMoney());
  }

  @Test
  void listLeaderboardEntries_usesLiveActiveSessionState() {
    SessionService sessionService = createSessionService();

    ActiveSession alice = sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.saveActiveSession();
    alice.player().addMoney(new BigDecimal("50.00"));

    List<PlayerLeaderboardEntry> entries = sessionService.listLeaderboardEntries();

    assertEquals(1, entries.size());
    assertEquals("Alice", entries.getFirst().username());
    assertEquals(
        0,
        entries.getFirst().netWorth().compareTo(new BigDecimal("1050.00")));
    assertEquals(
        0,
        entries.getFirst().totalReturnPercent().compareTo(new BigDecimal("0.05000000")));
  }

  @Test
  void listLeaderboardEntries_handlesSavedProfilesAndInvalidDisplayNames() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), BigDecimal.ZERO);
    sessionService.logout();
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    ProfileFile profile = storage.read(paths.profileFile("Alice"), ProfileFile.class);
    storage.write(paths.profileFile("Alice"), profile.withDisplayName("x".repeat(49)));

    List<PlayerLeaderboardEntry> entries = sessionService.listLeaderboardEntries();

    assertEquals(1, entries.size());
    assertEquals("Alice", entries.getFirst().username());
    assertEquals(0, entries.getFirst().totalReturnPercent().compareTo(BigDecimal.ZERO));
  }

  @Test
  void listLeaderboardEntries_appliesValidSavedDisplayNameForInactiveProfile() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.updateDisplayName("Allie");
    sessionService.logout();

    List<PlayerLeaderboardEntry> entries = sessionService.listLeaderboardEntries();

    assertEquals("Allie", entries.getFirst().username());
  }

  @Test
  void listLeaderboardEntries_ordersTiesByReturnPercentThenUsername() {
    SessionService sessionService = createSessionService();

    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("100.00"));
    sessionService.saveActiveSession();

    ActiveSession charlie = sessionService.register("Charlie", "2345".toCharArray(), new BigDecimal("50.00"));
    charlie.player().addMoney(new BigDecimal("50.00"));
    sessionService.saveActiveSession();

    ActiveSession bob = sessionService.register("Bob", "3456".toCharArray(), new BigDecimal("50.00"));
    bob.player().addMoney(new BigDecimal("50.00"));
    sessionService.saveActiveSession();

    List<PlayerLeaderboardEntry> entries = sessionService.listLeaderboardEntries();

    assertEquals(List.of("Bob", "Charlie", "Alice"), entries.stream()
        .map(PlayerLeaderboardEntry::username)
        .toList());
    assertEquals(
        List.of("100.00", "100.00", "100.00"),
        entries.stream()
            .map(entry -> entry.netWorth().setScale(2, RoundingMode.HALF_UP).toPlainString())
            .toList());
  }

  @Test
  void welcomePreferences_roundTripForActiveSession() {
    SessionService sessionService = createSessionService();
    sessionService.register("weluser", "1234".toCharArray(), new BigDecimal("1000.00"));
    assertFalse(sessionService.hasSeenWelcome());
    sessionService.markWelcomeSeen();
    assertTrue(sessionService.hasSeenWelcome());
  }

  @Test
  void updateDisplayName_persistsAcrossLogin() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.updateDisplayName("Allie");
    sessionService.logout();

    ActiveSession back = sessionService.login("Alice", "1234".toCharArray());
    assertEquals("Allie", back.player().getName());
    ProfilePaths paths = new ProfilePaths(tempDir);
    ProfileFile profile = new JsonStorage().read(paths.profileFile("Alice"), ProfileFile.class);
    assertEquals("Allie", profile.displayName());
  }

  @Test
  void updateDisplayName_blankValueResetsToUsernameAndClearsStoredDisplayName() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.updateDisplayName("Allie");

    sessionService.updateDisplayName("   ");

    ProfilePaths paths = new ProfilePaths(tempDir);
    ProfileFile profile = new JsonStorage().read(paths.profileFile("Alice"), ProfileFile.class);
    assertEquals("Alice", sessionService.getActiveSession().orElseThrow().player().getName());
    assertEquals(null, profile.displayName());
  }

  @Test
  void avatarMethods_delegateForActiveSession() throws Exception {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    Path source = tempDir.resolve("avatar.png");
    writePng(source);

    sessionService.saveAvatarFromFile(source);
    Path avatar = sessionService.avatarPath("alice");

    assertTrue(Files.isRegularFile(avatar));
    sessionService.clearAvatar();
    assertFalse(Files.exists(avatar));
  }

  @Test
  void activeSessionOnlyOperations_throwWhenNoSessionExists() {
    SessionService sessionService = createSessionService();

    assertThrows(IllegalStateException.class, sessionService::hasSeenWelcome);
    assertThrows(IllegalStateException.class, sessionService::markWelcomeSeen);
    assertThrows(IllegalStateException.class, () -> sessionService.updateDisplayName("Allie"));
    assertThrows(IllegalStateException.class, () -> sessionService.saveAvatarFromFile(tempDir));
    assertThrows(IllegalStateException.class, sessionService::clearAvatar);
    assertThrows(IllegalStateException.class, () -> sessionService.deleteActiveProfile("1234".toCharArray()));
    assertThrows(IllegalStateException.class, () -> sessionService.exitGameAndDeleteProfile("1234".toCharArray()));
  }

  @Test
  void deleteActiveProfile_removesFiles() throws Exception {
    SessionService sessionService = createSessionService();
    sessionService.register("Zed", "1234".toCharArray(), new BigDecimal("100.00"));
    Path profileDir = tempDir.resolve(ProfilePaths.normalizeUsername("Zed"));
    assertTrue(Files.isDirectory(profileDir));
    sessionService.deleteActiveProfile("1234".toCharArray());
    assertFalse(Files.exists(profileDir));
  }

  @Test
  void deleteActiveProfile_rejectsInvalidPinFormat() {
    SessionService sessionService = createSessionService();
    sessionService.register("Zed", "1234".toCharArray(), new BigDecimal("100.00"));

    assertThrows(
        RegistrationValidationException.class,
        () -> sessionService.deleteActiveProfile("12".toCharArray()));
  }

  @Test
  void exitGameAndDeleteProfile_liquidatesHoldingsAndRemovesFiles() throws Exception {
    SessionService sessionService = createSessionService();
    ActiveSession session =
        sessionService.register("ExitUser", "1234".toCharArray(), new BigDecimal("10000.00"));
    session.exchange().buy("AAPL", new BigDecimal("2"), session.player());
    session.player().addRegularSavingsPlan(
        new model.trading.savings.RegularSavingsPlan(
            "AAPL",
            model.trading.savings.SavingsInstallmentMode.FIXED_SHARES,
            new BigDecimal("1"),
            7,
            session.exchange().getDay()));
    Path profileDir = tempDir.resolve(ProfilePaths.normalizeUsername("ExitUser"));
    assertTrue(Files.isDirectory(profileDir));
    assertFalse(session.player().getPortfolio().getShares().isEmpty());

    var result = sessionService.exitGameAndDeleteProfile("1234".toCharArray());

    assertFalse(Files.exists(profileDir));
    assertFalse(sessionService.hasActiveSession());
    assertEquals(1, result.symbolsSold());
    assertTrue(result.transactionCount() >= 1);
    assertTrue(result.finalCash().compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void exitGameAndDeleteProfile_rejectsWrongPin() {
    SessionService sessionService = createSessionService();
    sessionService.register("PinUser", "1234".toCharArray(), new BigDecimal("1000.00"));
    assertThrows(
        AuthenticationException.class,
        () -> sessionService.exitGameAndDeleteProfile("9999".toCharArray()));
    assertTrue(sessionService.hasActiveSession());
    assertTrue(Files.isDirectory(tempDir.resolve(ProfilePaths.normalizeUsername("PinUser"))));
  }

  @Test
  void deleteProfile_throwsWhenTargetIsActiveSession() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    assertThrows(
        ProfileInUseException.class,
        () -> sessionService.deleteProfile("Alice", "1234".toCharArray()));
  }

  @Test
  void deleteProfile_removesInactiveProfileAndRejectsBadCredentials() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.logout();
    Path profileDir = tempDir.resolve("alice");

    assertThrows(
        AuthenticationException.class,
        () -> sessionService.deleteProfile("Missing", "1234".toCharArray()));
    assertThrows(
        AuthenticationException.class,
        () -> sessionService.deleteProfile("Alice", "9999".toCharArray()));

    sessionService.deleteProfile("Alice", "1234".toCharArray());

    assertFalse(Files.exists(profileDir));
  }

  @Test
  void leaderboardServiceAndDefaultProfilesRoot_areAvailable() {
    SessionService sessionService = createSessionService();

    assertEquals(0, sessionService.leaderboardService().loadRows().size());
    assertTrue(SessionServiceFactory.defaultProfilesRoot().endsWith(Path.of(".millions", "profiles")));
  }

  @Test
  void login_ignoresInvalidSavedDisplayName() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    sessionService.logout();
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    ProfileFile profile = storage.read(paths.profileFile("Alice"), ProfileFile.class);
    storage.write(paths.profileFile("Alice"), profile.withDisplayName("x".repeat(49)));

    ActiveSession restored = sessionService.login("Alice", "1234".toCharArray());

    assertEquals("Alice", restored.player().getName());
  }

  @Test
  void register_withCustomMarketDataFile_usesUploadedSymbols() throws Exception {
    SessionService sessionService = createSessionService();
    Path customCsv = tempDir.resolve("upload.csv");
    Files.writeString(customCsv, "STOCK,CUSTOM,Custom Inc,99.00\n");

    ActiveSession session = sessionService.register(
        "Trader", "1234".toCharArray(), new BigDecimal("500.00"), Optional.of(customCsv));

    assertEquals("CUSTOM", session.exchange().listings().getStocks().getFirst().getSymbol());
    assertTrue(Files.isRegularFile(tempDir.resolve("trader").resolve("market-data.csv")));
  }

  @Test
  void login_restoresUsingProfileMarketDataFile() throws Exception {
    SessionService sessionService = createSessionService();
    Path customCsv = tempDir.resolve("only.csv");
    Files.writeString(customCsv, "STOCK,ONLY,Only Inc,12.00\n");
    sessionService.register(
        "Solo", "1234".toCharArray(), new BigDecimal("100.00"), Optional.of(customCsv));
    sessionService.logout();

    ActiveSession restored = sessionService.login("Solo", "1234".toCharArray());

    assertEquals("ONLY", restored.exchange().listings().getStocks().getFirst().getSymbol());
  }

  private SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        SessionServiceTest.class,
        "NYSE");
  }

  private static void writePng(Path path) throws Exception {
    BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    try {
      graphics.setColor(Color.GREEN);
      graphics.fillRect(0, 0, 8, 8);
    } finally {
      graphics.dispose();
    }
    ImageIO.write(image, "png", path.toFile());
  }
}
