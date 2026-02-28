package model.persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Stock;

public class CsvReader {

  public static List<Stock> readCsv(String fileName) {
    List<Stock> stocks = new ArrayList<>();

    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");

        if (tokens.length == 3) {
          stocks.add(setStock(tokens));
        }
      }

    } catch (IOException exception) {
      exception.printStackTrace();
    }
    return stocks;
    
  }

  /**
   * Sets a valid Stock object.
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
