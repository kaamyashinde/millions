package model.core.market;


import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.player.Player;
import model.trading.calculator.PurchaseCalculator;
import model.trading.calculator.SaleCalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import model.exception.trading.InsufficientFundsException;
import model.exception.trading.InsufficientSharesException;
import model.core.asset.fund.Fund;
import model.core.asset.fund.FundComponent;
import model.core.market.event.MarketEvent;
import model.core.market.pricing.DailyPriceMoveStrategy;
import model.core.market.pricing.MarketEventStrategy;
import model.core.market.event.SymbolMarketEventTarget;
import model.trading.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeTest {

  private Exchange exchange;
  private Stock appleStock;
  private Stock googleStock;
  private Stock microsoftStock;
  private Fund techFund;
  private Player player;

  @BeforeEach
  void setUp() {
    appleStock = new Stock("AAPL", "Apple Inc.");
    appleStock.addNewSalesPrice(new BigDecimal("150.00"));

    googleStock = new Stock("GOOGL", "Alphabet Inc.");
    googleStock.addNewSalesPrice(new BigDecimal("2800.00"));

    microsoftStock = new Stock("MSFT", "Microsoft Corporation");
    microsoftStock.addNewSalesPrice(new BigDecimal("300.00"));
    techFund = new Fund(
        "TECHX",
        "Tech Titans Blend Fund",
        List.of(
            new FundComponent(appleStock, new BigDecimal("0.40")),
            new FundComponent(googleStock, new BigDecimal("0.35")),
            new FundComponent(microsoftStock, new BigDecimal("0.25"))));

    List<Stock> stocks = List.of(appleStock, googleStock, microsoftStock);
    exchange = new ExchangeBuilder("NYSE")
        .stocks(stocks)
        .funds(List.of(techFund))
        .build();

    player = new Player("TestPlayer", new BigDecimal("100000.00"));
  }

  @Test
  void getName() {
    assertEquals("NYSE", exchange.getName());
  }

  @Test
  void getDay_initialDayIsOne() {
    assertEquals(1, exchange.getDay());
  }

  @Test
  void builderRejectsInvalidTradingDay() {
    assertThrows(IllegalArgumentException.class, () -> new ExchangeBuilder("NYSE").day(0).build());
  }

  @Test
  void buy_createsTransactionAndUpdatesPlayer() {
    BigDecimal initialMoney = player.getMoney();
    BigDecimal quantity = new BigDecimal("10");

    Transaction transaction = exchange.buy("AAPL", quantity, player);

    assertNotNull(transaction);
    assertTrue(player.getMoney().compareTo(initialMoney) < 0);
    assertFalse(player.getPortfolio().getShares().isEmpty());
  }

  @Test
  void buy_fundCreatesPortfolioHolding() {
    Transaction transaction = exchange.buy("TECHX", new BigDecimal("2"), player);

    assertNotNull(transaction);
    assertEquals("TECHX", player.getPortfolio().getShares().getFirst().getAsset().getSymbol());
  }

  @Test
  void buy_transactionHasCorrectDay() {
    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    assertEquals(1, transaction.getDay());
  }

  @Test
  void sell_createsTransactionAndUpdatesPlayer() {
    // First buy some shares
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share shareToSell = player.getPortfolio().getShares().getFirst();

    BigDecimal moneyBeforeSale = player.getMoney();
    Transaction transaction = exchange.sell(shareToSell, player);

    assertNotNull(transaction);
    assertTrue(player.getMoney().compareTo(moneyBeforeSale) > 0);
  }

  @Test
  void sell_transactionHasCorrectDay() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share shareToSell = player.getPortfolio().getShares().getFirst();

    Transaction transaction = exchange.sell(shareToSell, player);
    assertEquals(1, transaction.getDay());
  }

  @Test
  void advance_incrementsDay() {
    assertEquals(1, exchange.getDay());

    exchange.advance();
    assertEquals(2, exchange.getDay());

    exchange.advance();
    assertEquals(3, exchange.getDay());
  }

  @Test
  void advance_withMultipleDays_incrementsDayByRequestedAmount() {
    exchange.advance(4);

    assertEquals(5, exchange.getDay());
    assertEquals(5, appleStock.getHistoricalPrices().size());
    assertEquals(5, googleStock.getHistoricalPrices().size());
    assertEquals(5, microsoftStock.getHistoricalPrices().size());
  }

  @Test
  void advance_withZeroDays_doesNotChangeDayOrPrices() {
    BigDecimal applePriceBeforeAdvance = appleStock.getSalesPrice();

    exchange.advance(0);

    assertEquals(1, exchange.getDay());
    assertEquals(applePriceBeforeAdvance, appleStock.getSalesPrice());
    assertEquals(1, appleStock.getHistoricalPrices().size());
  }

  @Test
  void advance_withNegativeDays_throwsException() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> exchange.advance(-1));

    assertEquals("Days to advance cannot be negative", exception.getMessage());
  }

  @Test
  void advance_updateStockPrices() {
    Exchange rangeCheckedExchange =
        new ExchangeBuilder("NYSE")
            .stocks(List.of(appleStock, googleStock, microsoftStock))
            .random(new Random(7))
            .dailyPriceMoveStrategy(DailyPriceMoveStrategy.uniform(0.05 / Math.sqrt(7)))
            .marketEventStrategy((stocks, tradingDay, random) -> Optional.empty())
            .build();
    BigDecimal initialApplePrice = appleStock.getSalesPrice();
    BigDecimal initialGooglePrice = googleStock.getSalesPrice();

    rangeCheckedExchange.advance();

    // One daily step: ±5%/√7 of prior price (same scaling as Exchange.advance)
    double dailySigma = 0.05 / Math.sqrt(7);
    BigDecimal newApplePrice = appleStock.getSalesPrice();
    BigDecimal newGooglePrice = googleStock.getSalesPrice();

    BigDecimal appleLowerBound =
        initialApplePrice.multiply(BigDecimal.valueOf(1 - dailySigma));
    BigDecimal appleUpperBound =
        initialApplePrice.multiply(BigDecimal.valueOf(1 + dailySigma));
    assertTrue(newApplePrice.compareTo(appleLowerBound) >= 0);
    assertTrue(newApplePrice.compareTo(appleUpperBound) <= 0);

    BigDecimal googleLowerBound =
        initialGooglePrice.multiply(BigDecimal.valueOf(1 - dailySigma));
    BigDecimal googleUpperBound =
        initialGooglePrice.multiply(BigDecimal.valueOf(1 + dailySigma));
    assertTrue(newGooglePrice.compareTo(googleLowerBound) >= 0);
    assertTrue(newGooglePrice.compareTo(googleUpperBound) <= 0);
  }

  @Test
  void advance_roundsGeneratedPricesToCents() {
    Exchange preciseExchange =
        new ExchangeBuilder("NYSE")
            .stocks(List.of(appleStock))
            .dailyPriceMoveStrategy(
                (stock, random) ->
                    stock.getSalesPrice().multiply(new BigDecimal("1.234567890123456789")))
            .marketEventStrategy((stocks, tradingDay, random) -> Optional.empty())
            .build();

    preciseExchange.advance();

    assertEquals(new BigDecimal("185.19"), appleStock.getSalesPrice());
  }

  @Test
  void buyAfterAdvance_usesCurrentDay() {
    exchange.advance(2);

    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    assertEquals(3, transaction.getDay());
  }

  @Test
  void getGainers_returnsStocksWithPositivePriceChange() {
    // Advance multiple times to get random price changes
    exchange.advance();
    exchange.advance();
    exchange.advance();

    List<Stock> gainers = exchange.getGainers(10);
    // All gainers should have positive price changes
    assertTrue(gainers.stream()
        .allMatch(stock -> stock.getLatestPriceChange().signum() > 0));
  }

  @Test
  void getGainers_returnsSortedByPriceChangeDescending() {
    exchange.advance();
    exchange.advance();

    List<Stock> gainers = exchange.getGainers(10);

    // Verify gainers are sorted in descending order (highest gains first)
    for (int i = 0; i < gainers.size() - 1; i++) {
      assertTrue(gainers.get(i).getLatestPriceChange()
          .compareTo(gainers.get(i + 1).getLatestPriceChange()) >= 0);
    }
  }

  @Test
  void getGainers_respectsLimit() {
    exchange.advance();
    exchange.advance();
    exchange.advance();

    List<Stock> gainers = exchange.getGainers(2);
    assertTrue(gainers.size() <= 2);
  }

  @Test
  void getLosers_returnsStocksWithNegativePriceChange() {
    exchange.advance();
    exchange.advance();
    exchange.advance();

    List<Stock> losers = exchange.getLosers(10);
    // All losers should have negative price changes
    assertTrue(losers.stream()
        .allMatch(stock -> stock.getLatestPriceChange().signum() < 0));
  }

  @Test
  void getLosers_returnsSortedByPriceChangeAscending() {
    exchange.advance();
    exchange.advance();

    List<Stock> losers = exchange.getLosers(10);

    // Verify losers are sorted in ascending order (biggest losses first)
    for (int i = 0; i < losers.size() - 1; i++) {
      assertTrue(losers.get(i).getLatestPriceChange()
          .compareTo(losers.get(i + 1).getLatestPriceChange()) <= 0);
    }
  }

  @Test
  void getLosers_respectsLimit() {
    exchange.advance();
    exchange.advance();
    exchange.advance();

    List<Stock> losers = exchange.getLosers(2);
    assertTrue(losers.size() <= 2);
  }

  @Test
  void buyUpToBudget_spendsAtMostMaxAndPlayerCash() {
    Player p = new Player("P", new BigDecimal("1600"));
    exchange.buyUpToBudget("AAPL", new BigDecimal("1600"), p);
    assertTrue(p.getMoney().signum() >= 0);
    Share sh = p.getPortfolio().getShares().getFirst();
    BigDecimal total = new model.trading.calculator.PurchaseCalculator(sh).calculateTotal();
    assertTrue(total.compareTo(new BigDecimal("1600")) <= 0);
  }

  @Test
  void buyUpToBudget_throwsWhenCannotAffordAnyShare() {
    Player p = new Player("P", BigDecimal.ZERO);
    assertThrows(InsufficientFundsException.class,
        () -> exchange.buyUpToBudget("AAPL", new BigDecimal("50000"), p));
  }

  @Test
  void sellByQuantity_splitsFifoLots() {
    exchange.buy("AAPL", new BigDecimal("3"), player);
    exchange.buy("AAPL", new BigDecimal("2"), player);
    List<Transaction> txs = exchange.sellByQuantity("AAPL", new BigDecimal("4"), player);
    assertEquals(2, txs.size());
    assertTrue(player.getPortfolio().totalQuantityForSymbol("AAPL").compareTo(new BigDecimal("1")) == 0);
  }

  @Test
  void sellByQuantity_throwsWhenNotEnoughShares() {
    exchange.buy("AAPL", new BigDecimal("1"), player);
    assertThrows(InsufficientSharesException.class,
        () -> exchange.sellByQuantity("AAPL", new BigDecimal("5"), player));
  }

  @Test
  void sellUpToTargetNet_respectsNetCap() {
    exchange.buy("AAPL", new BigDecimal("100"), player);
    BigDecimal target = new BigDecimal("500");
    List<Transaction> txs = exchange.sellUpToTargetNet("AAPL", target, player);
    BigDecimal sumNet = txs.stream()
        .map(t -> new model.trading.calculator.SaleCalculator(t.getShare()).calculateTotal())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertTrue(sumNet.compareTo(target) <= 0);
    assertTrue(sumNet.signum() > 0);
  }

  @Test
  void advance_appliesLargeShockToAffectedStockAndStoresEvent() {
    Exchange eventExchange =
        new ExchangeBuilder("NYSE")
            .stocks(List.of(appleStock, googleStock, microsoftStock))
            .random(new Random(3))
            .dailyPriceMoveStrategy(
                (stock, random) -> stock.getSalesPrice().multiply(new BigDecimal("1.015")))
            .marketEventStrategy(
                (stocks, tradingDay, random) -> Optional.of(
                    new MarketEvent(
                        tradingDay,
                        "AAPL: Earnings beat expectations",
                        "Apple Inc. reported stronger earnings than expected.",
                        new SymbolMarketEventTarget(Set.of("AAPL")),
                        new BigDecimal("1.015"))))
            .build();

    BigDecimal initialApplePrice = appleStock.getSalesPrice();
    BigDecimal initialGooglePrice = googleStock.getSalesPrice();

    eventExchange.advance();

    assertTrue(eventExchange.getLastMarketEvent().isPresent());
    assertEquals("AAPL: Earnings beat expectations", eventExchange.getLastMarketEvent().get().title());
    assertEquals(1, eventExchange.getMarketEventHistory().size());
    assertEquals("AAPL: Earnings beat expectations", eventExchange.getMarketEventHistory().getFirst().title());
    assertEquals(new BigDecimal("154.53"), appleStock.getSalesPrice());
    assertEquals(new BigDecimal("2842.00"), googleStock.getSalesPrice());

    double dailySigma = 0.05 / Math.sqrt(7);
    BigDecimal normalUpperBound = initialApplePrice.multiply(BigDecimal.valueOf(1 + dailySigma));
    assertTrue(appleStock.getSalesPrice().compareTo(normalUpperBound) > 0);
  }

  @Test
  void randomFromResources_loadsJsonTemplatesAndCreatesEvent() {
    MarketEventStrategy strategy =
        MarketEventStrategy.randomFromResources(1.0, MarketEventStrategy.DEFAULT_TEMPLATE_RESOURCE);

    Optional<MarketEvent> generated =
        strategy.maybeCreateEvent(List.of(appleStock), 8, new Random(4));

    assertTrue(generated.isPresent());
    MarketEvent event = generated.get();
    assertTrue(List.of(
        "AAPL: Earnings beat expectations",
        "AAPL: Product launch gains traction",
        "AAPL: Regulatory setback",
        "AAPL: Guidance cut rattles investors").contains(event.title()));
    assertTrue(event.description().contains("Apple Inc."));
    assertTrue(event.description().contains("AAPL"));
    assertEquals(Set.of("AAPL"), event.getAffectedSymbols());
    assertTrue(event.priceFactor().compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void randomFromResources_returnsEmptyForNoStocksAndRejectsBadInputs() {
    MarketEventStrategy strategy =
        MarketEventStrategy.randomFromResources(1.0, MarketEventStrategy.DEFAULT_TEMPLATE_RESOURCE);

    assertTrue(strategy.maybeCreateEvent(List.of(), 8, new Random(4)).isEmpty());
    assertThrows(NullPointerException.class, () -> strategy.maybeCreateEvent(null, 8, new Random(4)));
    assertThrows(NullPointerException.class, () -> strategy.maybeCreateEvent(List.of(appleStock), 8, null));
    assertThrows(
        IllegalStateException.class,
        () -> MarketEventStrategy.randomFromResources(1.0, "data/missing-templates.json"));
  }

  @Test
  void advance_withoutNewEvent_clearsLastMarketEvent() {
    AtomicInteger calls = new AtomicInteger();
    MarketEventStrategy singleEventStrategy =
        (stocks, tradingDay, random) -> {
          if (calls.getAndIncrement() == 0) {
            return Optional.of(
                new MarketEvent(
                    tradingDay,
                    "AAPL: Guidance cut rattles investors",
                    "Apple Inc. lowered guidance.",
                    new SymbolMarketEventTarget(Set.of("AAPL")),
                    new BigDecimal("0.85")));
          }
          return Optional.empty();
        };
    Exchange eventExchange =
        new ExchangeBuilder("NYSE")
            .stocks(List.of(appleStock, googleStock, microsoftStock))
            .random(new Random(11))
            .dailyPriceMoveStrategy((stock, random) -> stock.getSalesPrice())
            .marketEventStrategy(singleEventStrategy)
            .build();

    eventExchange.advance();
    assertTrue(eventExchange.getLastMarketEvent().isPresent());
    assertEquals(1, eventExchange.getMarketEventHistory().size());

    eventExchange.advance();
    assertTrue(eventExchange.getLastMarketEvent().isEmpty());
    assertEquals(1, eventExchange.getMarketEventHistory().size());
  }

  @Test
  void getMarketEventsForStock_returnsOnlyMatchingEventsInChronologicalOrder() {
    AtomicInteger calls = new AtomicInteger();
    Exchange eventExchange =
        new ExchangeBuilder("NYSE")
            .stocks(List.of(appleStock, googleStock, microsoftStock))
            .random(new Random(15))
            .dailyPriceMoveStrategy((stock, random) -> stock.getSalesPrice())
            .marketEventStrategy(
                (stocks, tradingDay, random) -> switch (calls.getAndIncrement()) {
                  case 0 -> Optional.of(
                      new MarketEvent(
                          tradingDay,
                          "AAPL: Earnings beat expectations",
                          "Apple Inc. reported stronger earnings than expected.",
                          new SymbolMarketEventTarget(Set.of("AAPL")),
                          new BigDecimal("1.10")));
                  case 1 -> Optional.of(
                      new MarketEvent(
                          tradingDay,
                          "MSFT: Regulatory setback",
                          "Microsoft faces a regulatory setback.",
                          new SymbolMarketEventTarget(Set.of("MSFT")),
                          new BigDecimal("0.88")));
                  case 2 -> Optional.of(
                      new MarketEvent(
                          tradingDay,
                          "AAPL: Product launch gains traction",
                          "Apple Inc. announced strong demand for a new release.",
                          new SymbolMarketEventTarget(Set.of("AAPL")),
                          new BigDecimal("1.09")));
                  default -> Optional.empty();
                })
            .build();

    eventExchange.advance(4);

    List<MarketEvent> fullHistory = eventExchange.getMarketEventHistory();
    assertEquals(3, fullHistory.size());
    assertEquals(2, fullHistory.getFirst().day());
    assertEquals(4, fullHistory.getLast().day());

    List<MarketEvent> appleEvents = eventExchange.getMarketEventsForStock("AAPL");
    assertEquals(2, appleEvents.size());
    assertEquals("AAPL: Earnings beat expectations", appleEvents.getFirst().title());
    assertEquals("AAPL: Product launch gains traction", appleEvents.getLast().title());

    List<MarketEvent> googleEvents = eventExchange.getMarketEventsForStock("GOOGL");
    assertTrue(googleEvents.isEmpty());
  }

  @Test
  void getGainers_andLosers_doNotOverlap() {
    exchange.advance();
    exchange.advance();

    List<Stock> gainers = exchange.getGainers(10);
    List<Stock> losers = exchange.getLosers(10);

    // No stock should appear in both gainers and losers
    for (Stock gainer : gainers) {
      for (Stock loser : losers) {
        assertNotEquals(gainer.getSymbol(), loser.getSymbol());
      }
    }
  }
}
