package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.math.BigDecimal;
import model.core.market.stock.Stock;
import model.core.player.Share;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShareTest {

  private Stock testStock;
  private Share testShare;
  private BigDecimal testQuantity;
  private BigDecimal testPurchasePrice;

  @BeforeEach
  void setUp() {
    testStock = new Stock("AAPL", "Apple Inc.");
    testQuantity = new BigDecimal("10.5");
    testPurchasePrice = new BigDecimal("150.75");
    testShare = new Share(testStock, testQuantity, testPurchasePrice);
  }

  @Test
  void getAsset() {
    assertEquals(testStock, testShare.getAsset());
  }

  @Test
  void getQuantity() {
    assertEquals(testQuantity, testShare.getQuantity());
    assertEquals(new BigDecimal("10.5"), testShare.getQuantity());
  }

  @Test
  void getPurchasePrice() {
    assertEquals(testPurchasePrice, testShare.getPurchasePrice());
    assertEquals(new BigDecimal("150.75"), testShare.getPurchasePrice());
  }

  @Test
  void constructorSetsValuesCorrectly() {
    Stock newStock = new Stock("GOOGL", "Alphabet Inc.");
    BigDecimal quantity = new BigDecimal("5");
    BigDecimal price = new BigDecimal("2800.00");

    Share share = new Share(newStock, quantity, price);

    assertNotNull(share);
    assertEquals(newStock, share.getAsset());
    assertEquals(quantity, share.getQuantity());
    assertEquals(price, share.getPurchasePrice());
  }

  @Test
  void shareWithZeroQuantity() {
    Share zeroShare = new Share(testStock, BigDecimal.ZERO, testPurchasePrice);
    assertEquals(BigDecimal.ZERO, zeroShare.getQuantity());
  }

  @Test
  void shareWithDifferentPrices() {
    BigDecimal lowPrice = new BigDecimal("10.00");
    BigDecimal highPrice = new BigDecimal("9999.99");

    Share lowPriceShare = new Share(testStock, testQuantity, lowPrice);
    Share highPriceShare = new Share(testStock, testQuantity, highPrice);

    assertEquals(lowPrice, lowPriceShare.getPurchasePrice());
    assertEquals(highPrice, highPriceShare.getPurchasePrice());
  }
}