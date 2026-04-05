package model.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import model.core.market.Share;
import model.core.market.Stock;
import model.core.trading.transaction.Purchase;
import model.core.trading.transaction.Sale;
import model.core.trading.transaction.Transaction;
import model.core.trading.transaction.TransactionArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionArchiveTest {

  private TransactionArchive archive;
  private Share share;

  @BeforeEach
  void setUp() {
    archive = new TransactionArchive();
    Stock stock = new Stock("AAPL", "Apple Inc.");
    stock.addNewSalesPrice(new BigDecimal("200.00"));
    share = new Share(stock, new BigDecimal("10"), new BigDecimal("100.00"));
  }

  @Test
  void addTransaction() {
    Purchase purchase = new Purchase(share, 1);
    assertTrue(archive.addTransaction(purchase));
    assertFalse(archive.isEmpty());
  }

  @Test
  void isEmpty() {
    assertTrue(archive.isEmpty());
    archive.addTransaction(new Purchase(share, 1));
    assertFalse(archive.isEmpty());
  }

  @Test
  void getTransactions() {
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Purchase(share, 3));
    List<Transaction> firstTwo = archive.getTransactions(2);
    assertEquals(2, firstTwo.size());
    assertEquals(1, firstTwo.get(0).getDay());
    assertEquals(2, firstTwo.get(1).getDay());
  }

  @Test
  void getPurchases() {
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Purchase(share, 2));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Purchase(share, 3));
    List<Purchase> purchasesUpToDay2 = archive.getPurchases(2);
    assertEquals(2, purchasesUpToDay2.size());
    assertTrue(purchasesUpToDay2.stream().allMatch(p -> p.getDay() <= 2));
    List<Purchase> purchasesUpToDay1 = archive.getPurchases(1);
    assertEquals(1, purchasesUpToDay1.size());
    assertEquals(1, purchasesUpToDay1.get(0).getDay());
  }

  @Test
  void getSales() {
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Sale(share, 3));
    List<Sale> salesUpToDay2 = archive.getSales(2);
    assertEquals(1, salesUpToDay2.size());
    assertEquals(2, salesUpToDay2.get(0).getDay());
    List<Sale> salesUpToDay3 = archive.getSales(3);
    assertEquals(2, salesUpToDay3.size());
  }

  @Test
  void countDistinctDay() {
    assertEquals(0, archive.countDistinctDay());
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Purchase(share, 1));
    assertEquals(2, archive.countDistinctDay());
  }
}