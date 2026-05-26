package model.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.file.Path;
import model.persistence.ProfileFile;
import model.persistence.io.JsonStorage;
import model.persistence.profile.ProfilePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SessionPersistenceServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void save_persistsActivePlayerAndExchangeState() {
    SessionService sessionService = createSessionService();
    ActiveSession session =
        sessionService.register("Alice", "1234".toCharArray(), new BigDecimal("100.00"));
    session.player().addMoney(new BigDecimal("25.00"));
    session.exchange().advance();
    SessionPersistenceService persistence = new SessionPersistenceService(
        new ProfilePaths(tempDir),
        new JsonStorage());

    persistence.save(session);

    ProfileFile saved = new JsonStorage().read(
        new ProfilePaths(tempDir).profileFile("Alice"),
        ProfileFile.class);
    assertEquals(0, saved.cash().compareTo(new BigDecimal("125.00")));
    assertEquals(2, saved.day());
  }

  private SessionService createSessionService() {
    return SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        SessionPersistenceServiceTest.class,
        "NYSE");
  }
}
