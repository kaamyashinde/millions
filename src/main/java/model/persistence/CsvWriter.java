package model.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import model.Stock;

/**
 * A class responsible for writing the Stock Exchange data to a CSV file.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @see Stock
 * @since 2026-02-28
 */
public class CsvWriter {

  /**
   * Write the stock exchange data to a CSV file. The data should be in the format:
   * symbol,company,price. Each line in the CSV file should represent a stock with its symbol,
   * company name, and price.
   *
   * @param directory    the directory where the CSV file will be saved
   * @param fileName     the name of the CSV file to write to (without the .csv extension)
   * @param stocksToSave the list of stocks to write to the CSV file
   */
  public static void writeCsvToFile(Path directory, String fileName, List<Stock> stocksToSave) {
    Path path = directory.resolve(fileName + ".csv");
    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      for (Stock stock : stocksToSave) {
        writer.write(formatStock(stock));
        writer.newLine();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Formats a Stock object into a CSV line format. The format is: symbol,company,price.
   *
   * @param stock the Stock object to be formatted into a CSV line
   * @return a String representing the Stock object in CSV line format
   */
  private static String formatStock(Stock stock) {
    return String.join(",",
        stock.getSymbol(),
        stock.getCompany(),
        stock.getSalesPrice().toString()
    );
  }
}
