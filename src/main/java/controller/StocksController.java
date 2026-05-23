package controller;

import static model.utils.Validator.checkNotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.core.market.Exchange;
import model.core.asset.Stock;
import model.core.market.event.MarketEvent;

/**
 * Supplies sorted stock listings and selection state for the stocks tab.
 */
public class StocksController {

  private final Exchange exchange;
  private final ObservableList<Stock> stocks = FXCollections.observableArrayList();
  private final ObjectProperty<Stock> selectedStock = new SimpleObjectProperty<>();

  /**
   * @param exchange exchange whose stocks are listed
   */
  public StocksController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
    refresh();
  }

  public Exchange getExchange() {
    return exchange;
  }

  public ObservableList<Stock> getStocks() {
    return stocks;
  }

  public ObjectProperty<Stock> selectedStockProperty() {
    return selectedStock;
  }

  public Stock getSelectedStock() {
    return selectedStock.get();
  }

  public void setSelectedStock(Stock stock) {
    selectedStock.set(stock);
  }

  public String getMetaText() {
    return exchange.getName()
        + " · trading day "
        + exchange.getDay()
        + " · "
        + exchange.findStocks("").size()
        + " listing(s)";
  }

  public List<MarketEvent> getMarketHistoryFor(Stock stock) {
    if (stock == null) {
      return List.of();
    }
    return exchange.getMarketEventsForStock(stock.getSymbol());
  }

  /**
   * Reloads stock rows from the exchange, preserving selection when possible.
   */
  public void refresh() {
    Stock previous = selectedStock.get();
    List<Stock> sorted = new ArrayList<>(exchange.findStocks(""));
    sorted.sort(Comparator.comparing(Stock::getSymbol));
    stocks.setAll(sorted);
    if (previous != null) {
      for (Stock stock : sorted) {
        if (stock.getSymbol().equals(previous.getSymbol())) {
          selectedStock.set(stock);
          return;
        }
      }
    }
    if (selectedStock.get() == null && !sorted.isEmpty()) {
      selectedStock.set(sorted.get(0));
    }
  }
}
