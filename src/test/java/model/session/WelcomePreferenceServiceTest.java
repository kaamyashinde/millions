package model.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WelcomePreferenceServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void welcomePreference_roundTripsForActiveSession() {
    SessionService sessionService = createSessionService();
    ActiveSession session =
        sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("100.00"));
    WelcomePreferenceService welcomePreferenceService = new WelcomePreferenceService(
        new ProfilePaths(tempDir),
        new JsonStorage());

    assertFalse(welcomePreferenceService.hasSeenWelcome(session));

    welcomePreferenceService.markWelcomeSeen(session);

    assertTrue(welcomePreferenceService.hasSeenWelcome(session));
  }

  private SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        WelcomePreferenceServiceTest.class,
        "NYSE");
  }
}
