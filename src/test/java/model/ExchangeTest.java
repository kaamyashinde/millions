package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeTest {

  private Exchange exchange;
  private Stock appleStock;
  private Stock googleStock;
  private Stock microsoftStock;
  private Player player;

  @BeforeEach
  void setUp() {
    appleStock = new Stock("AAPL", "Apple Inc.");
    appleStock.addNewSalesPrice(new BigDecimal("150.00"));

    googleStock = new Stock("GOOGL", "Alphabet Inc.");
    googleStock.addNewSalesPrice(new BigDecimal("2800.00"));

    microsoftStock = new Stock("MSFT", "Microsoft Corporation");
    microsoftStock.addNewSalesPrice(new BigDecimal("300.00"));

    List<Stock> stocks = List.of(appleStock, googleStock, microsoftStock);
    exchange = new Exchange("NYSE", stocks);

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
  void hasStock_returnsTrueForExistingStock() {
    assertTrue(exchange.hasStock("AAPL"));
    assertTrue(exchange.hasStock("GOOGL"));
    assertTrue(exchange.hasStock("MSFT"));
  }

  @Test
  void hasStock_returnsFalseForNonExistingStock() {
    assertFalse(exchange.hasStock("TSLA"));
    assertFalse(exchange.hasStock("AMZN"));
  }

  @Test
  void getStock_returnsCorrectStock() {
    Stock retrievedStock = exchange.getStock("AAPL");
    assertNotNull(retrievedStock);
    assertEquals("AAPL", retrievedStock.getSymbol());
    assertEquals("Apple Inc.", retrievedStock.getCompany());
  }

  @Test
  void getStock_returnsNullForNonExistingStock() {
    assertNull(exchange.getStock("TSLA"));
  }

  @Test
  void findStocks_findsBySymbol() {
    List<Stock> results = exchange.findStocks("AAPL");
    assertEquals(1, results.size());
    assertEquals("AAPL", results.getFirst().getSymbol());
  }

  @Test
  void findStocks_findsByCompanyName() {
    List<Stock> results = exchange.findStocks("Microsoft");
    assertEquals(1, results.size());
    assertEquals("MSFT", results.getFirst().getSymbol());
  }

  @Test
  void findStocks_findsByPartialMatch() {
    List<Stock> results = exchange.findStocks("Inc");
    assertEquals(2, results.size());
  }

  @Test
  void findStocks_isCaseInsensitive() {
    List<Stock> resultsByLowerCase = exchange.findStocks("apple");
    List<Stock> resultsByUpperCase = exchange.findStocks("APPLE");

    assertEquals(1, resultsByLowerCase.size());
    assertEquals(1, resultsByUpperCase.size());
    assertEquals(resultsByLowerCase.getFirst().getSymbol(),
        resultsByUpperCase.getFirst().getSymbol());
  }

  @Test
  void findStocks_returnsEmptyListWhenNoMatch() {
    List<Stock> results = exchange.findStocks("Tesla");
    assertTrue(results.isEmpty());
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
    BigDecimal initialApplePrice = appleStock.getSalesPrice();
    BigDecimal initialGooglePrice = googleStock.getSalesPrice();

    exchange.advance();

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
