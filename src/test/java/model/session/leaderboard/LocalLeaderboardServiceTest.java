package model.session.leaderboard;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketDataFileService;
import model.persistence.profile.ProfilePaths;
import model.session.SessionService;
import model.session.SessionServiceFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalLeaderboardServiceTest {

  @TempDir
  Path tempDir;

  @Test
  void loadRows_restoresEachProfileWithItsOwnMarketData() throws Exception {
    SessionService sessionService = SessionServiceFactory.createLocalProfileSessionService(
        tempDir,
        "/data/demo-stocks.csv",
        LocalLeaderboardServiceTest.class,
        "NYSE");

    Path cheapCsv = tempDir.resolve("cheap.csv");
    Files.writeString(cheapCsv, "STOCK,CHEAP,Cheap Co,1.00\n");
    sessionService.register(
        "Low", "1234".toCharArray(), new BigDecimal("50.00"), Optional.of(cheapCsv));
    sessionService.logout();

    sessionService.register("High", "5678".toCharArray(), new BigDecimal("1000.00"));
    sessionService.updateDisplayName("High Roller");
    sessionService.logout();

    ProfilePaths profilePaths = new ProfilePaths(tempDir);
    JsonStorage jsonStorage = new JsonStorage();
    MarketDataFileService marketDataFileService = new MarketDataFileService(
        profilePaths, LocalLeaderboardServiceTest.class, "/data/demo-stocks.csv");
    LocalLeaderboardService leaderboardService = new LocalLeaderboardService(
        profilePaths,
        jsonStorage,
        marketDataFileService);

    var rows = leaderboardService.loadRows();

    assertEquals(2, rows.size());
    assertEquals(new BigDecimal("1000.00"), rows.getFirst().netWorth());
    assertEquals("high", rows.getFirst().normalizedUsername());
    assertEquals("High Roller", rows.getFirst().displayName());
  }

  @Test
  void rankingSupportsAscendingTotalReturnOrderingAndPrivateConstructor() throws Exception {
    List<PlayerLeaderboardEntry> entries = List.of(
        new PlayerLeaderboardEntry("Charlie", new BigDecimal("150"), new BigDecimal("0.10")),
        new PlayerLeaderboardEntry("Alice", new BigDecimal("120"), new BigDecimal("0.20")),
        new PlayerLeaderboardEntry("Bob", new BigDecimal("200"), new BigDecimal("0.20")));

    List<String> ordered = entries.stream()
        .sorted(PlayerLeaderboardRanking.displayComparator(
            PlayerLeaderboardMetric.TOTAL_RETURN_PERCENT,
            true))
        .map(PlayerLeaderboardEntry::username)
        .toList();

    assertEquals(List.of("Charlie", "Bob", "Alice"), ordered);

    Constructor<PlayerLeaderboardRanking> constructor =
        PlayerLeaderboardRanking.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
