package model;

import java.math.BigDecimal;

/**
 * Investor level: requires 70 distinct trading days and net worth at least 1.2x starting money;
 * trade size capped at 50x starting money.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class InvestorLevel implements PlayerLevel {

  /** Singleton instance for the investor state. */
  public static final InvestorLevel INSTANCE = new InvestorLevel();

  private InvestorLevel() {}

  @Override
  public String name() {
    return "INVESTOR";
  }

  @Override
  public String displayName() {
    return "Investor";
  }

  @Override
  public BigDecimal maxTradeSize(Player player) {
    return player.getStartingMoney().multiply(BigDecimal.valueOf(50));
  }

  @Override
  public boolean qualifies(Player player) {
    return player.getTransactionArchive().countDistinctDay() >= 70
        && player
            .getNetWorth()
            .compareTo(player.getStartingMoney().multiply(BigDecimal.valueOf(1.20)))
            >= 0;
  }
}
