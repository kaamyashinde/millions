package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
}