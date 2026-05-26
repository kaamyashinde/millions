package model.core.market;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import model.core.asset.InvestableAsset;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;

/**
 * Stores and queries the assets listed on an exchange.
 */
public final class ExchangeListings {

  private final Map<String, Stock> stockMap;
  private final Map<String, Fund> fundMap;
  private final Map<String, InvestableAsset> assetMap;

  /**
   * Creates an immutable symbol registry from the listed stocks and funds.
   *
   * @param stocks listed stocks
   * @param funds listed funds
   */
  ExchangeListings(List<Stock> stocks, List<Fund> funds) {
    this.stockMap = buildStockMap(stocks);
    this.fundMap = buildFundMap(funds);
    this.assetMap = buildAssetMap(stockMap, fundMap);
  }

  /**
   * Checks whether a stock symbol is listed.
   *
   * @param symbol stock symbol
   * @return {@code true} when the stock exists
   */
  public boolean hasStock(String symbol) {
    return stockMap.containsKey(normalizeSymbol(symbol));
  }

  /**
   * Checks whether any asset symbol is listed.
   *
   * @param symbol stock or fund symbol
   * @return {@code true} when the asset exists
   */
  public boolean hasAsset(String symbol) {
    return assetMap.containsKey(normalizeSymbol(symbol));
  }

  /**
   * Finds stocks by symbol or company name.
   *
   * @param searchTerm text to match
   * @return matching stocks
   */
  public List<Stock> findStocks(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase(Locale.ROOT);
    return stockMap.values().stream()
        .filter(stock -> stock.getSymbol().toLowerCase(Locale.ROOT).contains(lowerCaseTerm)
            || stock.getCompany().toLowerCase(Locale.ROOT).contains(lowerCaseTerm))
        .toList();
  }

  /**
   * Finds funds by symbol or display name.
   *
   * @param searchTerm text to match
   * @return matching funds
   */
  public List<Fund> findFunds(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase(Locale.ROOT);
    return fundMap.values().stream()
        .filter(fund -> fund.getSymbol().toLowerCase(Locale.ROOT).contains(lowerCaseTerm)
            || fund.getDisplayName().toLowerCase(Locale.ROOT).contains(lowerCaseTerm))
        .toList();
  }

  /**
   * Finds all listed assets by symbol or display name.
   *
   * @param searchTerm text to match
   * @return matching assets
   */
  public List<InvestableAsset> findAssets(String searchTerm) {
    String lowerCaseTerm = searchTerm.toLowerCase(Locale.ROOT);
    return assetMap.values().stream()
        .filter(asset -> asset.getSymbol().toLowerCase(Locale.ROOT).contains(lowerCaseTerm)
            || asset.getDisplayName().toLowerCase(Locale.ROOT).contains(lowerCaseTerm))
        .toList();
  }

  /**
   * Gets a stock by symbol.
   *
   * @param symbol stock symbol
   * @return stock, or {@code null} when missing
   */
  public Stock getStock(String symbol) {
    return stockMap.get(normalizeSymbol(symbol));
  }

  /**
   * Gets a fund by symbol.
   *
   * @param symbol fund symbol
   * @return fund, or {@code null} when missing
   */
  public Fund getFund(String symbol) {
    return fundMap.get(normalizeSymbol(symbol));
  }

  /**
   * Gets any listed asset by symbol.
   *
   * @param symbol stock or fund symbol
   * @return asset, or {@code null} when missing
   */
  public InvestableAsset getAsset(String symbol) {
    return assetMap.get(normalizeSymbol(symbol));
  }

  /**
   * Returns all listed stocks.
   *
   * @return immutable stock list
   */
  public List<Stock> getStocks() {
    return List.copyOf(stockMap.values());
  }

  /**
   * Returns all listed funds.
   *
   * @return immutable fund list
   */
  public List<Fund> getFunds() {
    return List.copyOf(fundMap.values());
  }

  /**
   * Returns all listed assets.
   *
   * @return immutable asset list
   */
  public List<InvestableAsset> getAssets() {
    return List.copyOf(assetMap.values());
  }

  /**
   * Builds the stock lookup while preserving the provided listing order.
   *
   * @param stocks listed stocks
   * @return immutable symbol-to-stock map
   */
  private static Map<String, Stock> buildStockMap(List<Stock> stocks) {
    Map<String, Stock> result = new LinkedHashMap<>();
    stocks.forEach(stock -> result.put(stock.getSymbol(), stock));
    return Collections.unmodifiableMap(result);
  }

  /**
   * Builds the fund lookup while preserving the provided listing order.
   *
   * @param funds listed funds
   * @return immutable symbol-to-fund map
   */
  private static Map<String, Fund> buildFundMap(List<Fund> funds) {
    Map<String, Fund> result = new LinkedHashMap<>();
    funds.forEach(fund -> result.put(fund.getSymbol(), fund));
    return Collections.unmodifiableMap(result);
  }

  /**
   * Combines stocks and funds into one asset lookup.
   *
   * @param stocks stock lookup
   * @param funds fund lookup
   * @return immutable symbol-to-asset map
   */
  private static Map<String, InvestableAsset> buildAssetMap(
      Map<String, Stock> stocks,
      Map<String, Fund> funds) {
    Map<String, InvestableAsset> result = new LinkedHashMap<>();
    result.putAll(stocks);
    result.putAll(funds);
    return Collections.unmodifiableMap(result);
  }

  /**
   * Normalizes lookup symbols to the exchange's canonical uppercase form.
   *
   * @param symbol raw symbol
   * @return uppercase symbol
   */
  private static String normalizeSymbol(String symbol) {
    return symbol.toUpperCase(Locale.ROOT);
  }
}
