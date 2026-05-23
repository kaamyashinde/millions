package model.core.player;


/**
 * Observer interface for receiving notifications when a {@link Player}'s state changes.
 * Implementations are notified after mutations such as money deposits, withdrawals,
 * or portfolio modifications.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-04-04
 */
public interface PlayerObserver {

  /**
   * Called after the observed player's state has changed.
   *
   * @param player the player whose state changed
   */
  void onPlayerStateChanged(Player player);
}
