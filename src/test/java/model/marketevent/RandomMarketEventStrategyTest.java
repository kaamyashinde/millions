package model.marketevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import model.Stock;
import model.marketevent.unexpected.MarketEvent;
import model.marketevent.unexpected.strategy.RandomMarketEventStrategy;
import model.marketevent.unexpected.target.SymbolMarketEventTarget;
import org.junit.jupiter.api.Test;

class RandomMarketEventStrategyTest {

  @Test
  void maybeCreateEvent_returnsEmptyWhenNoListedStocks() {
    RandomMarketEventStrategy strategy = new RandomMarketEventStrategy(1.0, singleTemplate());
    Random random = new Random(1L);

    assertEquals(Optional.empty(), strategy.maybeCreateEvent(List.of(), 3, random));
  }

  @Test
  void maybeCreateEvent_rejectsNullListedStocks() {
    RandomMarketEventStrategy strategy = new RandomMarketEventStrategy(1.0, singleTemplate());

    NullPointerException error =
        assertThrows(
            NullPointerException.class,
            () -> strategy.maybeCreateEvent(null, 1, new Random(1L)));

    assertEquals("Listed stocks cannot be null", error.getMessage());
  }

  @Test
  void maybeCreateEvent_rejectsNullRandom() {
    RandomMarketEventStrategy strategy = new RandomMarketEventStrategy(1.0, singleTemplate());
    Stock stock = new Stock("AAPL", "Apple Inc.");

    NullPointerException error =
        assertThrows(
            NullPointerException.class,
            () -> strategy.maybeCreateEvent(List.of(stock), 1, null));

    assertEquals("Random cannot be null", error.getMessage());
  }

  @Test
  void maybeCreateEvent_buildsEventWithExpectedShapeWhenTriggered() {
    Stock stock = new Stock("MSFT", "Microsoft Corporation");
    RandomMarketEventStrategy.EventTemplate template =
        new RandomMarketEventStrategy.EventTemplate(
            "Earnings beat expectations",
            "%s reported stronger earnings than expected, lifting sentiment around %s.",
            1.10,
            1.11);
    RandomMarketEventStrategy strategy = new RandomMarketEventStrategy(1.0, List.of(template));
    Random random = new Random(12345L);

    Optional<MarketEvent> maybe = strategy.maybeCreateEvent(List.of(stock), 42, random);

    assertTrue(maybe.isPresent());
    MarketEvent event = maybe.orElseThrow();
    assertEquals(42, event.day());
    assertEquals("MSFT: Earnings beat expectations", event.title());
    assertEquals(
        "Microsoft Corporation reported stronger earnings than expected, lifting sentiment around MSFT.",
        event.description());
    assertInstanceOf(SymbolMarketEventTarget.class, event.target());
    assertEquals(Set.of("MSFT"), event.target().getAffectedSymbols());
    BigDecimal factor = event.priceFactor();
    assertTrue(
        factor.compareTo(BigDecimal.valueOf(1.10)) >= 0
            && factor.compareTo(BigDecimal.valueOf(1.11)) <= 0);
  }

  @Test
  void maybeCreateEvent_canReturnEmptyWhenProbabilityCheckFails() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    RandomMarketEventStrategy.EventTemplate template =
        new RandomMarketEventStrategy.EventTemplate("Title", "%s %s", 1.0, 1.01);
    RandomMarketEventStrategy strategy = new RandomMarketEventStrategy(0.0, List.of(template));
    Random random = new Random(1L);

    assertEquals(Optional.empty(), strategy.maybeCreateEvent(List.of(stock), 1, random));
  }

  private static List<RandomMarketEventStrategy.EventTemplate> singleTemplate() {
    return List.of(new RandomMarketEventStrategy.EventTemplate("T", "%s %s", 1.0, 1.01));
  }
}
