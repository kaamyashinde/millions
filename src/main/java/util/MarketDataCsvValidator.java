package util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.core.asset.Stock;
import model.core.asset.fund.Fund;

/**
 * Validates rows in mixed market-data CSV files before they are parsed into domain objects.
 */
public final class MarketDataCsvValidator {

  /** CSV record type for stock rows. */
  public static final String STOCK_RECORD = "STOCK";

  /** CSV record type for fund rows. */
  public static final String FUND_RECORD = "FUND";

  private MarketDataCsvValidator() {
  }

  /**
   * Splits one CSV row into trimmed tokens and validates the record type.
   *
   * @param line raw CSV row
   * @return trimmed tokens for a supported record type
   * @throws IllegalArgumentException if the row is empty or uses an unknown record type
   */
  public static String[] splitValidatedRow(String line) {
    String[] rawTokens = line.split(",");
    if (rawTokens.length == 0) {
      throw new IllegalArgumentException("Invalid market-data row: " + line);
    }
    String[] tokens = Arrays.stream(rawTokens).map(String::trim).toArray(String[]::new);
    if (!STOCK_RECORD.equals(tokens[0]) && !FUND_RECORD.equals(tokens[0])) {
      throw new IllegalArgumentException("Unknown market-data record type: " + line);
    }
    return tokens;
  }

  /**
   * Validates that a stock row has the expected number of columns.
   *
   * @param tokens parsed row tokens
   * @param line original row text for error messages
   * @throws IllegalArgumentException if the row does not contain exactly four columns
   */
  public static void requireStockColumns(String[] tokens, String line) {
    if (tokens.length != 4) {
      throw new IllegalArgumentException("Invalid stock row: " + line);
    }
  }

  /**
   * Validates that a fund row has the minimum number of columns.
   *
   * @param tokens parsed row tokens
   * @throws IllegalArgumentException if the row has fewer than four columns
   */
  public static void requireFundColumns(String[] tokens) {
    if (tokens.length < 4) {
      throw new IllegalArgumentException("Invalid fund row: " + String.join(",", tokens));
    }
  }

  /**
   * Validates one fund component token and returns its symbol and weight parts.
   *
   * @param componentToken component in {@code SYMBOL:weight} form
   * @return trimmed symbol and weight tokens
   * @throws IllegalArgumentException if the token is not in {@code SYMBOL:weight} form
   */
  public static String[] requireFundComponentParts(String componentToken) {
    String[] parts = componentToken.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid fund component: " + componentToken);
    }
    return new String[] {parts[0].trim(), parts[1].trim()};
  }

  /**
   * Validates that a fund component references a stock loaded earlier in the file.
   *
   * @param symbol stock symbol from the component token
   * @param stocksBySymbol stocks parsed from the same CSV file
   * @return matching stock
   * @throws IllegalArgumentException if no stock exists for the symbol
   */
  public static Stock requireKnownStock(String symbol, Map<String, Stock> stocksBySymbol) {
    String normalizedSymbol = symbol.toUpperCase();
    Stock stock = stocksBySymbol.get(normalizedSymbol);
    if (stock == null) {
      throw new IllegalArgumentException("Unknown stock symbol in fund component: " + normalizedSymbol);
    }
    return stock;
  }

  /**
   * Validates that a symbol has not already been used in the same asset collection.
   *
   * @param <T> asset value type stored in the map
   * @param assetsBySymbol assets keyed by symbol
   * @param symbol symbol to check
   * @param messagePrefix prefix for duplicate-symbol error messages
   * @throws IllegalArgumentException if the symbol is already present
   */
  public static <T> void requireUniqueSymbol(
      Map<String, T> assetsBySymbol,
      String symbol,
      String messagePrefix) {
    if (assetsBySymbol.containsKey(symbol)) {
      throw new IllegalArgumentException(messagePrefix + symbol);
    }
  }

  /**
   * Validates that fund symbols are unique and do not collide with stock symbols.
   *
   * @param stocksBySymbol stocks parsed from the same CSV file
   * @param funds funds parsed from the same CSV file
   * @throws IllegalArgumentException if a fund symbol duplicates a stock or another fund
   */
  public static void requireUniqueFundSymbols(Map<String, Stock> stocksBySymbol, List<Fund> funds) {
    Map<String, Fund> fundsBySymbol = new LinkedHashMap<>();
    for (Fund fund : funds) {
      if (stocksBySymbol.containsKey(fund.getSymbol())) {
        throw new IllegalArgumentException("Duplicate asset symbol: " + fund.getSymbol());
      }
      requireUniqueSymbol(fundsBySymbol, fund.getSymbol(), "Duplicate fund symbol: ");
      fundsBySymbol.put(fund.getSymbol(), fund);
    }
  }
}
