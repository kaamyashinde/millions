package model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import model.core.market.Stock;
import model.core.market.fund.Fund;
import model.core.market.fund.FundComponent;
import model.persistence.GameStateMapper;
import model.persistence.GameStateRepository;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.ProfileDirectories;
import model.persistence.ProfileImageService;
import model.persistence.ProfilePreferencesRepository;
import model.persistence.SavedRunMapper;
import model.persistence.SavedRunRecord;
import model.persistence.SavedRunRepository;
import model.persistence.UserAccountRecord;
import model.persistence.UserAccountRepository;
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

    ActiveSession alice =
        sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
    alice.exchange().buy("AAPL", new BigDecimal("1.0"), alice.player());
    alice.exchange().advance();
    sessionService.saveActiveSession();

    ActiveSession bob =
        sessionService.register("Bob", "5678".toCharArray(), new BigDecimal("2000.00"));
    bob.player().addMoney(new BigDecimal("25.00"));
    bob.exchange().advance(2);
    sessionService.saveActiveSession();

    ActiveSession reloadedAlice = sessionService.login("alice", "1234".toCharArray());
    assertEquals("Alice", reloadedAlice.username());
    assertEquals(2, reloadedAlice.exchange().getDay());
    assertEquals(1, reloadedAlice.player().getPortfolio().getShares().size());
    assertEquals("AAPL",
        reloadedAlice.player().getPortfolio().getShares().getFirst().getAsset().getSymbol());

    ActiveSession reloadedBob = sessionService.login("Bob", "5678".toCharArray());
    assertEquals("Bob", reloadedBob.username());
    assertEquals(3, reloadedBob.exchange().getDay());
    assertTrue(reloadedBob.player().getPortfolio().getShares().isEmpty(),
        "Bob should not see Alice's holdings");
    assertEquals(new BigDecimal("2025.00"), reloadedBob.player().getMoney());
  }

  @Test
  void listLeaderboardEntries_usesLiveActiveSessionState() {
    SessionService sessionService = createSessionService();

    ActiveSession alice =
        sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("1000.00"));
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

    ActiveSession charlie =
        sessionService.register("Charlie", "2345".toCharArray(), new BigDecimal("50.00"));
    charlie.player().addMoney(new BigDecimal("50.00"));
    sessionService.saveActiveSession();

    ActiveSession bob =
        sessionService.register("Bob", "3456".toCharArray(), new BigDecimal("50.00"));
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

    SavedRunRecord saved = sessionService.saveCurrentRun("strategy-a");

    assertEquals("strategy-a", saved.label());
    assertEquals(1, saved.holdings().size());
    assertEquals("AAPL", saved.holdings().getFirst().symbol());
    assertEquals(1, sessionService.listSavedRuns().size());
    UUID runId = UUID.fromString(saved.runId());
    assertTrue(sessionService.setRunLeaderboardEligible(runId, true));
    assertTrue(sessionService.listSavedRuns().getFirst().eligibleForLeaderboard());
    assertTrue(sessionService.deleteSavedRun(runId));
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
    UserAccountRecord account =
        new UserAccountRepository(tempDir).findByUsername("Alice").orElseThrow();
    assertEquals("Allie", account.displayName());
  }

  @Test
  void deleteActiveProfile_removesFiles() throws Exception {
    SessionService sessionService = createSessionService();
    sessionService.register("Zed", "1234".toCharArray(), new BigDecimal("100.00"));
    Path profileDir = tempDir.resolve(ProfileDirectories.normalizeUsername("Zed"));
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

  private SessionService createSessionService() {
    UserAccountRepository userAccountRepository = new UserAccountRepository(tempDir);
    PinHashingService pinHashingService = new PinHashingService();

    GamePersistenceService gamePersistenceService = new GamePersistenceService(
        new GameStateRepository(tempDir),
        new GameStateMapper("NYSE"),
        SessionServiceTest::sampleMarketData);

    AuthService authService = new AuthService(
        userAccountRepository, pinHashingService, gamePersistenceService);

    ProfileService profileService = new ProfileService(
        userAccountRepository,
        new ProfileImageService(tempDir),
        pinHashingService,
        tempDir);

    SavedRunService savedRunService = new SavedRunService(
        new SavedRunRepository(tempDir), new SavedRunMapper());

    ProfilePreferencesService profilePreferencesService = new ProfilePreferencesService(
        new ProfilePreferencesRepository(tempDir));

    return new SessionService(
        authService,
        profileService,
        gamePersistenceService,
        savedRunService,
        profilePreferencesService);
  }

  private static MarketData sampleMarketData() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("150.00"));

    Stock microsoft = new Stock("MSFT", "Microsoft Corp.");
    microsoft.addNewSalesPrice(new BigDecimal("300.00"));

    Fund blend = new Fund(
        "BLEND",
        "Blend Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.60")),
            new FundComponent(microsoft, new BigDecimal("0.40"))));

    return new MarketData(List.of(apple, microsoft), List.of(blend));
  }
}
