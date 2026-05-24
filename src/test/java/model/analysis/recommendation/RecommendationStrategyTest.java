package model.analysis.recommendation;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import model.core.asset.Stock;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecommendationStrategyTest {

  @Nested
  class Trend {

    @Test
    void recommend_returnsBuyForClearPositiveTrend() {
      assertEquals(
          StockRecommendation.BUY,
          RecommendationStrategy.TREND.recommend(prices("100.00", "101.00", "102.00", "104.00")));
    }

    @Test
    void recommend_returnsHoldForNearlyFlatTrend() {
      assertEquals(
          StockRecommendation.HOLD,
          RecommendationStrategy.TREND.recommend(prices("100.00", "100.40", "100.20", "100.70")));
    }

    @Test
    void recommend_returnsSellForClearNegativeTrend() {
      assertEquals(
          StockRecommendation.SELL,
          RecommendationStrategy.TREND.recommend(prices("100.00", "98.50", "97.50", "95.00")));
    }

    @Test
    void recommend_returnsHoldWhenHistoryIsTooShort() {
      assertEquals(StockRecommendation.HOLD, RecommendationStrategy.TREND.recommend(prices("100.00")));
    }

    @Test
    void recommend_matchesStockHistoricalPrices() {
      Stock stock = stockWithPrices("100.00", "101.00", "102.00", "104.00");

      assertEquals(
          StockRecommendation.BUY, RecommendationStrategy.TREND.recommend(stock.getHistoricalPrices()));
    }
  }

  @Nested
  class Momentum {

    @Test
    void recommend_returnsBuyWhenRecentHalfAcceleratesVersusOlderHalf() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100"),
              new BigDecimal("101"),
              new BigDecimal("103"),
              new BigDecimal("106"));

      assertEquals(StockRecommendation.BUY, RecommendationStrategy.MOMENTUM.recommend(prices));
    }

    @Test
    void recommend_returnsHoldWhenMomentumIsMild() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100.00"),
              new BigDecimal("100.40"),
              new BigDecimal("100.20"),
              new BigDecimal("100.70"));

      assertEquals(StockRecommendation.HOLD, RecommendationStrategy.MOMENTUM.recommend(prices));
    }

    @Test
    void recommend_returnsSellWhenRecentMoveIsMuchWeakerThanOlderMove() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100"),
              new BigDecimal("102"),
              new BigDecimal("99"),
              new BigDecimal("98"));

      assertEquals(StockRecommendation.SELL, RecommendationStrategy.MOMENTUM.recommend(prices));
    }

    @Test
    void recommend_returnsHoldWhenFewerThanFourPrices() {
      List<BigDecimal> prices =
          List.of(new BigDecimal("100"), new BigDecimal("101"), new BigDecimal("104"));

      assertEquals(StockRecommendation.HOLD, RecommendationStrategy.MOMENTUM.recommend(prices));
    }
  }

  @Nested
  class MeanReversion {

    @Test
    void recommend_returnsBuyWhenLatestPriceIsFarBelowRecentAverage() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100"),
              new BigDecimal("100"),
              new BigDecimal("100"),
              new BigDecimal("97"));

      assertEquals(StockRecommendation.BUY, RecommendationStrategy.MEAN_REVERSION.recommend(prices));
    }

    @Test
    void recommend_returnsSellWhenLatestPriceIsFarAboveRecentAverage() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100"),
              new BigDecimal("100"),
              new BigDecimal("100"),
              new BigDecimal("103"));

      assertEquals(StockRecommendation.SELL, RecommendationStrategy.MEAN_REVERSION.recommend(prices));
    }

    @Test
    void recommend_returnsHoldWhenPriceIsCloseToAverage() {
      List<BigDecimal> prices =
          List.of(
              new BigDecimal("100"),
              new BigDecimal("100.4"),
              new BigDecimal("100.2"),
              new BigDecimal("100.3"));

      assertEquals(StockRecommendation.HOLD, RecommendationStrategy.MEAN_REVERSION.recommend(prices));
    }

    @Test
    void recommend_returnsHoldWhenHistoryIsTooShort() {
      List<BigDecimal> prices = List.of(new BigDecimal("100"));

      assertEquals(StockRecommendation.HOLD, RecommendationStrategy.MEAN_REVERSION.recommend(prices));
    }
  }

  private static List<BigDecimal> prices(String... values) {
    return stockWithPrices(values).getHistoricalPrices();
  }

  private static Stock stockWithPrices(String... prices) {
    Stock stock = new Stock("AAPL", "Apple Inc.");
    for (String price : prices) {
      stock.addNewSalesPrice(new BigDecimal(price));
    }
    return stock;
  }
}
