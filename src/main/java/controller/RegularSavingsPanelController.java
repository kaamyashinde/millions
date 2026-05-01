package controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Exchange;
import model.InvestableAsset;

/**
 * Supplies UI-ready data for the savings page: investable assets listed on an
 * {@link Exchange}, sorted by symbol, for combo-box selection when creating a plan.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
public class RegularSavingsPanelController {

  private final Exchange exchange;
  private final ObservableList<InvestableAsset> listedAssets = FXCollections.observableArrayList();

  /**
   * Creates a controller for the given exchange and loads the initial asset list.
   *
   * @param exchange exchange whose listings populate the observable list
   */
  public RegularSavingsPanelController(Exchange exchange) {
    this.exchange = exchange;
    refreshListedAssets();
  }

  /**
   * Returns the mutable observable list backing asset pickers; sorted by symbol after each
   * {@link #refreshListedAssets()}.
   *
   * @return assets on this exchange, for {@link javafx.scene.control.ComboBox} items
   */
  public ObservableList<InvestableAsset> getListedAssets() {
    return listedAssets;
  }

  /**
   * Rebuilds the listed assets from the exchange (same order: symbol ascending). Call if listings
   * can change at runtime.
   */
  public void refreshListedAssets() {
    List<InvestableAsset> sorted = new ArrayList<>(exchange.findAssets(""));
    sorted.sort(Comparator.comparing(InvestableAsset::getSymbol));
    listedAssets.setAll(sorted);
  }
}
