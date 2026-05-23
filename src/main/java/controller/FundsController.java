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
import model.core.asset.fund.Fund;

/**
 * Supplies sorted fund listings and selection state for the funds tab.
 */
public class FundsController {

  private final Exchange exchange;
  private final ObservableList<Fund> funds = FXCollections.observableArrayList();
  private final ObjectProperty<Fund> selectedFund = new SimpleObjectProperty<>();

  /**
   * @param exchange exchange whose funds are listed
   */
  public FundsController(Exchange exchange) {
    checkNotNull(exchange, "Exchange");
    this.exchange = exchange;
    refresh();
  }

  public Exchange getExchange() {
    return exchange;
  }

  public ObservableList<Fund> getFunds() {
    return funds;
  }

  public ObjectProperty<Fund> selectedFundProperty() {
    return selectedFund;
  }

  public Fund getSelectedFund() {
    return selectedFund.get();
  }

  public void setSelectedFund(Fund fund) {
    selectedFund.set(fund);
  }

  public String getMetaText() {
    return exchange.getName()
        + " · trading day "
        + exchange.getDay()
        + " · "
        + exchange.findFunds("").size()
        + " fund(s)";
  }

  /**
   * Reloads fund rows from the exchange, preserving selection when possible.
   */
  public void refresh() {
    Fund previous = selectedFund.get();
    List<Fund> sorted = new ArrayList<>(exchange.findFunds(""));
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
