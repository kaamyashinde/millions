package model.core.player;


/**
 * Observer that recalculates the player's level whenever the player's state changes.
 * Registered automatically inside the {@link Player} constructor so the level
 * stays current after every money or portfolio mutation.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public class PlayerLevelObserver implements PlayerObserver {

  /**
   * Triggers a level recalculation on the given player.
   *
   * @param player the player whose level should be re-evaluated
   */
  @Override
  public void onPlayerStateChanged(Player player) {
    player.recalculateLevel();
  }
}
