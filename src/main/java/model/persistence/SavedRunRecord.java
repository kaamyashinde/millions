package model.persistence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

/**
 * JSON-serialized snapshot of one saved playthrough for comparison and optional leaderboard intent.
 *
 * @param schemaVersion            format version for forward compatibility
 * @param runId                    unique id (UUID string)
 * @param savedAt                  ISO-8601 instant string
 * @param label                    optional user label
 * @param tradingDays              in-game duration (exchange day count at save)
 * @param cash                     liquid balance at save
 * @param holdings                 denormalized lots with mark-to-market values
 * @param stats                    net worth, level, and performance metrics at save
 * @param eligibleForLeaderboard   user-selected flag for a future leaderboard submission flow
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SavedRunRecord(
    int schemaVersion,
    String runId,
    String savedAt,
    String label,
    int tradingDays,
    BigDecimal cash,
    List<SavedRunHoldingSnapshot> holdings,
    SavedRunStatsSnapshot stats,
    boolean eligibleForLeaderboard) {

  /** Current on-disk format version. */
  public static final int CURRENT_SCHEMA_VERSION = 1;

  /**
   * One portfolio lot at save time.
   *
   * @param symbol                trading symbol
   * @param assetName             display name
   * @param quantity              shares held in this lot
   * @param purchasePricePerUnit  cost basis per unit for this lot
   * @param marketValueNet        mark-to-market net liquidation value for this lot
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SavedRunHoldingSnapshot(
      String symbol,
      String assetName,
      BigDecimal quantity,
      BigDecimal purchasePricePerUnit,
      BigDecimal marketValueNet) {
  }

  /**
   * Performance and summary stats at save time.
   *
   * @param netWorth                  total net worth (cash + holdings)
   * @param startingMoney             profile starting balance
   * @param playerLevel               resolved {@link model.PlayerLevel} name
   * @param portfolioReturnPercent    total return % when available
   * @param portfolioVolatility       volatility metric when available
   * @param portfolioSharpeRatio      Sharpe ratio when available
   * @param benchmarkReturnPercent    benchmark return % when available
   * @param benchmarkVolatility       benchmark volatility when available
   * @param benchmarkSharpeRatio      benchmark Sharpe when available
   */
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record SavedRunStatsSnapshot(
      BigDecimal netWorth,
      BigDecimal startingMoney,
      String playerLevel,
      BigDecimal portfolioReturnPercent,
      BigDecimal portfolioVolatility,
      BigDecimal portfolioSharpeRatio,
      BigDecimal benchmarkReturnPercent,
      BigDecimal benchmarkVolatility,
      BigDecimal benchmarkSharpeRatio) {
  }
}
