package model;

import static model.utils.Validator.checkNotNull;
import java.math.BigDecimal;
import java.util.List;
import model.transactioncalculator.SaleCalculator;

/**
 * Represents a portfolio of stocks and shares.This would belong to a user.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 31-01-2026
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
   * @return The list of shares in the portfolio.
   */
  public List<Share> getShares() {
    return this.shares;
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
    return shares.stream().filter(share -> share.getStock().getSymbol().equals(symbol)).toList();
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
