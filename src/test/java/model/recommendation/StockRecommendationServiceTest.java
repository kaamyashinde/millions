package model.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.util.List;
import model.core.market.stock.Stock;
import model.core.market.stock.recommendation.StockRecommendation;
import model.core.market.stock.recommendation.StockRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockRecommendationServiceTest {

  private StockRecommendationService service;

  @BeforeEach
  void setUp() {
    service = new StockRecommendationService();
  }

  @Test
  void recommend_returnsBuyForClearPositiveTrend() {
    Stock stock = stockWithPrices("100.00", "101.00", "102.00", "104.00");

    assertEquals(StockRecommendation.BUY, service.recommend(stock));
  }

  @Test
  void recommend_returnsHoldForNearlyFlatTrend() {
    Stock stock = stockWithPrices("100.00", "100.40", "100.20", "100.70");

    assertEquals(StockRecommendation.HOLD, service.recommend(stock));
  }

  @Test
  void recommend_returnsSellForClearNegativeTrend() {
    Stock stock = stockWithPrices("100.00", "98.50", "97.50", "95.00");

    assertEquals(StockRecommendation.SELL, service.recommend(stock));
  }

  @Test
  void recommend_returnsHoldWhenHistoryIsTooShort() {
    Stock stock = stockWithPrices("100.00");

    assertEquals(StockRecommendation.HOLD, service.recommend(stock));
  }

  @Test
  void recommend_returnsHoldWhenRecentStartPriceIsZero() {
    List<BigDecimal> prices = List.of(
        BigDecimal.ZERO,
        new BigDecimal("100.00"),
        new BigDecimal("101.00"),
        new BigDecimal("104.00"));

    assertEquals(StockRecommendation.HOLD, service.recommend(prices));
  }

  @Test
  void recommend_listOverloadRejectsNull() {
    NullPointerException error =
        assertThrows(NullPointerException.class, () -> service.recommend((List<BigDecimal>) null));

    assertEquals("Historical prices cannot be null", error.getMessage());
  }

  @Test
  void recommend_stockOverloadRejectsNull() {
    NullPointerException error =
        assertThrows(NullPointerException.class, () -> service.recommend((Stock) null));

    assertEquals("Stock cannot be null", error.getMessage());
  }

  /**
   * Creates a stock with the provided ordered prices.
   *
   * @param prices ordered prices, oldest to newest
   * @return stock populated with those prices
   */
  private static Stock stockWithPrices(String... prices) {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
