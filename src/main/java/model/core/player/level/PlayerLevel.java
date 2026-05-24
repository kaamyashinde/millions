package model.core.player.level;


import model.core.player.Player;

import java.math.BigDecimal;

/**
 * Sealed state hierarchy for player progression. Each level defines qualification rules,
 * display metadata, trade limits, and delegates transitions via {@link #checkTransition(Player)}.
 * Use {@link PlayerLevels} for stable singleton references ({@code NOVICE}, {@code INVESTOR},
 * {@code SPECULATOR}) without circular class-init issues.
 *
 * @author kaamyashinde
 * @version 2.0.0
 * @since 2026-02-28
 */
public sealed interface PlayerLevel permits NoviceLevel, InvestorLevel, SpeculatorLevel {

  /**
   * Stable identifier for persistence (e.g. saved runs), matching historical enum names.
   *
   * @return uppercase level id such as {@code NOVICE}
   */
  String name();

  /**
   * Human-readable label for UI.
   *
   * @return short display name
   */
  String displayName();

  /**
   * Maximum notional size for a single trade at this level, relative to the player's profile.
   *
   * @param player the player whose limits apply
   * @return positive cap; higher tiers may return a very large value
   */
  BigDecimal maxTradeSize(Player player);

  /**
   * Whether this level's requirements are met for the given player snapshot.
   *
   * @param player player to evaluate
   * @return true if this tier's bar is cleared
   */
  boolean qualifies(Player player);

  /**
   * Resolves the highest tier the player qualifies for (State transition).
   *
   * @param player current player state
   * @return the new {@link PlayerLevel} instance (singleton per tier)
   */
  default PlayerLevel checkTransition(Player player) {
    if (SpeculatorLevel.INSTANCE.qualifies(player)) {
      return SpeculatorLevel.INSTANCE;
    }
    if (InvestorLevel.INSTANCE.qualifies(player)) {
      return InvestorLevel.INSTANCE;
    }
    return NoviceLevel.INSTANCE;
  }
}
