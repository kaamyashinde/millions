package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.math.BigDecimal;
import java.util.List;
import model.transaction.Purchase;
import model.transaction.Sale;
import model.transaction.Transaction;
import model.transaction.TransactionArchive;
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
    assertEquals(1, firstTwo.get(0).getWeek());
    assertEquals(2, firstTwo.get(1).getWeek());
  }

  @Test
  void getPurchases() {
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Purchase(share, 2));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Purchase(share, 3));
    List<Purchase> purchasesUpToWeek2 = archive.getPurchases(2);
    assertEquals(2, purchasesUpToWeek2.size());
    assertTrue(purchasesUpToWeek2.stream().allMatch(p -> p.getWeek() <= 2));
    List<Purchase> purchasesUpToWeek1 = archive.getPurchases(1);
    assertEquals(1, purchasesUpToWeek1.size());
    assertEquals(1, purchasesUpToWeek1.get(0).getWeek());
  }

  @Test
  void getSales() {
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Sale(share, 3));
    List<Sale> salesUpToWeek2 = archive.getSales(2);
    assertEquals(1, salesUpToWeek2.size());
    assertEquals(2, salesUpToWeek2.get(0).getWeek());
    List<Sale> salesUpToWeek3 = archive.getSales(3);
    assertEquals(2, salesUpToWeek3.size());
  }

  @Test
  void countDistinctWeek() {
    assertEquals(0, archive.countDistinctWeek());
    archive.addTransaction(new Purchase(share, 1));
    archive.addTransaction(new Sale(share, 2));
    archive.addTransaction(new Purchase(share, 1));
    assertEquals(2, archive.countDistinctWeek());
  }
}