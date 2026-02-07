package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SaleCalculatorTest {

  private SaleCalculator calculator;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    calculator = new SaleCalculator(share);
  }

  @Test
  void calculateGross() {
    assertEquals(new BigDecimal("2000.00"), calculator.calculateGross());
  }

  @Test
  void calculateCommission() {
    assertEquals(0, new BigDecimal("20").compareTo(calculator.calculateCommission()));
  }

  @Test
  void calculateTax() {
    assertEquals(0, new BigDecimal("294").compareTo(calculator.calculateTax()));
  }

  @Test
  void calculateTotal() {
    assertEquals(0, new BigDecimal("1686").compareTo(calculator.calculateTotal()));
  }
}