package view.pages.funds;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import controller.FundsController;
import controller.TradingController;
import model.core.asset.fund.Fund;
import view.theme.ThemeStyles;
import view.util.UiFormat;

/**
 * JavaFX panel listing all funds on an exchange together with a composition detail view.
 */
public class FundsPage extends BorderPane {

  private final FundsController controller;
  private final Label metaLabel = new Label();
  private final TextField searchField = new TextField();
  private final TableView<Fund> table = new TableView<>();
  private final FundDetailView detailView = new FundDetailView();

  /**
   * Builds a fund listing for the given exchange.
   *
   * @param controller controller supplying listed funds
   * @param trading trading controller for buy actions in the detail pane
   * @param onTradeComplete invoked after a successful trade
   */
  public FundsPage(FundsController controller, TradingController trading, Runnable onTradeComplete) {
    this.controller = controller;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");

    Text heading = new Text("Available Funds");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");
    ThemeStyles.addStyleClasses(metaLabel, "finance-meta");
    VBox.setMargin(metaLabel, new Insets(0, 0, 8, 0));
    searchField.setPromptText("Search by symbol or fund name");
    searchField.setId("funds-search-field");
    ThemeStyles.styleField(searchField);
    searchField.textProperty().addListener((_, _, value) -> {
      controller.setSearchTerm(value);
      syncTableSelection();
      detailView.showFund(controller.getSelectedFund());
      updateMetaText();
    });

    HBox topRow = new HBox(16, heading);
    topRow.setAlignment(Pos.CENTER_LEFT);
    VBox top = new VBox(8, topRow, searchField, metaLabel);
    setTop(top);

    buildTable();
    table.setItems(controller.getFunds());
    table.setPlaceholder(new Label("No funds available."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldFund, newFund) ->
        detailView.showFund(newFund));

    detailView.setTradeHandlers(
        trading, () -> getScene() != null ? getScene().getWindow() : null, onTradeComplete);

    SplitPane splitPane = new SplitPane(table, detailView);
    splitPane.setDividerPositions(0.46);
    setCenter(splitPane);

    refresh();
  }

  /**
   * Reloads row order from the exchange and refreshes the selected detail panel.
   */
  public void refresh() {
    controller.refresh();
    syncTableSelection();
    updateMetaText();
    table.refresh();
    detailView.showFund(controller.getSelectedFund());
  }

  private void buildTable() {
    TableColumn<Fund, String> symbolColumn = new TableColumn<>("Symbol");
    symbolColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSymbol()));

    TableColumn<Fund, String> nameColumn = new TableColumn<>("Fund");
    nameColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(cell.getValue().getDisplayName()));

    TableColumn<Fund, String> priceColumn = new TableColumn<>("Latest Price");
    priceColumn.setCellValueFactory(
        cell -> new SimpleStringProperty(UiFormat.decimal(cell.getValue().getSalesPrice())));

    table.getColumns().setAll(List.of(symbolColumn, nameColumn, priceColumn));
  }

  public String getSearchText() {
    return searchField.getText();
  }

  public FundDetailView getDetailView() {
    return detailView;
  }

  private void syncTableSelection() {
    Fund selected = controller.getSelectedFund();
    if (selected != null) {
      table.getSelectionModel().select(selected);
    } else {
      table.getSelectionModel().clearSelection();
    }
  }

  private void updateMetaText() {
    metaLabel.setText(controller.getMetaText());
  }
}
