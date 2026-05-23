package model.core.player.level;


import model.core.player.Player;

import java.math.BigDecimal;

/**
 * Novice player level: always qualifies as a fallback; limits trade size to 10x starting money.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class NoviceLevel implements PlayerLevel {

  /** Singleton instance for the novice state. */
  public static final NoviceLevel INSTANCE = new NoviceLevel();

  private NoviceLevel() {}

  @Override
  public String name() {
    return "NOVICE";
  }

  @Override
  public String displayName() {
    return "Novice";
  }

  @Override
  public BigDecimal maxTradeSize(Player player) {
    return player.getStartingMoney().multiply(BigDecimal.valueOf(10));
  }

  @Override
  public boolean qualifies(Player player) {
    return true;
  }
}
