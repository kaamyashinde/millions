package model.exception.trading;


import model.core.asset.Share;
import model.core.asset.Stock;
import model.core.player.Player;

/**
 * An exception thrown when a player attempts to sell a share that is not present in their
 * portfolio.
 *
 * @author kevindmazali
 * @version 0.0.1
 * @since 2026-02-07
 */

public class ShareNotFoundException extends RuntimeException {

  private final transient Share share;
  private final transient Player player;

  /**
   * Constructs a new ShareNotFoundException with a message indicating the share and player
   * involved.
   *
   * @param share  the share that was not found in the player's portfolio
   * @param player the player who attempted to sell the share
   */
  public ShareNotFoundException(Share share, Player player) {
    super("Share of asset " + share.getAsset().getSymbol() + " was not found in the "
        + player.getName() + "'s portfolio.");
    this.share = share;
    this.player = player;
  }

  /**
   * Stock symbol for the share that was not found.
   *
   * @return the symbol
   */
  public String getStockSymbol() {
    return share.getAsset().getSymbol();
  }

  /**
   * Name of the player whose portfolio was checked.
   *
   * @return the player name
   */
  public String getPlayerName() {
    return player.getName();
  }
}
