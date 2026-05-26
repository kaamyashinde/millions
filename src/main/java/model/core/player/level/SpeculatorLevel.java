package model.core.player.level;


import java.math.BigDecimal;
import model.core.player.Player;

/**
 * Speculator level: requires 20 distinct trading days and net worth at least 2x starting money;
 * effectively uncapped trade size.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class SpeculatorLevel implements PlayerLevel {

  /** Singleton instance for the speculator state. */
  public static final SpeculatorLevel INSTANCE = new SpeculatorLevel();

  /** Effectively uncapped trade size for top tier. */
  private static final BigDecimal UNCAPPED = BigDecimal.valueOf(Long.MAX_VALUE);

  private SpeculatorLevel() {}

  @Override
  public String name() {
    return "SPECULATOR";
  }

  @Override
  public String displayName() {
    return "Speculator";
  }

  @Override
  public BigDecimal maxTradeSize(Player player) {
    return UNCAPPED;
  }

  @Override
  public boolean qualifies(Player player) {
    return player.getTransactionArchive().countDistinctDay() >= 20
        && player.getNetWorth().compareTo(player.getStartingMoney().multiply(BigDecimal.valueOf(2)))
            >= 0;
  }
}
