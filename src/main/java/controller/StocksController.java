package controller;

import static util.Validator.checkNotNull;

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
 *
 * <p>The controller adapts {@link Exchange#findStocks(String)} into observable rows for
 * {@link view.pages.stocks.StocksPage} and exposes market-event history for selected stocks.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-01
 */
public class StocksController {

  private final Exchange exchange;
  private final ObservableList<Stock> stocks = FXCollections.observableArrayList();
  private final ObjectProperty<Stock> selectedStock = new SimpleObjectProperty<>();

  /**
   * Creates a stock-list controller and loads the initial rows.
   *
   * @param exchange exchange whose stocks are listed
   */
  public StocksController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
    refresh();
  }

  /**
   * Exposes the exchange backing stock rows.
   *
   * @return exchange backing the stock listings
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Exposes stock rows for list bindings.
   *
   * @return observable stock rows sorted by symbol
   */
  public ObservableList<Stock> getStocks() {
    return stocks;
  }

  /**
   * Exposes the selected stock property.
   *
   * @return selected stock property for view bindings
   */
  public ObjectProperty<Stock> selectedStockProperty() {
    return selectedStock;
  }

  /**
   * Returns the selected stock.
   *
   * @return currently selected stock, or {@code null} when no stock is available
   */
  public Stock getSelectedStock() {
    return selectedStock.get();
  }

  /**
   * Updates the selected stock.
   *
   * @param stock stock to select, or {@code null} to clear selection
   */
  public void setSelectedStock(Stock stock) {
    selectedStock.set(stock);
  }

  /**
   * Formats the stocks-page metadata line.
   *
   * @return compact exchange, day, and listing-count text for the stocks page
   */
  public String getMetaText() {
    return exchange.getName()
        + " · trading day "
        + exchange.getDay()
        + " · "
        + exchange.findStocks("").size()
        + " listing(s)";
  }

  /**
   * Returns historical market events affecting a stock.
   *
   * @param stock selected stock, or {@code null}
   * @return matching market events, or an empty list when no stock is selected
   */
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
