package model.trading.transaction;


import model.core.asset.Share;
import model.core.asset.Stock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.function.Function;
import model.trading.calculator.PurchaseCalculator;
import model.trading.calculator.SaleCalculator;
import model.trading.transaction.TransactionSizing;
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
    assertEquals(0, TransactionSizing.maxQuantityForBudget(stock, null).signum());
    assertEquals(0, TransactionSizing.maxQuantityForBudget(stock, BigDecimal.ZERO).signum());
    assertEquals(0, TransactionSizing.maxQuantityForBudget(stock, new BigDecimal("-1")).signum());
    assertThrows(NullPointerException.class, () -> TransactionSizing.maxQuantityForBudget(null, BigDecimal.ONE));
  }

  @Test
  void maxQuantityForBudget_zeroWhenAssetPriceIsNonPositiveOrBudgetTooSmall() {
    Stock free = new Stock("FREE", "Free");
    free.addNewSalesPrice(BigDecimal.ZERO);

    assertEquals(0, TransactionSizing.maxQuantityForBudget(free, BigDecimal.TEN).signum());
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

  @Test
  void maxQuantityForTargetNet_zeroForInvalidTargetsAndLots() {
    Stock stock = new Stock("TST", "Test");
    stock.addNewSalesPrice(new BigDecimal("100"));
    Share lot = new Share(stock, new BigDecimal("10"), new BigDecimal("50"));
    Share emptyLot = new Share(stock, BigDecimal.ZERO, new BigDecimal("50"));

    assertEquals(0, TransactionSizing.maxQuantityForTargetNet(lot, null).signum());
    assertEquals(0, TransactionSizing.maxQuantityForTargetNet(lot, BigDecimal.ZERO).signum());
    assertEquals(0, TransactionSizing.maxQuantityForTargetNet(emptyLot, BigDecimal.TEN).signum());
    assertThrows(NullPointerException.class, () -> TransactionSizing.maxQuantityForTargetNet(null, BigDecimal.ONE));
  }

  @Test
  @SuppressWarnings("unchecked")
  void refineDownStepsDownUntilMetricFits() throws Exception {
    Method method = TransactionSizing.class.getDeclaredMethod(
        "refineDown",
        BigDecimal.class,
        BigDecimal.class,
        Function.class);
    method.setAccessible(true);
    Function<BigDecimal, BigDecimal> metric = quantity -> quantity.compareTo(new BigDecimal("0.9999")) > 0
        ? BigDecimal.TEN
        : BigDecimal.ZERO;

    BigDecimal refined = (BigDecimal) method.invoke(null, BigDecimal.ONE, BigDecimal.ONE, metric);

    assertEquals(0, refined.compareTo(new BigDecimal("0.9999")));
  }
}
