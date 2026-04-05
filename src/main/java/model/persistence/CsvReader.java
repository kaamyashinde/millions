package model.persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import model.market.Stock;
import model.market.fund.Fund;
import model.market.fund.FundComponent;

/**
 * Reads mixed market data from CSV where each row is either a stock or a fund definition.
 *
 * <p>Supported row formats:
 * <ul>
 *   <li>{@code STOCK,symbol,company,price}</li>
 *   <li>{@code FUND,symbol,name,STOCK_A:0.40,STOCK_B:0.60}</li>
 * </ul>
 * <p>
 * Empty lines and comment lines beginning with {@code #} are ignored.
 */
public final class CsvReader {

  private static final String STOCK_RECORD = "STOCK";
  private static final String FUND_RECORD = "FUND";

  private CsvReader() {
  }

  /**
   * Reads mixed market data from a CSV file on disk.
   *
   * @param directory directory containing the CSV file
   * @param fileName  file name without the {@code .csv} extension
   * @return parsed stocks and funds
   */
  public static MarketData readMarketData(Path directory, String fileName) {
    Path path = directory.resolve(fileName + ".csv");
    try (Stream<String> lines = Files.lines(path)) {
      return marketDataFromLineStream(lines);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  /**
   * Reads mixed market data from an input stream such as a bundled classpath resource.
   *
   * @param input UTF-8 market-data text stream
   * @return parsed stocks and funds
   */
  public static MarketData readMarketData(InputStream input) {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
    try (Stream<String> lines = reader.lines()) {
      return marketDataFromLineStream(lines);
    }
  }

  /**
   * Reads only stocks from a mixed market-data CSV.
   *
   * @param directory directory containing the CSV file
   * @param fileName  file name without the {@code .csv} extension
   * @return loaded stocks
   */
  public static List<Stock> readCsv(Path directory, String fileName) {
    return readMarketData(directory, fileName).stocks();
  }

  /**
   * Reads only stocks from a mixed market-data input stream.
   *
   * @param input UTF-8 market-data text stream
   * @return loaded stocks
   */
  public static List<Stock> readCsv(InputStream input) {
    return readMarketData(input).stocks();
  }

  private static MarketData marketDataFromLineStream(Stream<String> lines) {
    List<String> cleanedLines = lines
        .map(String::trim)
        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
        .toList();
    Map<String, Stock> stocksBySymbol = parseStocks(cleanedLines);
    List<Fund> funds = parseFunds(cleanedLines, stocksBySymbol);
    ensureUniqueFundSymbols(stocksBySymbol, funds);
    return new MarketData(List.copyOf(stocksBySymbol.values()), funds);
  }

  private static Map<String, Stock> parseStocks(List<String> cleanedLines) {
    Map<String, Stock> stocksBySymbol = new LinkedHashMap<>();
    for (String line : cleanedLines) {
      String[] tokens = splitLine(line);
      if (!STOCK_RECORD.equals(tokens[0])) {
        continue;
      }
      if (tokens.length != 4) {
        throw new IllegalArgumentException("Invalid stock row: " + line);
      }
      Stock stock = parseStock(tokens);
      ensureUniqueSymbol(stocksBySymbol, stock.getSymbol(), "Duplicate stock symbol: ");
      stocksBySymbol.put(stock.getSymbol(), stock);
    }
    return stocksBySymbol;
  }

  private static List<Fund> parseFunds(List<String> cleanedLines,
      Map<String, Stock> stocksBySymbol) {
    return cleanedLines.stream()
        .map(CsvReader::splitLine)
        .filter(tokens -> FUND_RECORD.equals(tokens[0]))
        .map(tokens -> parseFund(tokens, stocksBySymbol))
        .toList();
  }

  private static String[] splitLine(String line) {
    String[] rawTokens = line.split(",");
    if (rawTokens.length == 0) {
      throw new IllegalArgumentException("Invalid market-data row: " + line);
    }
    String[] tokens = new String[rawTokens.length];
    for (int index = 0; index < rawTokens.length; index++) {
      tokens[index] = rawTokens[index].trim();
    }
    if (!STOCK_RECORD.equals(tokens[0]) && !FUND_RECORD.equals(tokens[0])) {
      throw new IllegalArgumentException("Unknown market-data record type: " + line);
    }
    return tokens;
  }

  private static Stock parseStock(String[] tokens) {
    BigDecimal price = new BigDecimal(tokens[3]);
    Stock stock = new Stock(tokens[1], tokens[2]);
    stock.addNewSalesPrice(price);
    return stock;
  }

  private static Fund parseFund(String[] tokens, Map<String, Stock> stocksBySymbol) {
    if (tokens.length < 4) {
      throw new IllegalArgumentException("Invalid fund row: " + String.join(",", tokens));
    }
    List<FundComponent> components = Stream.of(tokens)
        .skip(3)
        .map(componentToken -> parseFundComponent(componentToken, stocksBySymbol))
        .toList();
    return new Fund(tokens[1], tokens[2], components);
  }

  private static FundComponent parseFundComponent(
      String componentToken,
      Map<String, Stock> stocksBySymbol) {
    String[] parts = componentToken.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid fund component: " + componentToken);
    }
    String symbol = parts[0].trim().toUpperCase();
    Stock stock = stocksBySymbol.get(symbol);
    if (stock == null) {
      throw new IllegalArgumentException("Unknown stock symbol in fund component: " + symbol);
    }
    BigDecimal weight = new BigDecimal(parts[1].trim());
    return new FundComponent(stock, weight);
  }

  private static void ensureUniqueFundSymbols(Map<String, Stock> stocksBySymbol, List<Fund> funds) {
    Map<String, Fund> fundsBySymbol = new LinkedHashMap<>();
    for (Fund fund : funds) {
      if (stocksBySymbol.containsKey(fund.getSymbol())) {
        throw new IllegalArgumentException("Duplicate asset symbol: " + fund.getSymbol());
      }
      ensureUniqueSymbol(fundsBySymbol, fund.getSymbol(), "Duplicate fund symbol: ");
      fundsBySymbol.put(fund.getSymbol(), fund);
    }
  }

  private static <T> void ensureUniqueSymbol(
      Map<String, T> assetsBySymbol,
      String symbol,
      String messagePrefix) {
    if (assetsBySymbol.containsKey(symbol)) {
      throw new IllegalArgumentException(messagePrefix + symbol);
    }
  }
}
