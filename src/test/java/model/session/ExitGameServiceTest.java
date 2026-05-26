package model.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfileImageService;
import model.persistence.profile.ProfilePaths;
import model.session.profile.ProfileService;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExitGameServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void exitAndDelete_liquidatesSavingsAndProfileDirectory() {
    SessionService sessionService = createSessionService();
    ActiveSession session =
        sessionService.register("ExitUser", "1234".toCharArray(), new BigDecimal("10000.00"));
    session.exchange().buy("AAPL", new BigDecimal("2"), session.player());
    session.player().addRegularSavingsPlan(new RegularSavingsPlan(
        "AAPL",
        SavingsInstallmentMode.FIXED_SHARES,
        BigDecimal.ONE,
        7,
        session.exchange().getDay()));
    Path profileDir = tempDir.resolve("exituser");
    ExitGameService exitGameService = new ExitGameService(createProfileService());

    ExitGameResult result = exitGameService.exitAndDelete(session, "1234".toCharArray());

    assertFalse(Files.exists(profileDir));
    assertTrue(session.player().getPortfolio().getShares().isEmpty());
    assertTrue(session.player().getRegularSavingsPlans().isEmpty());
    assertTrue(result.symbolsSold() >= 1);
    assertTrue(result.transactionCount() >= 1);
    assertTrue(result.finalCash().compareTo(BigDecimal.ZERO) > 0);
  }

  private ProfileService createProfileService() {
    ProfilePaths profilePaths = new ProfilePaths(tempDir);
    return new ProfileService(
        profilePaths,
        new JsonStorage(),
        new ProfileImageService(profilePaths));
  }

  private SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        ExitGameServiceTest.class,
        "NYSE");
  }
}
