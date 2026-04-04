package model.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import model.Exchange;
import model.Player;
import model.Stock;
import model.fund.Fund;
import model.fund.FundComponent;
import model.savings.RegularSavingsPlan;
import model.savings.SavingsInstallmentMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameStateRepositoryTest {

  @TempDir
  Path tempDir;

  @Test
  void saveAndLoad_roundTripRestoresPlayerAndExchangeState() {
    GameStateRepository repository = new GameStateRepository(tempDir);
    GameStateMapper mapper = new GameStateMapper("NYSE");
    Exchange exchange = mapper.createFreshExchange(sampleMarketData());
    Player player = new Player("alice", new BigDecimal("1000.00"));

    exchange.buy("AAPL", new BigDecimal("1.5"), player);
    exchange.advance();
    player.addRegularSavingsPlan(new RegularSavingsPlan(
        "AAPL",
        SavingsInstallmentMode.FIXED_SHARES,
        new BigDecimal("2.0"),
        5,
        exchange.getDay()));

    repository.save("alice", mapper.toSnapshot(player, exchange));

    GameStateSnapshot snapshot = repository.load("alice").orElseThrow();
    Exchange restoredExchange = mapper.restoreExchange(snapshot.exchange(), sampleMarketData());
    Player restoredPlayer = mapper.restorePlayer(snapshot.player(), restoredExchange);

    assertEquals(exchange.getDay(), restoredExchange.getDay());
    assertEquals(player.getMoney(), restoredPlayer.getMoney());
    assertEquals(1, restoredPlayer.getPortfolio().getShares().size());
    assertEquals("AAPL", restoredPlayer.getPortfolio().getShares().getFirst().getAsset().getSymbol());
    assertEquals(1, restoredPlayer.getTransactionArchive().getAllTransactions().size());
    assertEquals(1, restoredPlayer.getRegularSavingsPlans().size());
    assertEquals(
        player.getRegularSavingsPlans().getFirst().getNextDueDay(),
        restoredPlayer.getRegularSavingsPlans().getFirst().getNextDueDay());
    assertTrue(
        restoredExchange.getStock("AAPL").getHistoricalPrices().size() >= 2,
        "Price history should include the advanced trading day");
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
            new FundComponent(apple, new BigDecimal("0.50")),
            new FundComponent(microsoft, new BigDecimal("0.50"))));

    return new MarketData(List.of(apple, microsoft), List.of(blend));
  }
}
