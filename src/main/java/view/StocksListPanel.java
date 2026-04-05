package view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.market.Exchange;
import model.market.Stock;
import model.market.marketevent.unexpected.MarketEvent;
import model.stockinfo.StockFinancialInfoProvider;

/**
 * JavaFX panel listing all stocks on an {@link Exchange}: symbol, company, latest price, mock
 * revenue, and health indicator. Prices reflect the same {@link Stock} instances as the rest of the
 * demo; use {@link #refresh()} after trading days advance so the table re-renders updated values.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
public class StocksListPanel extends BorderPane {

  private final Exchange exchange;
  private final StockFinancialInfoProvider financialInfoProvider = new StockFinancialInfoProvider();
  private final Label metaLabel = new Label();
  private final TableView<Stock> table = new TableView<>();
  private final ObservableList<Stock> rows = FXCollections.observableArrayList();
  private final StockDetailView detailView = new StockDetailView();

  /**
   * Builds a read-only listing for the given exchange.
   *
   * @param exchange exchange whose listed stocks are shown (via
   *                 {@link Exchange#findStocks(String)})
   */
  public StocksListPanel(Exchange exchange) {
    this.exchange = exchange;

    setPadding(new Insets(16));

    Text heading = new Text("Available stocks");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));

    metaLabel.setWrapText(true);

    Button refreshBtn = new Button("Refresh");
    styleButton(refreshBtn);
    refreshBtn.setOnAction(_ -> refresh());

    HBox topRow = new HBox(16, heading, refreshBtn);
    topRow.setAlignment(Pos.CENTER_LEFT);

    VBox top = new VBox(8, topRow, metaLabel);
    setTop(top);

    buildTable();
    table.setItems(rows);
    table.setPlaceholder(new Label("No stocks available."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.getSelectionModel().selectedItemProperty().addListener((obs, previous, selected) ->
        detailView.showStock(
            selected,
            exchange.getDay(),
            exchange.getLastMarketEvent(),
            getMarketHistoryForStock(selected)));

    SplitPane splitPane = new SplitPane(table, detailView);
    splitPane.setDividerPositions(0.46);
    setCenter(splitPane);

    refresh();
  }

  private void buildTable() {
    TableColumn<Stock, String> colSym = new TableColumn<>("Symbol");
    colSym.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSymbol()));

    TableColumn<Stock, String> colCompany = new TableColumn<>("Company");
    colCompany.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCompany()));

    TableColumn<Stock, String> colPrice = new TableColumn<>("Latest price");
    colPrice.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getSalesPrice().toPlainString()));

    TableColumn<Stock, String> colRevenue = new TableColumn<>("Revenue (mock)");
    colRevenue.setCellValueFactory(
        c ->
            new SimpleStringProperty(
                financialInfoProvider.formatMoney(
                    financialInfoProvider.forStock(c.getValue()).revenue())));

    TableColumn<Stock, String> colHealth = new TableColumn<>("Health");
    colHealth.setCellValueFactory(
        c ->
            new SimpleStringProperty(
                financialInfoProvider.forStock(c.getValue()).health().displayLabel()));

    table.getColumns().setAll(List.of(colSym, colCompany, colPrice, colRevenue, colHealth));
  }

  /**
   * Reloads row order from the exchange, updates the day label, and forces the table to redraw
   * prices (needed after another tab advances the trading day).
   */
  public void refresh() {
    metaLabel.setText(
        exchange.getName()
            + " · trading day "
            + exchange.getDay()
            + " · "
            + exchange.findStocks("").size()
            + " listing(s)");
    List<Stock> sorted = new ArrayList<>(exchange.findStocks(""));
    sorted.sort(Comparator.comparing(Stock::getSymbol));
    Stock previousSelection = table.getSelectionModel().getSelectedItem();
    rows.setAll(sorted);
    restoreSelection(previousSelection, sorted);
    if (table.getSelectionModel().getSelectedItem() == null && !sorted.isEmpty()) {
      table.getSelectionModel().selectFirst();
    }
    table.refresh();
    detailView.refresh(
        exchange.getDay(),
        exchange.getLastMarketEvent(),
        getMarketHistoryForStock(detailView.getSelectedStock()));
  }

  /**
   * Returns the embedded stock detail view used by this list panel.
   *
   * @return detail view bound to the current table selection
   */
  public StockDetailView getDetailView() {
    return detailView;
  }

  /**
   * Restores the previous stock selection after the backing rows are rebuilt.
   *
   * @param previousSelection previously selected stock, possibly null
   * @param sorted            current sorted table rows
   */
  private void restoreSelection(Stock previousSelection, List<Stock> sorted) {
    if (previousSelection == null) {
      table.getSelectionModel().clearSelection();
      return;
    }
    for (Stock stock : sorted) {
      if (stock.getSymbol().equals(previousSelection.getSymbol())) {
        table.getSelectionModel().select(stock);
        return;
      }
    }
    table.getSelectionModel().clearSelection();
  }

  /**
   * Returns the market-event history relevant to the given stock.
   *
   * @param stock selected stock, or {@code null} when no row is selected
   * @return immutable list of relevant market events
   */
  private List<MarketEvent> getMarketHistoryForStock(Stock stock) {
    if (stock == null) {
      return List.of();
    }
    return exchange.getMarketEventsForStock(stock.getSymbol());
  }

  private static void styleButton(Button b) {
    b.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }
}
