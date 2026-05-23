package model.analysis.recommendation;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockRecommendationServiceTest {

  private StockRecommendationService service;

  @BeforeEach
  void setUp() {
    service = new StockRecommendationService();
  }

  @Test
  void recommend_listOverloadRejectsNull() {
    NullPointerException error =
        assertThrows(NullPointerException.class, () -> service.recommend((List<BigDecimal>) null));

    assertEquals("Historical prices cannot be null", error.getMessage());
  }

  @Test
  void constructor_rejectsNullStrategy() {
    NullPointerException error =
        assertThrows(NullPointerException.class, () -> new StockRecommendationService(null));

    assertEquals("Recommendation strategy cannot be null", error.getMessage());
  }

  @Test
  void recommend_delegatesToInjectedStrategy() {
    StockRecommendationService custom =
        new StockRecommendationService(RecommendationStrategy.MEAN_REVERSION);
    Stock stock = stockWithPrices("100", "100", "100", "97");

    assertEquals(StockRecommendation.BUY, custom.recommend(stock));
  }

  @Test
  void recommend_listDelegatesToInjectedStrategy() {
    StockRecommendationService custom =
        new StockRecommendationService(RecommendationStrategy.MOMENTUM);
    List<BigDecimal> prices =
        List.of(
            new BigDecimal("100"),
            new BigDecimal("101"),
            new BigDecimal("103"),
            new BigDecimal("106"));

    assertEquals(StockRecommendation.BUY, custom.recommend(prices));
  }

  private static Stock stockWithPrices(String... prices) {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
