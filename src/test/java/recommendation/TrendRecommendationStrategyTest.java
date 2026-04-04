package recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TrendRecommendationStrategyTest {

  private TrendRecommendationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new TrendRecommendationStrategy();
  }

  @Test
  void recommend_returnsBuyForClearPositiveTrend() {
    List<BigDecimal> prices = prices("100.00", "101.00", "102.00", "104.00");

    assertEquals(StockRecommendation.BUY, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldForNearlyFlatTrend() {
    List<BigDecimal> prices = prices("100.00", "100.40", "100.20", "100.70");

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsSellForClearNegativeTrend() {
    List<BigDecimal> prices = prices("100.00", "98.50", "97.50", "95.00");

    assertEquals(StockRecommendation.SELL, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldWhenHistoryIsTooShort() {
    List<BigDecimal> prices = prices("100.00");

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }

  @Test
  void recommend_matchesStockHistoricalPrices() {
    Stock stock = stockWithPrices("100.00", "101.00", "102.00", "104.00");

    assertEquals(StockRecommendation.BUY, strategy.recommend(stock.getHistoricalPrices()));
  }

  private static List<BigDecimal> prices(String... values) {
    Stock stock = stockWithPrices(values);
    return stock.getHistoricalPrices();
  }

  private static Stock stockWithPrices(String... prices) {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
