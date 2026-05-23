package model.persistence;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.player.Player;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.profile.ProfilePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProfileFileTest {

  @TempDir
  Path tempDir;

  @Test
  void hashPin_isDeterministicAndUsernameScoped() {
    String hash1 = ProfileFile.hashPin("alice", "1234".toCharArray());
    String hash2 = ProfileFile.hashPin("alice", "1234".toCharArray());
    String hashBob = ProfileFile.hashPin("bob", "1234".toCharArray());
    assertEquals(hash1, hash2);
    assertFalse(hash1.equals(hashBob));
  }

  @Test
  void matchesPin_verifiesCorrectPin() {
    ProfileFile profile = new ProfileFile(
        "Alice", "alice", ProfileFile.hashPin("alice", "1234".toCharArray()),
        null, false, "Alice", new BigDecimal("1000"), new BigDecimal("1000"),
        List.of(), List.of(), List.of(), "NYSE", 1, List.of(), List.of(), null);
    assertTrue(profile.matchesPin("1234".toCharArray()));
    assertFalse(profile.matchesPin("9999".toCharArray()));
  }

  @Test
  void captureAndRestore_roundTripsPlayerAndExchange() {
    MarketData marketData = sampleMarketData();
    Exchange exchange = ProfileFile.createFreshExchange(marketData, "NYSE");
    Player player = new Player("Alice", new BigDecimal("1000"));
    exchange.buy("AAPL", new BigDecimal("1"), player);
    exchange.advance();

    ProfileFile saved = ProfileFile.capture(
        player, exchange, "Alice", "alice",
        ProfileFile.hashPin("alice", "1234".toCharArray()),
        null, false);

    ProfileFile.RestoredSession restored = saved.restore(marketData);
    assertEquals(2, restored.exchange().getDay());
    assertEquals(1, restored.player().getPortfolio().getShares().size());
    assertEquals("AAPL",
        restored.player().getPortfolio().getShares().getFirst().getAsset().getSymbol());
  }

  @Test
  void jsonRoundTrip_viaJsonStorage() {
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    MarketData marketData = sampleMarketData();
    Exchange exchange = ProfileFile.createFreshExchange(marketData, "NYSE");
    Player player = new Player("Alice", new BigDecimal("500"));

    ProfileFile original = ProfileFile.capture(
        player, exchange, "Alice", "alice", "pinhash", null, false);
    Path file = paths.profileFile("alice");
    storage.write(file, original);
    ProfileFile loaded = storage.read(file, ProfileFile.class);
    assertEquals("Alice", loaded.username());
    assertEquals("NYSE", loaded.exchangeName());
  }

  @Test
  void listUsernames_ignoresBackupDirectoriesWithInvalidNames() throws Exception {
    ProfilePaths paths = new ProfilePaths(tempDir);
    JsonStorage storage = new JsonStorage();
    MarketData marketData = sampleMarketData();
    Exchange exchange = ProfileFile.createFreshExchange(marketData, "NYSE");
    Player player = new Player("Alice", new BigDecimal("500"));
    ProfileFile original = ProfileFile.capture(
        player, exchange, "Alice", "alice", "pinhash", null, false);

    storage.write(paths.profileFile("alice"), original);
    Path backupProfile = tempDir.resolve("alice.corrupt-backup").resolve("profile.json");
    Files.createDirectories(backupProfile.getParent());
    Files.writeString(backupProfile, "{\"stockPrices\":[{\"prices\":[not-json]}]}");

    assertEquals(List.of("Alice"), paths.listUsernames(storage));
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
