package controller;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

  private static final int PERCENT_SCALE = 8;

  private final Exchange exchange;
  private final ObservableList<Stock> stocks = FXCollections.observableArrayList();
  private final ObjectProperty<Stock> selectedStock = new SimpleObjectProperty<>();
  private String searchTerm = "";

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

  public String getSearchTerm() {
    return searchTerm;
  }

  /**
   * Updates the active search term and reloads the filtered stock rows.
   *
   * @param searchTerm symbol or company text to match
   */
  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm == null ? "" : searchTerm.trim();
    refresh();
  }

  /**
   * Formats the stocks-page metadata line.
   *
   * @return compact exchange, day, and listing-count text for the stocks page
   */
  public String getMetaText() {
    int total = exchange.listings().findStocks("").size();
    int visible = stocks.size();
    String countText =
        searchTerm.isBlank() ? total + " listing(s)" : visible + " of " + total + " listing(s)";
    return exchange.getName()
        + " · trading day "
        + exchange.getDay()
        + " · "
        + countText;
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
   * Returns the strongest positive movers by latest percentage price change.
   *
   * @param limit maximum number of rows to return
   * @return winners sorted from highest to lowest percent gain
   */
  public List<MarketMover> getTopWinners(int limit) {
    if (limit <= 0) {
      return List.of();
    }
    return marketMovers().stream()
        .filter(mover -> mover.absoluteChange().signum() > 0)
        .sorted(
            Comparator.comparing(MarketMover::percentChange)
                .reversed()
                .thenComparing(MarketMover::symbol))
        .limit(limit)
        .toList();
  }

  /**
   * Returns the strongest negative movers by latest percentage price change.
   *
   * @param limit maximum number of rows to return
   * @return losers sorted from lowest to highest percent loss
   */
  public List<MarketMover> getTopLosers(int limit) {
    if (limit <= 0) {
      return List.of();
    }
    return marketMovers().stream()
        .filter(mover -> mover.absoluteChange().signum() < 0)
        .sorted(
            Comparator.comparing(MarketMover::percentChange)
                .thenComparing(MarketMover::symbol))
        .limit(limit)
        .toList();
  }

  /**
   * Reloads stock rows from the exchange, preserving selection when possible.
   */
  public void refresh() {
    Stock previous = selectedStock.get();
    List<Stock> sorted = new ArrayList<>(exchange.listings().findStocks(searchTerm));
    sorted.sort(Comparator.comparing(Stock::getSymbol));
    stocks.setAll(sorted);
    if (previous != null) {
      Optional<Stock> restored = sorted.stream()
          .filter(stock -> stock.getSymbol().equals(previous.getSymbol()))
          .findFirst();
      if (restored.isPresent()) {
        selectedStock.set(restored.get());
        return;
      }
    }
    selectedStock.set(sorted.isEmpty() ? null : sorted.get(0));
  }

  /**
   * Builds all non-zero latest price movers from the exchange's stock list.
   *
   * @return market movers in exchange iteration order
   */
  private List<MarketMover> marketMovers() {
    return exchange.listings().getStocks().stream()
        .map(this::toMarketMover)
        .flatMap(Optional::stream)
        .toList();
  }

  /**
   * Converts a stock into a market mover when it has enough positive price history.
   *
   * @param stock listed stock to inspect
   * @return mover row, or empty when the stock has no latest movement
   */
  private Optional<MarketMover> toMarketMover(Stock stock) {
    List<BigDecimal> prices = stock.getHistoricalPrices();
    int size = prices.size();
    if (size < 2) {
      return Optional.empty();
    }
    BigDecimal previousPrice = prices.get(size - 2);
    BigDecimal currentPrice = prices.get(size - 1);
    BigDecimal change = currentPrice.subtract(previousPrice);
    if (previousPrice.signum() <= 0 || change.signum() == 0) {
      return Optional.empty();
    }
    BigDecimal percentChange =
        change.divide(previousPrice, PERCENT_SCALE, RoundingMode.HALF_UP);
    return Optional.of(
        new MarketMover(
            stock.getSymbol(),
            stock.getCompany(),
            currentPrice,
            change,
            percentChange));
  }
}
