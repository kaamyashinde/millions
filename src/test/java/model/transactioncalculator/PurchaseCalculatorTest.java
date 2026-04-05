package model.transactioncalculator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import model.core.market.stock.Stock;
import model.core.player.Share;
import model.core.trading.transactioncalculator.PurchaseCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PurchaseCalculatorTest {

  private PurchaseCalculator calculator;

  @BeforeEach
  void setUp() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    calculator = new PurchaseCalculator(share);
  }

  @Test
  void calculateGross() {
    // 10 shares × 100.00 = 1000.00
    assertEquals(new BigDecimal("1000.00"), calculator.calculateGross());
  }

  @Test
  void calculateCommission() {
    // 0.5% of 1000.00 = 5.00 (scale may vary due to BigDecimal multiply)
    assertEquals(0, new BigDecimal("5").compareTo(calculator.calculateCommission()));
  }

  @Test
  void calculateTax() {
    assertEquals(BigDecimal.ZERO, calculator.calculateTax());
  }

  @Test
  void calculateTotal() {
    // gross 1000.00 + commission 5.00 + tax 0 = 1005.00 (scale may vary)
    assertEquals(0, new BigDecimal("1005").compareTo(calculator.calculateTotal()));
  }
}