package model.core.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import model.core.asset.Stock;
import model.core.market.event.MarketEvent;
import model.core.market.event.SymbolMarketEventTarget;
import org.junit.jupiter.api.Test;

class MarketSimulatorTest {

  @Test
  void advance_updatesDayPricesAndEventHistory() {
    Stock apple = stock("AAPL", "Apple Inc.", "100.00");
    MarketEvent event = new MarketEvent(
        2,
        "AAPL: Earnings beat expectations",
        "Apple Inc. reported stronger earnings than expected.",
        new SymbolMarketEventTarget(Set.of("AAPL")),
        new BigDecimal("1.10"));
    MarketSimulator simulator = new MarketSimulator(
        new Random(1),
        (stock, random) -> stock.getSalesPrice().multiply(new BigDecimal("1.05")),
        (stocks, tradingDay, random) -> Optional.of(event),
        1,
        List.of(),
        Optional.empty());

    simulator.advance(1, List.of(apple));

    assertEquals(2, simulator.getDay());
    assertEquals(new BigDecimal("115.50"), apple.getSalesPrice());
    assertEquals(Optional.of(event), simulator.getLastMarketEvent());
    assertEquals(List.of(event), simulator.getMarketEventHistory());
    assertEquals(List.of(event), simulator.getMarketEventsForStock("AAPL"));
  }

  @Test
  void advance_rejectsNegativeDays() {
    MarketSimulator simulator = new MarketSimulator(
        new Random(1),
        (stock, random) -> stock.getSalesPrice(),
        (stocks, tradingDay, random) -> Optional.empty(),
        1,
        List.of(),
        Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> simulator.advance(-1, List.of()));
  }

  private static Stock stock(String symbol, String company, String price) {
    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(new BigDecimal(price));
    return stock;
  }
}
