package model.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import model.Stock;
import model.fund.Fund;
import model.fund.FundComponent;

/**
 * Writes mixed market-data CSV files containing both stocks and funds.
 */
public final class CsvWriter {

  private static final String STOCK_RECORD = "STOCK";
  private static final String FUND_RECORD = "FUND";

  private CsvWriter() {
  }

  /**
   * Writes mixed market data to a CSV file.
   *
   * @param directory directory where the CSV file will be saved
   * @param fileName name of the CSV file to write to without the {@code .csv} extension
   * @param marketData stocks and funds to serialize
   */
  public static void writeMarketDataToFile(Path directory, String fileName, MarketData marketData) {
    Path path = directory.resolve(fileName + ".csv");
    try (BufferedWriter writer = Files.newBufferedWriter(path)) {
      for (Stock stock : marketData.stocks()) {
        writer.write(formatStock(stock));
        writer.newLine();
      }
      for (Fund fund : marketData.funds()) {
        writer.write(formatFund(fund));
        writer.newLine();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Writes a stock-only market-data file in the mixed-format schema.
   *
   * @param directory directory where the CSV file will be saved
   * @param fileName file name without the {@code .csv} extension
   * @param stocksToSave stocks to serialize
   */
  public static void writeCsvToFile(Path directory, String fileName, List<Stock> stocksToSave) {
    writeMarketDataToFile(directory, fileName, new MarketData(stocksToSave, List.of()));
  }

  /**
   * Formats one stock into a mixed market-data CSV row.
   *
   * @param stock stock to serialize
   * @return CSV row for the stock
   */
  private static String formatStock(Stock stock) {
    return String.join(",",
        STOCK_RECORD,
        stock.getSymbol(),
        stock.getCompany(),
        stock.getSalesPrice().toString()
    );
  }

  /**
   * Formats one fund into a mixed market-data CSV row.
   *
   * @param fund fund to serialize
   * @return CSV row for the fund
   */
  private static String formatFund(Fund fund) {
    List<String> parts = new java.util.ArrayList<>();
    parts.add(FUND_RECORD);
    parts.add(fund.getSymbol());
    parts.add(fund.getDisplayName());
    fund.getComponents().stream()
        .sorted(Comparator.comparing(component -> component.stock().getSymbol()))
        .map(CsvWriter::formatFundComponent)
        .forEach(parts::add);
    return String.join(",", parts);
  }

  /**
   * Formats one fund component into {@code SYMBOL:weight} form.
   *
   * @param component fund component to serialize
   * @return component CSV token
   */
  private static String formatFundComponent(FundComponent component) {
    return component.stock().getSymbol() + ":" + component.weight().toPlainString();
  }
}
