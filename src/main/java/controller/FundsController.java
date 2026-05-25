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
import model.core.asset.fund.Fund;

/**
 * Supplies sorted fund listings and selection state for the funds tab.
 *
 * <p>The controller adapts {@link Exchange#findFunds(String)} into an {@link ObservableList} for
 * table and detail bindings, preserving the selected {@link Fund} when listings refresh.
 *
 * @author kevindmazali
 * @contributor kaamyashinde
 * @version 1.0.0
 * @since 2026-05-01
 */
public class FundsController {

  private final Exchange exchange;
  private final ObservableList<Fund> funds = FXCollections.observableArrayList();
  private final ObjectProperty<Fund> selectedFund = new SimpleObjectProperty<>();

  /**
   * Creates a fund-list controller and loads the initial rows.
   *
   * @param exchange exchange whose funds are listed
   */
  public FundsController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
    refresh();
  }

  /**
   * Exposes the exchange backing the fund listings.
   *
   * @return exchange backing the fund listings
   */
  public Exchange getExchange() {
    return exchange;
  }

  /**
   * Exposes fund rows for table and list bindings.
   *
   * @return observable fund rows sorted by symbol
   */
  public ObservableList<Fund> getFunds() {
    return funds;
  }

  /**
   * Exposes the selected fund property for bidirectional bindings.
   *
   * @return selected fund property for view bindings
   */
  public ObjectProperty<Fund> selectedFundProperty() {
    return selectedFund;
  }

  /**
   * Returns the currently selected fund.
   *
   * @return currently selected fund, or {@code null} when no fund is available
   */
  public Fund getSelectedFund() {
    return selectedFund.get();
  }

  /**
   * Updates the selected fund.
   *
   * @param fund fund to select, or {@code null} to clear selection
   */
  public void setSelectedFund(Fund fund) {
    selectedFund.set(fund);
  }

  /**
   * Formats the funds-page metadata line.
   *
   * @return compact exchange, day, and listing-count text for the funds page
   */
  public String getMetaText() {
    return exchange.getName()
        + " · trading day "
        + exchange.getDay()
        + " · "
        + exchange.listings().findFunds("").size()
        + " fund(s)";
  }

  /**
   * Reloads fund rows from the exchange, preserving selection when possible.
   */
  public void refresh() {
    Fund previous = selectedFund.get();
    List<Fund> sorted = new ArrayList<>(exchange.listings().findFunds(""));
    sorted.sort(Comparator.comparing(Fund::getSymbol));
    funds.setAll(sorted);
    if (previous != null) {
      for (Fund fund : sorted) {
        if (fund.getSymbol().equals(previous.getSymbol())) {
          selectedFund.set(fund);
          return;
        }
      }
    }
    if (selectedFund.get() == null && !sorted.isEmpty()) {
      selectedFund.set(sorted.get(0));
    }
  }
}
