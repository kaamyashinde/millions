package model.core.market;

import java.math.BigDecimal;
import java.util.List;

/**
 * Common contract for anything that can be bought, held, and sold through the exchange. This
 * includes stocks, funds, and potentially other asset types in the future.
 *
 * @author kevindmazali
 * @version 1.0.1
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
   * Returns the broad asset type label for UI output.
   *
   * @return human-readable asset type
   */
  String getAssetType();

  /**
   * Returns the historical prices for this asset, ordered from oldest to newest.
   */
  List<BigDecimal> getHistoricalPrices();
}
