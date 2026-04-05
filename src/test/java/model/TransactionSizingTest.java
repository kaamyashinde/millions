package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import model.market.Share;
import model.market.Stock;
import model.trading.transaction.TransactionSizing;
import model.trading.transactioncalculator.PurchaseCalculator;
import model.trading.transactioncalculator.SaleCalculator;
import org.junit.jupiter.api.Test;

class TransactionSizingTest {

  @Test
  void maxQuantityForBudget_respectsPurchaseCommission() {
    Stock stock = new Stock("TST", "Test");
    stock.addNewSalesPrice(new BigDecimal("100"));
    BigDecimal budget = new BigDecimal("1005");
    BigDecimal q = TransactionSizing.maxQuantityForBudget(stock, budget);
    Share share = new Share(stock, q, stock.getSalesPrice());
    BigDecimal total = new PurchaseCalculator(share).calculateTotal();
    assertTrue(total.compareTo(budget) <= 0);
    assertTrue(q.compareTo(BigDecimal.ZERO) > 0);
  }

  @Test
  void maxQuantityForBudget_zeroForNonPositiveBudget() {
    Stock stock = new Stock("TST", "Test");
    stock.addNewSalesPrice(new BigDecimal("10"));
    assertEquals(0, TransactionSizing.maxQuantityForBudget(stock, BigDecimal.ZERO).signum());
    assertEquals(0, TransactionSizing.maxQuantityForBudget(stock, new BigDecimal("-1")).signum());
  }

  @Test
  void maxQuantityForTargetNet_fullLotWhenUnderCap() {
    Stock stock = new Stock("TST", "Test");
    stock.addNewSalesPrice(new BigDecimal("100"));
    Share lot = new Share(stock, new BigDecimal("5"), new BigDecimal("50"));
    BigDecimal target = new BigDecimal("10000");
    BigDecimal q = TransactionSizing.maxQuantityForTargetNet(lot, target);
    assertEquals(0, q.compareTo(new BigDecimal("5")));
  }

  @Test
  void maxQuantityForTargetNet_partialBelowCap() {
    Stock stock = new Stock("TST", "Test");
    stock.addNewSalesPrice(new BigDecimal("100"));
    Share lot = new Share(stock, new BigDecimal("10"), new BigDecimal("50"));
    BigDecimal target = new BigDecimal("200");
    BigDecimal q = TransactionSizing.maxQuantityForTargetNet(lot, target);
    Share slice = new Share(stock, q, lot.getPurchasePrice());
    BigDecimal net = new SaleCalculator(slice).calculateTotal();
    assertTrue(net.compareTo(target) <= 0);
    assertTrue(q.signum() > 0);
  }
}
