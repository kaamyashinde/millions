package model.analysis.recommendation.strategy;


import model.analysis.recommendation.StockRecommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeanReversionStrategyTest {

  private MeanReversionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new MeanReversionStrategy();
  }

  @Test
  void recommend_returnsBuyWhenLatestPriceIsFarBelowRecentAverage() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("100"),
        new BigDecimal("100"),
        new BigDecimal("97"));

    assertEquals(StockRecommendation.BUY, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsSellWhenLatestPriceIsFarAboveRecentAverage() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("100"),
        new BigDecimal("100"),
        new BigDecimal("103"));

    assertEquals(StockRecommendation.SELL, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldWhenPriceIsCloseToAverage() {
    List<BigDecimal> prices = List.of(
        new BigDecimal("100"),
        new BigDecimal("100.4"),
        new BigDecimal("100.2"),
        new BigDecimal("100.3"));

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }

  @Test
  void recommend_returnsHoldWhenHistoryIsTooShort() {
    List<BigDecimal> prices = List.of(new BigDecimal("100"));

    assertEquals(StockRecommendation.HOLD, strategy.recommend(prices));
  }
}
