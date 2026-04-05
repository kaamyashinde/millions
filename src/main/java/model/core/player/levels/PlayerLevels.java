package model.core.player.levels;

/**
 * Canonical singleton references for each {@link PlayerLevel} state. Kept in a separate type so the
 * {@link PlayerLevel} interface does not initialize {@code NoviceLevel.INSTANCE} during its own
 * class initialization (which would leave these references null due to circular loading).
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public final class PlayerLevels {

  private PlayerLevels() {
  }

  /**
   * Lowest tier; always available as fallback.
   */
  public static final PlayerLevel NOVICE = NoviceLevel.INSTANCE;
  /**
   * Mid tier: sustained activity and solid returns.
   */
  public static final PlayerLevel INVESTOR = InvestorLevel.INSTANCE;
  /**
   * Top tier: long track record and strong performance.
   */
  public static final PlayerLevel SPECULATOR = SpeculatorLevel.INSTANCE;
}
