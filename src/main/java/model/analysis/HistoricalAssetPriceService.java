package model.analysis;

import static model.utils.Validator.checkNotNull;

import java.math.BigDecimal;
import java.util.List;
import model.InvestableAsset;
import model.Stock;
import model.fund.Fund;
import model.fund.FundComponent;

/**
 * Resolves historical prices for stocks and derived fund prices on a trading day.
 */
public class HistoricalAssetPriceService {

  /**
   * Returns the asset price for the requested trading day.
   *
   * @param asset stock or fund whose price should be resolved
   * @param day trading day number (1-based)
   * @return price on that trading day
   */
  public BigDecimal getPriceOnDay(InvestableAsset asset, int day) {
    checkNotNull(asset, "Asset");
    validateDay(day);
    if (asset instanceof Stock stock) {
      return getStockPriceOnDay(stock, day);
    }
    if (asset instanceof Fund fund) {
      return getFundPriceOnDay(fund, day);
    }
    throw new IllegalArgumentException("Unsupported asset type: " + asset.getClass().getSimpleName());
  }

  /**
   * Returns a stock price from its historical price list.
   *
   * @param stock stock to inspect
   * @param day trading day number (1-based)
   * @return stock price on that day
   */
  public BigDecimal getStockPriceOnDay(Stock stock, int day) {
    checkNotNull(stock, "Stock");
    validateDay(day);
    List<BigDecimal> prices = stock.getHistoricalPrices();
    if (prices.size() < day) {
      throw new IllegalArgumentException("Missing stock price for day " + day);
    }
    return prices.get(day - 1);
  }

  /**
   * Derives a fund price for a trading day from the weighted historical stock prices of its
   * components.
   *
   * @param fund fund to inspect
   * @param day trading day number (1-based)
   * @return derived fund price on that day
   */
  public BigDecimal getFundPriceOnDay(Fund fund, int day) {
    checkNotNull(fund, "Fund");
    validateDay(day);
    return fund.getComponents().stream()
        .map(component -> getComponentContribution(component, day))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Returns one weighted historical contribution inside a fund.
   *
   * @param component weighted fund component
   * @param day trading day number (1-based)
   * @return weighted price contribution on that day
   */
  public BigDecimal getComponentContribution(FundComponent component, int day) {
    checkNotNull(component, "Fund component");
    validateDay(day);
    return getStockPriceOnDay(component.stock(), day).multiply(component.weight());
  }

  private static void validateDay(int day) {
    if (day < 1) {
      throw new IllegalArgumentException("Trading day must be at least 1.");
    }
  }
}
