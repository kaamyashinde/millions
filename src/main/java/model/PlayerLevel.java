package model;

import java.math.BigDecimal;

/**
 * An enum representing the different levels of players in the system.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-02-28
 */
public enum PlayerLevel {
  SPECULATOR {
    @Override
    public boolean qualifies(Player player) {
      return player.getTransactionArchive().countDistinctDay() >= 140 &&
          player.getNetWorth()
              .compareTo(player.getStartingMoney()
                  .multiply(BigDecimal.valueOf(2))) >= 0;
    }
  },

  INVESTOR {
    @Override
    public boolean qualifies(Player player) {
      return player.getTransactionArchive().countDistinctDay() >= 70 &&
          player.getNetWorth()
              .compareTo(player.getStartingMoney()
                  .multiply(BigDecimal.valueOf(1.20))) >= 0;
    }
  },

  NOVICE {
    @Override
    public boolean qualifies(Player player) {
      return true;
    }
  };

  public abstract boolean qualifies(Player player);
}
