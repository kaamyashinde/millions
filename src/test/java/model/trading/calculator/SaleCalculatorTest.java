package model.trading.calculator;


import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import model.core.asset.Share;
import model.core.asset.Stock;
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
  void calculateTaxReturnsZeroWhenLoss() {
    Stock losingStock = new Stock("LOSE", "Losing Inc.");
    losingStock.addNewSalesPrice(new BigDecimal("50.00"));
    Share losingShare = new Share(losingStock, new BigDecimal("10"), new BigDecimal("100.00"));
    SaleCalculator lossCalculator = new SaleCalculator(losingShare);

    assertEquals(BigDecimal.ZERO, lossCalculator.calculateTax());
  }

  @Test
  void calculateTotalUsesExplicitSalePriceWhenProvided() {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
    SaleCalculator historicalCalculator = new SaleCalculator(share, new BigDecimal("150.00"));

    assertEquals(0, new BigDecimal("1339.50").compareTo(historicalCalculator.calculateTotal()));
  }

  @Test
  void calculateTotal() {
    assertEquals(0, new BigDecimal("1686").compareTo(calculator.calculateTotal()));
  }

  @Test
  void calculateTotalWhenLoss() {
    Stock losingStock = new Stock("LOSE", "Losing Inc.");
    losingStock.addNewSalesPrice(new BigDecimal("50.00"));
    Share losingShare = new Share(losingStock, new BigDecimal("10"), new BigDecimal("100.00"));
    SaleCalculator lossCalculator = new SaleCalculator(losingShare);

    // gross=500, commission=5, tax=0 → total = 500 - 5 - 0 = 495
    assertEquals(0, new BigDecimal("495").compareTo(lossCalculator.calculateTotal()));
  }
}
