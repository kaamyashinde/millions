package model;

public class ShareNotFoundException extends RuntimeException {

  public ShareNotFoundException(Share share, Player player) {
    super("Share of stock" + share.getStock().getSymbol() + " was not found in the "
        + player.getName() + "'s portfolio.");
  }
}
