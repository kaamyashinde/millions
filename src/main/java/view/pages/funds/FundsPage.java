package view.pages.funds;

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
import model.Exchange;
import model.fund.Fund;
import view.theme.ThemeStyles;

/**
 * JavaFX panel listing all funds on an exchange together with a composition detail view.
 */
public class FundsPage extends BorderPane {

  private final Exchange exchange;
  private final Label metaLabel = new Label();
  private final TableView<Fund> table = new TableView<>();
  private final ObservableList<Fund> rows = FXCollections.observableArrayList();
  private final FundDetailView detailView = new FundDetailView();

  /**
   * Builds a read-only fund listing for the given exchange.
   *
   * @param exchange exchange whose listed funds are shown
   */
  public FundsPage(Exchange exchange) {
    this.exchange = exchange;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Available Funds");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");
    ThemeStyles.addStyleClasses(metaLabel, "finance-meta");
    VBox.setMargin(metaLabel, new Insets(0, 0, 8, 0));

    Button refreshBtn = new Button("Refresh");
    styleButton(refreshBtn);
    refreshBtn.setOnAction(_ -> refresh());

    HBox topRow = new HBox(16, heading, refreshBtn);
    topRow.setAlignment(Pos.CENTER_LEFT);
    VBox top = new VBox(4, topRow, metaLabel);
    setTop(top);

    buildTable();
    table.setItems(rows);
    table.setPlaceholder(new Label("No funds available."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldFund, newFund) ->
        detailView.showFund(newFund));

    SplitPane splitPane = new SplitPane(table, detailView);
    splitPane.setDividerPositions(0.46);
    setCenter(splitPane);

    refresh();
  }

  /**
   * Reloads row order from the exchange and refreshes the selected detail panel.
   */
  public void refresh() {
    metaLabel.setText(
        exchange.getName()
            + " · "
            + exchange.getFunds().size()
            + " Fund Listing(s)");
    List<Fund> sorted = new ArrayList<>(exchange.findFunds(""));
    sorted.sort(Comparator.comparing(Fund::getSymbol));
    Fund previousSelection = table.getSelectionModel().getSelectedItem();
    rows.setAll(sorted);
    restoreSelection(previousSelection, sorted);
    if (table.getSelectionModel().getSelectedItem() == null && !sorted.isEmpty()) {
      table.getSelectionModel().selectFirst();
    }
    table.refresh();
    detailView.refresh();
  }

  private void buildTable() {
    TableColumn<Fund, String> symbolColumn = new TableColumn<>("Symbol");
    symbolColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSymbol()));

    TableColumn<Fund, String> nameColumn = new TableColumn<>("Fund");
    nameColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().getDisplayName()));

    TableColumn<Fund, String> priceColumn = new TableColumn<>("Latest Price");
    priceColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().getSalesPrice().toPlainString()));

    table.getColumns().setAll(List.of(symbolColumn, nameColumn, priceColumn));
  }

  private void restoreSelection(Fund previousSelection, List<Fund> sorted) {
    if (previousSelection == null) {
      table.getSelectionModel().clearSelection();
      return;
    }
    for (Fund fund : sorted) {
      if (fund.getSymbol().equals(previousSelection.getSymbol())) {
        table.getSelectionModel().select(fund);
        return;
      }
    }
    table.getSelectionModel().clearSelection();
  }

  private static void styleButton(Button button) {
    ThemeStyles.styleButton(button);
  }
}
