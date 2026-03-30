package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Exchange;
import model.Stock;

/**
 * Supplies UI-ready data for {@link view.RegularSavingsPanel}: stocks listed on an
 * {@link Exchange}, sorted by symbol, for combo-box selection when creating a plan.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
public class RegularSavingsPanelController {

  private final Exchange exchange;
  private final ObservableList<Stock> listedStocks = FXCollections.observableArrayList();

  /**
   * Creates a controller for the given exchange and loads the initial stock list.
   *
   * @param exchange exchange whose listings populate the observable list
   */
  public RegularSavingsPanelController(Exchange exchange) {
    this.exchange = exchange;
    refreshListedStocks();
  }

  /**
   * Returns the mutable observable list backing stock pickers; sorted by symbol after each
   * {@link #refreshListedStocks()}.
   *
   * @return stocks on this exchange, for {@link javafx.scene.control.ComboBox} items
   */
  public ObservableList<Stock> getListedStocks() {
    return listedStocks;
  }

  /**
   * Rebuilds the listed stocks from the exchange (same order: symbol ascending). Call if listings
   * can change at runtime.
   */
  public void refreshListedStocks() {
    List<Stock> sorted = new ArrayList<>(exchange.findStocks(""));
    sorted.sort(Comparator.comparing(Stock::getSymbol));
    listedStocks.setAll(sorted);
  }
}
