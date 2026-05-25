package model.persistence;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.Exchange;
import model.core.market.event.MarketEvent;
import model.core.market.event.SymbolMarketEventTarget;
import model.core.player.Player;
import model.persistence.io.JsonStorage;
import model.persistence.market.MarketData;
import model.persistence.profile.ProfilePaths;
import model.trading.savings.SavingsInstallmentMode;
import model.trading.savings.RegularSavingsPlan;
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
  void hashPin_wrapsMissingSha256Provider() {
    List<Provider> removedProviders = new ArrayList<>();
    for (Provider provider : Security.getProviders()) {
      if (provider.getService("MessageDigest", "SHA-256") != null) {
        Security.removeProvider(provider.getName());
        removedProviders.add(provider);
      }
    }
    try {
      assertThrows(
          IllegalStateException.class,
          () -> ProfileFile.hashPin("alice", "1234".toCharArray()));
    } finally {
      for (Provider provider : removedProviders) {
        Security.addProvider(provider);
      }
    }
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
  void matchesPin_returnsFalseWhenHashIsMissing() {
    ProfileFile profile = new ProfileFile(
        "Alice", "alice", null,
        null, false, "Alice", new BigDecimal("1000"), new BigDecimal("1000"),
        List.of(), List.of(), List.of(), "NYSE", 1, List.of(), List.of(), null);

    assertFalse(profile.matchesPin("1234".toCharArray()));
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
  void jsonRead_defaultsMissingListsToEmpty() throws Exception {
    JsonStorage storage = new JsonStorage();
    Path file = tempDir.resolve("legacy-profile.json");
    Files.writeString(
        file,
        """
        {
          "username": "Alice",
          "normalizedUsername": "alice",
          "pinHash": "hash",
          "displayName": null,
          "hasSeenWelcome": false,
          "playerName": "Alice",
          "startingMoney": 1000,
          "cash": 1000,
          "exchangeName": "NYSE",
          "day": 1,
          "lastEvent": null
        }
        """);

    ProfileFile loaded = storage.read(file, ProfileFile.class);

    assertNotNull(loaded.holdings());
    assertTrue(loaded.holdings().isEmpty());
    assertNotNull(loaded.transactions());
    assertTrue(loaded.transactions().isEmpty());
  }

  @Test
  void withDisplayNameAndWelcomeSeen_returnUpdatedCopies() {
    ProfileFile profile = new ProfileFile(
        "Alice", "alice", "hash", null, false, "Alice",
        new BigDecimal("1000"), new BigDecimal("1000"),
        List.of(), List.of(), List.of(), "NYSE", 1, List.of(), List.of(), null);

    ProfileFile renamed = profile.withDisplayName("Allie");
    ProfileFile welcomed = renamed.withWelcomeSeen();

    assertEquals("Allie", renamed.displayName());
    assertFalse(renamed.hasSeenWelcome());
    assertTrue(welcomed.hasSeenWelcome());
    assertEquals("Allie", welcomed.displayName());
  }

  @Test
  void captureAndRestore_roundTripsSavingsPlansAndMarketEvents() {
    Stock apple = new Stock("AAPL", "Apple Inc.");
    apple.addNewSalesPrice(new BigDecimal("150.00"));
    MarketData marketData = new MarketData(List.of(apple), List.of());
    MarketEvent event = new MarketEvent(
        3,
        "AAPL: Surprise",
        "Apple surprise",
        new SymbolMarketEventTarget(Set.of("AAPL")),
        new BigDecimal("1.10"));
    Exchange exchange = new Exchange.Builder("NYSE")
        .stocks(List.of(apple))
        .funds(List.of())
        .day(3)
        .marketEventHistory(List.of(event))
        .lastMarketEvent(event)
        .build();
    Player player = new Player("Alice", new BigDecimal("1000"));
    RegularSavingsPlan plan = new RegularSavingsPlan(
        "aapl",
        SavingsInstallmentMode.FIXED_SHARES,
        new BigDecimal("2"),
        7,
        exchange.getDay());
    plan.setNextDueDay(42);
    plan.setActive(false);
    player.addRegularSavingsPlan(plan);

    ProfileFile saved = ProfileFile.capture(
        player, exchange, "Alice", "alice", "hash", "Allie", true);
    ProfileFile.RestoredSession restored = saved.restore(marketData);

    assertEquals(1, restored.player().getRegularSavingsPlans().size());
    RegularSavingsPlan restoredPlan = restored.player().getRegularSavingsPlans().getFirst();
    assertEquals("AAPL", restoredPlan.getSymbol());
    assertEquals(SavingsInstallmentMode.FIXED_SHARES, restoredPlan.getMode());
    assertEquals(new BigDecimal("2"), restoredPlan.getAmount());
    assertEquals(7, restoredPlan.getIntervalDays());
    assertEquals(42, restoredPlan.getNextDueDay());
    assertFalse(restoredPlan.isActive());
    assertEquals(1, saved.events().size());
    assertEquals("AAPL: Surprise", restored.exchange().getLastMarketEvent().orElseThrow().title());
    assertEquals(1, restored.exchange().getMarketEventsForStock("AAPL").size());
  }

  @Test
  void restore_rejectsUnknownHoldingAsset() {
    ProfileFile profile = baseProfile(
        List.of(new ProfileFile.HoldingRow("MISSING", BigDecimal.ONE, BigDecimal.TEN)),
        List.of(),
        List.of(),
        List.of(new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("150.00")))),
        List.of(),
        null);

    IllegalStateException thrown = assertThrows(
        IllegalStateException.class,
        () -> profile.restore(new MarketData(List.of(sampleStock("AAPL")), List.of())));

    assertEquals("Unknown asset in saved profile: MISSING", thrown.getMessage());
  }

  @Test
  void restore_rejectsUnknownTransactionType() {
    ProfileFile profile = baseProfile(
        List.of(),
        List.of(new ProfileFile.TxRow("DIVIDEND", "AAPL", BigDecimal.ONE, BigDecimal.TEN, 1)),
        List.of(),
        List.of(new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("150.00")))),
        List.of(),
        null);

    IllegalStateException thrown = assertThrows(
        IllegalStateException.class,
        () -> profile.restore(new MarketData(List.of(sampleStock("AAPL")), List.of())));

    assertEquals("Unknown transaction type: DIVIDEND", thrown.getMessage());
  }

  @Test
  void restore_recreatesSaleTransactions() {
    ProfileFile profile = baseProfile(
        List.of(),
        List.of(new ProfileFile.TxRow("SALE", "AAPL", BigDecimal.ONE, BigDecimal.TEN, 2)),
        List.of(),
        List.of(new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("150.00")))),
        List.of(),
        null);

    ProfileFile.RestoredSession restored =
        profile.restore(new MarketData(List.of(sampleStock("AAPL")), List.of()));

    assertEquals("Sale", restored.player().getTransactionArchive().getAllTransactions().getFirst().getTypeName());
  }

  @Test
  void restore_keepsFirstDuplicateSavedPriceRow() {
    ProfileFile profile = baseProfile(
        List.of(),
        List.of(),
        List.of(),
        List.of(
            new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("150.00"))),
            new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("999.00")))),
        List.of(),
        null);

    ProfileFile.RestoredSession restored =
        profile.restore(new MarketData(List.of(sampleStock("AAPL")), List.of()));

    assertEquals(new BigDecimal("150.00"), restored.exchange().getStock("AAPL").getSalesPrice());
  }

  @Test
  void createFreshExchange_keepsFirstDuplicateStockSymbol() {
    Stock first = new Stock("DUP", "First");
    first.addNewSalesPrice(BigDecimal.ONE);
    Stock second = new Stock("DUP", "Second");
    second.addNewSalesPrice(BigDecimal.TEN);

    Exchange exchange = ProfileFile.createFreshExchange(
        new MarketData(List.of(first, second), List.of()),
        "NYSE");

    assertEquals("First", exchange.getStock("DUP").getCompany());
  }

  @Test
  void restore_rejectsFundComponentsMissingFromSavedPrices() {
    Stock apple = sampleStock("AAPL");
    Stock microsoft = sampleStock("MSFT");
    Fund fund = new Fund(
        "PAIR",
        "Pair Fund",
        List.of(
            new FundComponent(apple, new BigDecimal("0.50")),
            new FundComponent(microsoft, new BigDecimal("0.50"))));
    ProfileFile profile = baseProfile(
        List.of(),
        List.of(),
        List.of(),
        List.of(new ProfileFile.PriceRow("AAPL", List.of(new BigDecimal("150.00")))),
        List.of(),
        null);

    IllegalStateException thrown = assertThrows(
        IllegalStateException.class,
        () -> profile.restore(new MarketData(List.of(apple, microsoft), List.of(fund))));

    assertEquals("Missing stock in restored exchange: MSFT", thrown.getMessage());
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

  private static Stock sampleStock(String symbol) {
    Stock stock = new Stock(symbol, symbol + " Company");
    stock.addNewSalesPrice(new BigDecimal("150.00"));
    return stock;
  }

  private static ProfileFile baseProfile(
      List<ProfileFile.HoldingRow> holdings,
      List<ProfileFile.TxRow> transactions,
      List<ProfileFile.SavingsRow> savings,
      List<ProfileFile.PriceRow> stockPrices,
      List<ProfileFile.EventRow> events,
      ProfileFile.EventRow lastEvent) {
    return new ProfileFile(
        "Alice",
        "alice",
        "hash",
        null,
        false,
        "Alice",
        new BigDecimal("1000"),
        new BigDecimal("1000"),
        holdings,
        transactions,
        savings,
        "NYSE",
        1,
        stockPrices,
        events,
        lastEvent);
  }
}
