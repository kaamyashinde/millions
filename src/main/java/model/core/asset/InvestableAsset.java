package model.core.asset;


import java.math.BigDecimal;

/**
 * Common contract for anything that can be bought, held, and sold through the exchange.
 */
public interface InvestableAsset {

  /**
   * Returns the unique trading symbol used to identify this asset on the exchange.
   *
   * @return uppercase trading symbol
   */
  String getSymbol();

  /**
   * Returns the user-facing name of the asset.
   *
   * @return display name for portfolio and listing views
   */
  String getDisplayName();

  /**
   * Returns the latest tradable price for one unit of the asset.
   *
   * @return current sales price
   */
  BigDecimal getSalesPrice();

  /**
   * Returns the price for one unit of the asset on a historical trading day.
   *
   * @param day trading day number, 1-based
   * @return price on the requested trading day
   */
  BigDecimal getPriceOnDay(int day);

  /**
   * Returns the broad asset type label for UI output.
   *
   * @return human-readable asset type
   */
  String getAssetType();
}
