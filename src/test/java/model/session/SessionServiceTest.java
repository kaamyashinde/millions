package model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import model.Stock;
import model.fund.Fund;
import model.fund.FundComponent;
import model.persistence.GameStateRepository;
import model.persistence.MarketData;
import model.persistence.PinHashingService;
import model.persistence.UserAccountRepository;
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

  private SessionService createSessionService() {
    return new SessionService(
        new UserAccountRepository(tempDir),
        new GameStateRepository(tempDir),
        new PinHashingService(),
        SessionServiceTest::sampleMarketData,
        "NYSE");
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
