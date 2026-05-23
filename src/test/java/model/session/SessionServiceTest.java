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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;
import model.session.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
  void saveCurrentRun_persistsSnapshotForActiveUser() {
    SessionService sessionService = createSessionService();
    sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    ActiveSession session = sessionService.getActiveSession().orElseThrow();
    session.exchange().buy("AAPL", new BigDecimal("1.0"), session.player());

    ProfileFile.SavedRunRow saved = sessionService.saveCurrentRun("strategy-a");

    assertEquals("strategy-a", saved.label());
    assertEquals(1, sessionService.listSavedRuns().size());
    assertTrue(sessionService.setRunLeaderboardEligible(saved.id(), true));
    assertTrue(sessionService.listSavedRuns().getFirst().eligibleForLeaderboard());
    assertTrue(sessionService.deleteSavedRun(saved.id()));
    assertTrue(sessionService.listSavedRuns().isEmpty());
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
  void deleteActiveProfile_removesFiles() throws Exception {
    SessionService sessionService = createSessionService();
    sessionService.register("Zed", "1234".toCharArray(), new BigDecimal("100.00"));
    Path profileDir = tempDir.resolve(ProfilePaths.normalizeUsername("Zed"));
    assertTrue(Files.isDirectory(profileDir));
    sessionService.deleteActiveProfile("1234".toCharArray());
    assertFalse(Files.exists(profileDir));
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
  void register_withCustomMarketDataFile_usesUploadedSymbols() throws Exception {
    SessionService sessionService = createSessionService();
    Path customCsv = tempDir.resolve("upload.csv");
    Files.writeString(customCsv, "STOCK,CUSTOM,Custom Inc,99.00\n");

    ActiveSession session = sessionService.register(
        "Trader", "1234".toCharArray(), new BigDecimal("500.00"), Optional.of(customCsv));

    assertEquals("CUSTOM", session.exchange().getStocks().getFirst().getSymbol());
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

    assertEquals("ONLY", restored.exchange().getStocks().getFirst().getSymbol());
  }

  private SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        SessionServiceTest.class,
        "NYSE");
  }
}
