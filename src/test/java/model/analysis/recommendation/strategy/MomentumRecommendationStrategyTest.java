package model.analysis.recommendation.strategy;


import model.analysis.recommendation.StockRecommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MomentumRecommendationStrategyTest {

  private MomentumRecommendationStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new MomentumRecommendationStrategy();
  }

  @Test
  void recommend_returnsBuyWhenRecentHalfAcceleratesVersusOlderHalf() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("101"),
        new BigDecimal("103"),
        new BigDecimal("106"));

    assertEquals(StockRecommendation.BUY, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldWhenMomentumIsMild() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100.00"),
        new BigDecimal("100.40"),
        new BigDecimal("100.20"),
        new BigDecimal("100.70"));

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsSellWhenRecentMoveIsMuchWeakerThanOlderMove() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("102"),
        new BigDecimal("99"),
        new BigDecimal("98"));

    assertEquals(StockRecommendation.SELL, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldWhenFewerThanFourPrices() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("101"),
        new BigDecimal("104"));

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }
}
