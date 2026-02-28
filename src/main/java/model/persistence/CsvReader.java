package model.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import model.Stock;

/**
 * A utility class for reading stock data from a CSV file and converting it into a list of Stock
 * objects. The CSV file is expected to have the following format: symbol,company,price For example:
 * NVDA,Nvidia,191.27 We skip empty lines and lines starting with '#' (considered as comments).
 *
 * @author kevindmazali
 * @version 0.0.1
 * @see Stock
 * @since 2026-02-28
 */

public class CsvReader {

  private static final String FILE_NAME = "src/main/resources/";

  /**
   * Reads stock data from a CSV file and returns a list of Stock objects. Each line in the CSV file
   * should contain the stock symbol, company name, and price, separated by commas.
   *
   * @param fileName the name of the CSV file to read from, which will be saved in the resources
   *                 folder
   * @return a list of Stock objects created from the CSV data
   */

  public static List<Stock> readCsv(String fileName) {
    Path path = Path.of(FILE_NAME + fileName + ".csv");

    try (Stream<String> lines = Files.lines(path)) {
      return lines
          .map(String::trim)
          .filter(line -> !line.isEmpty() && !line.startsWith("#"))
          .map(line -> line.split(","))
          .filter(tokens -> tokens.length == 3)
          .map(CsvReader::setStock)
          .toList();

    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Sets a Stock object based on the provided tokens from a CSV line. The tokens are expected to be
   * in the order: symbol, company, price. The price is converted from a String to a BigDecimal
   * before being added to the Stock object.
   *
   * @param tokens an array of Strings containing the stock symbol, company name, and price in that
   *               order
   * @return a Stock object created from the provided tokens
   */
  private static Stock setStock(String[] tokens) {
    String symbol = tokens[0];
    String company = tokens[1];
    BigDecimal price = new BigDecimal(tokens[2]);

    Stock stock = new Stock(symbol, company);
    stock.addNewSalesPrice(price);
    return stock;
  }

}
