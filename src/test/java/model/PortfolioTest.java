package model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

class PortfolioTest {

  private Portfolio portfolio;
  private Stock appleStock;
  private Stock googleStock;
  private Share appleShare;
  private Share googleShare;

  @BeforeEach
  void setUp() {
    portfolio = new Portfolio();
    appleStock = new Stock("AAPL", "Apple Inc.");
    appleStock.addNewSalesPrice(new BigDecimal("150.00"));
    googleStock = new Stock("GOOGL", "Alphabet Inc.");
    googleStock.addNewSalesPrice(new BigDecimal("2800.00"));
    appleShare = new Share(appleStock, new BigDecimal("10"), new BigDecimal("150.00"));
    googleShare = new Share(googleStock, new BigDecimal("5"), new BigDecimal("2800.00"));
  }

  @Test
  void addShare() {
    assertTrue(portfolio.addShare(appleShare));
    assertEquals(1, portfolio.getShares().size());
    assertTrue(portfolio.getShares().contains(appleShare));
  }

  @Test
  void addShareThrowsExceptionForNull() {
    assertThrows(NullPointerException.class, () -> portfolio.addShare(null));
  }

  @Test
  void addMultipleShares() {
    portfolio.addShare(appleShare);
    portfolio.addShare(googleShare);
    assertEquals(2, portfolio.getShares().size());
  }

  @Test
  void removeShare() {
    portfolio.addShare(appleShare);
    assertTrue(portfolio.removeShare(appleShare));
    assertEquals(0, portfolio.getShares().size());
  }

  @Test
  void removeShareReturnsFalseIfNotFound() {
    assertFalse(portfolio.removeShare(appleShare));
  }

  @Test
  void removeShareThrowsExceptionForNull() {
    assertThrows(NullPointerException.class, () -> portfolio.removeShare(null));
  }

  @Test
  void getShares() {
    assertTrue(portfolio.getShares().isEmpty());
    
    portfolio.addShare(appleShare);
    portfolio.addShare(googleShare);
    
    List<Share> shares = portfolio.getShares();
    assertEquals(2, shares.size());
    assertTrue(shares.contains(appleShare));
    assertTrue(shares.contains(googleShare));
  }

  @Test
  void getSharesBasedOnSymbol() {
    Share anotherAppleShare = new Share(appleStock, new BigDecimal("20"), new BigDecimal("155.00"));
    
    portfolio.addShare(appleShare);
    portfolio.addShare(googleShare);
    portfolio.addShare(anotherAppleShare);
    
    List<Share> appleShares = portfolio.getSharesBasedOnSymbol("AAPL");
    assertEquals(2, appleShares.size());
    assertTrue(appleShares.contains(appleShare));
    assertTrue(appleShares.contains(anotherAppleShare));
    
    List<Share> googleShares = portfolio.getSharesBasedOnSymbol("GOOGL");
    assertEquals(1, googleShares.size());
    assertTrue(googleShares.contains(googleShare));
  }

  @Test
  void getSharesBasedOnSymbolReturnsEmptyListIfNotFound() {
    portfolio.addShare(appleShare);
    List<Share> shares = portfolio.getSharesBasedOnSymbol("MSFT");
    assertTrue(shares.isEmpty());
  }

  @Test
  void getSharesBasedOnSymbolThrowsExceptionForNull() {
    assertThrows(NullPointerException.class, () -> portfolio.getSharesBasedOnSymbol(null));
  }

  @Test
  void containsShare() {
    assertFalse(portfolio.containsShare(appleShare));
    
    portfolio.addShare(appleShare);
    assertTrue(portfolio.containsShare(appleShare));
    assertFalse(portfolio.containsShare(googleShare));
  }

  @Test
  void emptyPortfolioHasNoShares() {
    Portfolio emptyPortfolio = new Portfolio();
    assertTrue(emptyPortfolio.getShares().isEmpty());
    assertEquals(0, emptyPortfolio.getShares().size());
  }

  @Test
  void getNetWorthOfEmptyPortfolio() {
    assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth()));
  }

  @Test
  void getNetWorthWithSingleShare() {
    // Apple: 10 shares at 150.00 = 1500.00 - 15.00 commission = 1485.00
    portfolio.addShare(appleShare);
    assertEquals(0, new BigDecimal("1485.00").compareTo(portfolio.getNetWorth()));
  }

  @Test
  void getNetWorthWithMultipleShares() {
    // Apple: 10 shares at 150.00 = 1500.00 - 15.00 commission = 1485.00
    // Google: 5 shares at 2800.00 = 14000.00 - 140.00 commission = 13860.00
    // Total = 15345.00
    portfolio.addShare(appleShare);
    portfolio.addShare(googleShare);
    assertEquals(0, new BigDecimal("15345.00").compareTo(portfolio.getNetWorth()));
  }

  @Test
  void getNetWorthWithMultipleSharesOfSameStock() {
    Share anotherAppleShare = new Share(appleStock, new BigDecimal("5"), new BigDecimal("160.00"));
    // First Apple: 10 shares at 150.00 = 1500.00 - 15.00 commission = 1485.00
    // Second Apple: 5 shares at 150.00 (stock price) = 750.00 - 7.50 commission = 742.50
    // Total = 2227.50
    portfolio.addShare(appleShare);
    portfolio.addShare(anotherAppleShare);
    assertEquals(0, new BigDecimal("2227.50").compareTo(portfolio.getNetWorth()));
  }
}