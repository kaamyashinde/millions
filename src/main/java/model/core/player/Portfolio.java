package model.core.player;


import model.core.asset.Share;
import model.trading.transaction.Sale;

import static util.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import model.trading.calculator.SaleCalculator;
import model.trading.transaction.TransactionSizing;

/**
 * Represents a portfolio of stocks and shares.This would belong to a user.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-01-31
 */
public class Portfolio {

  private final List<Share> shares;

  /**
   * This is the constructor for the Portfolio class. It does not take any parameters, as the
   * necessary information is filled in later.
   */
  public Portfolio() {
    this.shares = new java.util.ArrayList<>();
  }

  /**
   * Adds a share to the portfolio.
   *
   * @param share The share to be added.
   * @return True if the share was added successfully, false otherwise.
   * @throws NullPointerException if {@code share} is null.
   */
  public boolean addShare(Share share) {
    checkNotNullOnShare(share);
    return this.shares.add(share);
  }


  /**
   * Checks if the share is not null.
   *
   * @param share The share to be checked.
   */
  private static void checkNotNullOnShare(Share share) {
    checkNotNull(share, "Share");
  }

  /**
   * Removes a share from the portfolio.
   *
   * @param share The share to be removed.
   * @return True if the share was removed successfully, false otherwise.
   * @throws NullPointerException if {@code share} is null.
   */
  public boolean removeShare(Share share) {
    checkNotNullOnShare(share);
    return this.shares.remove(share);
  }

  /**
   * Gets the list of shares in the portfolio.
   *
   * @return An unmodifiable live view of the shares in the portfolio.
   */
  public List<Share> getShares() {
    return Collections.unmodifiableList(this.shares);
  }

  /**
   * Gets the shares based on the stock symbol.
   *
   * @param symbol The stock symbol.
   * @return The list of shares with the given stock symbol.
   * @throws NullPointerException if {@code symbol} is null.
   */
  public List<Share> getSharesBasedOnSymbol(String symbol) {
    checkNotNull(symbol, "Symbol");
    return shares.stream().filter(share -> share.getAsset().getSymbol().equals(symbol)).toList();
  }

  /**
   * Checks if the portfolio contains a specific share.
   *
   * @param share The share to check.
   * @return True if the share is in the portfolio, false otherwise.
   * @throws NullPointerException if {@code share} is null.
   */
  public boolean containsShare(Share share) {
    checkNotNullOnShare(share);
    return this.shares.contains(share);
  }

  /**
   * Total quantity held for the symbol across all lots (FIFO order is list order).
   *
   * @param symbol asset symbol to total
   * @return total quantity held for the symbol
   */
  public BigDecimal totalQuantityForSymbol(String symbol) {
    checkNotNull(symbol, "Symbol");
    return shares.stream()
        .filter(s -> s.getAsset().getSymbol().equals(symbol))
        .map(Share::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Describes the next FIFO slice for a sale (does not mutate the portfolio). The caller passes
   * this slice to {@link model.trading.transaction.Sale#commit(Player)}, which removes it via
   * {@link #removeFifoSliceForSale(Share)}.
   *
   * @param symbol asset symbol to sell
   * @param maxQuantity maximum quantity to include in the slice
   * @return sale slice from the first matching FIFO lot, or {@code null}
   */
  public Share buildNextFifoSaleSlice(String symbol, BigDecimal maxQuantity) {
    checkNotNull(symbol, "Symbol");
    checkNotNull(maxQuantity, "maxQuantity");
    for (Share lot : shares) {
      if (!lot.getAsset().getSymbol().equals(symbol)) {
        continue;
      }
      BigDecimal take = lot.getQuantity().min(maxQuantity);
      if (take.signum() <= 0) {
        continue;
      }
      return new Share(lot.getAsset(), take, lot.getPurchasePrice());
    }
    return null;
  }

  /**
   * Same as {@link #buildNextFifoSaleSlice(String, BigDecimal)} but quantity is sized from the
   * first matching lot so net proceeds do not exceed {@code targetNet}.
   *
   * @param symbol asset symbol to sell
   * @param targetNet maximum net proceeds to raise
   * @return sale slice from the first matching FIFO lot, or {@code null}
   */
  public Share buildNextFifoSliceForTargetNet(String symbol, BigDecimal targetNet) {
    checkNotNull(symbol, "Symbol");
    checkNotNull(targetNet, "targetNet");
    for (Share lot : shares) {
      if (!lot.getAsset().getSymbol().equals(symbol)) {
        continue;
      }
      BigDecimal q = TransactionSizing.maxQuantityForTargetNet(lot, targetNet);
      if (q.signum() <= 0) {
        continue;
      }
      return new Share(lot.getAsset(), q, lot.getPurchasePrice());
    }
    return null;
  }

  /**
   * Removes {@code slice} from the first FIFO lot with matching symbol, purchase price, and at
   * least {@code slice.getQuantity()} shares, splitting the lot if needed.
   *
   * @param slice sale slice to remove from FIFO holdings
   * @return true if a matching lot was updated or removed
   */
  public boolean removeFifoSliceForSale(Share slice) {
    checkNotNullOnShare(slice);
    for (int i = 0; i < shares.size(); i++) {
      Share lot = shares.get(i);
      if (!lot.getAsset().getSymbol().equals(slice.getAsset().getSymbol())) {
        continue;
      }
      if (lot.getPurchasePrice().compareTo(slice.getPurchasePrice()) != 0) {
        continue;
      }
      if (lot.getQuantity().compareTo(slice.getQuantity()) < 0) {
        continue;
      }
      if (lot.getQuantity().compareTo(slice.getQuantity()) == 0) {
        shares.remove(i);
        return true;
      }
      BigDecimal remainderQty = lot.getQuantity().subtract(slice.getQuantity());
      shares.set(i, new Share(lot.getAsset(), remainderQty, lot.getPurchasePrice()));
      return true;
    }
    return false;
  }

  /**
   * Calculates the net worth of the portfolio by summing up the value of all shares.
   *
   * @return The net worth of the portfolio.
   */
  public BigDecimal getNetWorth() {
    return shares.stream()
        .map(this::calculateShareValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Calculates the value of a share using the SaleCalculator.
   *
   * @param share The share for which the value is to be calculated.
   * @return The calculated value of the share.
   */
  private BigDecimal calculateShareValue(Share share) {
    return new SaleCalculator(share).calculateTotal();
  }
}
