package model;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StockTest {

  private Stock stock;

  @BeforeEach
  void setUp() {
    stock = new Stock("AAPL", "Apple Inc.", List.of(
        BigDecimal.valueOf(150.50),
        BigDecimal.valueOf(152.25),
        BigDecimal.valueOf(148.00)));
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
  void getPrice() {
    assertNotNull(stock.getPrice());
    assertEquals(3, stock.getPrice().size());
    assertEquals(BigDecimal.valueOf(150.50), stock.getPrice().get(0));
    assertEquals(BigDecimal.valueOf(152.25), stock.getPrice().get(1));
    assertEquals(BigDecimal.valueOf(148.00), stock.getPrice().get(2));
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
            + stock.getPrice()
            + '}';
    assertEquals(expected, stock.toString());
  }
}