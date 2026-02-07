package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
  void getWeek_initialWeekIsOne() {
    assertEquals(1, exchange.getWeek());
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
  void buy_transactionHasCorrectWeek() {
    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    assertEquals(1, transaction.getWeek());
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
  void sell_transactionHasCorrectWeek() {
    exchange.buy("AAPL", new BigDecimal("10"), player);
    Share shareToSell = player.getPortfolio().getShares().getFirst();

    Transaction transaction = exchange.sell(shareToSell, player);
    assertEquals(1, transaction.getWeek());
  }

  @Test
  void advance_incrementsWeek() {
    assertEquals(1, exchange.getWeek());

    exchange.advance();
    assertEquals(2, exchange.getWeek());

    exchange.advance();
    assertEquals(3, exchange.getWeek());
  }

  @Test
  void advance_updateStockPrices() {
    BigDecimal initialApplePrice = appleStock.getSalesPrice();
    BigDecimal initialGooglePrice = googleStock.getSalesPrice();

    exchange.advance();

    // Prices should change (within ±5% range)
    BigDecimal newApplePrice = appleStock.getSalesPrice();
    BigDecimal newGooglePrice = googleStock.getSalesPrice();

    // Verify prices are within expected range (95% to 105% of original)
    BigDecimal appleLowerBound = initialApplePrice.multiply(new BigDecimal("0.95"));
    BigDecimal appleUpperBound = initialApplePrice.multiply(new BigDecimal("1.05"));
    assertTrue(newApplePrice.compareTo(appleLowerBound) >= 0);
    assertTrue(newApplePrice.compareTo(appleUpperBound) <= 0);

    BigDecimal googleLowerBound = initialGooglePrice.multiply(new BigDecimal("0.95"));
    BigDecimal googleUpperBound = initialGooglePrice.multiply(new BigDecimal("1.05"));
    assertTrue(newGooglePrice.compareTo(googleLowerBound) >= 0);
    assertTrue(newGooglePrice.compareTo(googleUpperBound) <= 0);
  }

  @Test
  void buyAfterAdvance_usesCurrentWeek() {
    exchange.advance();
    exchange.advance();

    Transaction transaction = exchange.buy("AAPL", new BigDecimal("5"), player);
    assertEquals(3, transaction.getWeek());
  }
}