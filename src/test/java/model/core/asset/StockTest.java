package model.core.asset;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StockTest {

  private Stock stock;
  private List<BigDecimal> pricesAdded;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple Inc.");
    pricesAdded = new ArrayList<>();
    pricesAdded.add(BigDecimal.valueOf(150.50));
    pricesAdded.add(BigDecimal.valueOf(152.25));
    pricesAdded.add(BigDecimal.valueOf(148.00));
    pricesAdded.forEach(stock::addNewSalesPrice);
  }

  @Test
  void getSymbol() {
    assertEquals("AAPL", stock.getSymbol());
  }

  @Test
  void getCompany() {
    assertEquals("Apple Inc.", stock.getCompany());
  }

  @Test
  void getSalesPriceReturnsLatestPrice() {
    assertEquals(BigDecimal.valueOf(148.00), stock.getSalesPrice());
  }

  @Test
  void addNewSalesPriceAppendsAndGetSalesPriceReturnsNewLatest() {
    BigDecimal newPrice = BigDecimal.valueOf(160.75);
    stock.addNewSalesPrice(newPrice);
    assertEquals(newPrice, stock.getSalesPrice());
  }

  @Test
  void testToString() {
    String expected =
        "Stock{"
            + "symbol='"
            + stock.getSymbol()
            + '\''
            + ", company='"
            + stock.getCompany()
            + '\''
            + ", price="
            + pricesAdded
            + '}';
    assertEquals(expected, stock.toString());
  }

  @Test
  void constructorThrowsWhenSymbolIsNull() {
    NullPointerException thrown =
        assertThrows(
            NullPointerException.class,
            () -> new Stock(null, "Apple Inc."));
    assertEquals("Symbol cannot be null", thrown.getMessage());
  }

  @Test
  void constructorThrowsWhenCompanyIsNull() {
    NullPointerException thrown =
        assertThrows(
            NullPointerException.class,
            () -> new Stock("AAPL", null));
    assertEquals("Company cannot be null", thrown.getMessage());
  }

  @Test
  void addNewSalesPriceThrowsWhenPriceIsNull() {
    NullPointerException thrown =
        assertThrows(
            NullPointerException.class,
            () -> stock.addNewSalesPrice(null));
    assertEquals("Price cannot be null", thrown.getMessage());
  }

  @Test
  void getHistoricalPricesReturnsAllPrices() {
    assertEquals(pricesAdded, stock.getHistoricalPrices());
  }

  @Test
  void getHighestPriceReturnsMaximumPrice() {
    assertEquals(BigDecimal.valueOf(152.25), stock.getHighestPrice());
  }

  @Test
  void getLowestPriceReturnsMinimumPrice() {
    assertEquals(BigDecimal.valueOf(148.00), stock.getLowestPrice());
  }

  @Test
  void getLatestPriceChangeReturnsDifferenceBetweenLastTwoPrices() {
    assertEquals(BigDecimal.valueOf(-4.25), stock.getLatestPriceChange());
  }

  @Test
  void getLatestPriceChangeReturnsZeroWhenOnlyOnePriceExists() {
    Stock singlePriceStock = new Stock("AAPL", "Apple Inc.");
    singlePriceStock.addNewSalesPrice(BigDecimal.valueOf(150.00));
    assertEquals(BigDecimal.ZERO, singlePriceStock.getLatestPriceChange());
  }

  @Test
  void getHistoricalPricesIsUnmodifiable() {
    assertThrows(
        UnsupportedOperationException.class,
        () -> stock.getHistoricalPrices().add(BigDecimal.ONE));
  }
}
