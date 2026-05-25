package view.pages.savings;

import controller.SavingsController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.core.asset.InvestableAsset;
import model.trading.savings.RegularSavingsPlan;
import model.trading.savings.SavingsInstallmentMode;
import view.theme.ThemeStyles;

/**
 * Regular savings plans page: list, add, edit, and remove plans.
 */
public class SavingsPage extends BorderPane {

  private final SavingsController controller;
  private final Runnable afterModelChange;

  private final Label dayLabel = new Label();
  private final TableView<RegularSavingsPlan> table = new TableView<>();
  private final ComboBox<InvestableAsset> addAsset = new ComboBox<>();
  private final ComboBox<SavingsInstallmentMode> addMode =
      new ComboBox<>(FXCollections.observableArrayList(SavingsInstallmentMode.values()));
  private final TextField addAmount = new TextField();
  private final TextField addInterval = new TextField();
  private final ComboBox<SavingsInstallmentMode> editMode =
      new ComboBox<>(FXCollections.observableArrayList(SavingsInstallmentMode.values()));
  private final TextField editAmount = new TextField();
  private final TextField editInterval = new TextField();
  private final TextField editNextDue = new TextField();
  private final CheckBox editActive = new CheckBox("Active");
  private final GridPane editGrid = new GridPane();
  private final Label status = new Label();

  /**
   * Creates a regular savings page.
   *
   * @param controller savings controller
   * @param afterModelChange invoked after successful mutations
   */
  public SavingsPage(SavingsController controller, Runnable afterModelChange) {
    this.controller = controller;
    this.afterModelChange = afterModelChange;
    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page");
    updateDayLabel();

    HBox top = new HBox(16, dayLabel);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);

    buildTable();
    table.setItems(controller.getPlans());
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    setCenter(table);

    addMode.setValue(SavingsInstallmentMode.FIXED_SHARES);
    addAsset.setItems(controller.getListedAssets());
    addAsset.setPromptText("Asset");
    configureAssetCombo(addAsset);
    ThemeStyles.styleField(addAmount);
    ThemeStyles.styleField(addInterval);
    addAmount.setPromptText("Amount");
    addInterval.setPromptText("Interval (days)");

    Button addBtn = new Button("Add plan");
    ThemeStyles.styleButton(addBtn);
    addBtn.setOnAction(_ -> addPlan());

    GridPane addGrid = new GridPane();
    addGrid.setHgap(8);
    addGrid.setVgap(8);
    addGrid.addRow(0, new Label("New plan"), addAsset, addMode, addAmount, addInterval, addBtn);

    editMode.setValue(SavingsInstallmentMode.FIXED_SHARES);
    ThemeStyles.styleField(editAmount);
    ThemeStyles.styleField(editInterval);
    ThemeStyles.styleField(editNextDue);
    Button applyBtn = new Button("Apply to selected");
    Button removeBtn = new Button("Remove selected");
    ThemeStyles.styleButton(applyBtn);
    ThemeStyles.styleButton(removeBtn);
    applyBtn.setOnAction(_ -> applyEdit());
    removeBtn.setOnAction(_ -> removeSelected());

    editGrid.setHgap(8);
    editGrid.setVgap(8);
    editGrid.addRow(
        0,
        new Label("Edit selected"),
        editMode,
        editAmount,
        editInterval,
        editNextDue,
        editActive,
        applyBtn,
        removeBtn);
    editGrid.setVisible(false);
    editGrid.setManaged(false);

    table.getSelectionModel().selectedItemProperty().addListener((obs, prev, sel) -> {
      if (sel != null) {
        editMode.setValue(sel.getMode());
        editAmount.setText(sel.getAmount().toPlainString());
        editInterval.setText(Integer.toString(sel.getIntervalDays()));
        editNextDue.setText(Integer.toString(sel.getNextDueDay()));
        editActive.setSelected(sel.isActive());
      }
    });

    VBox bottom = new VBox(12, new Separator(), addGrid, editGrid, status);
    bottom.setPadding(new Insets(12, 0, 0, 0));
    ThemeStyles.addStyleClasses(status, "finance-status");
    setBottom(bottom);
  }

  private void buildTable() {
    TableColumn<RegularSavingsPlan, String> colSym = new TableColumn<>("Symbol");
    colSym.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSymbol()));
    TableColumn<RegularSavingsPlan, String> colMode = new TableColumn<>("Mode");
    colMode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMode().name()));
    TableColumn<RegularSavingsPlan, String> colAmt = new TableColumn<>("Amount");
    colAmt.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getAmount().toPlainString()));
    TableColumn<RegularSavingsPlan, String> colInt = new TableColumn<>("Interval");
    colInt.setCellValueFactory(
        c -> new SimpleStringProperty(Integer.toString(c.getValue().getIntervalDays())));
    TableColumn<RegularSavingsPlan, String> colDue = new TableColumn<>("Next due");
    colDue.setCellValueFactory(
        c -> new SimpleStringProperty(Integer.toString(c.getValue().getNextDueDay())));
    TableColumn<RegularSavingsPlan, Boolean> colAct = new TableColumn<>("Active");
    colAct.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().isActive()));
    TableColumn<RegularSavingsPlan, Void> colEdit = new TableColumn<>("Edit");
    colEdit.setCellFactory(
        _ ->
            new TableCell<>() {
              private final Button editBtn = new Button("Edit");

              {
                ThemeStyles.styleButton(editBtn);
                editBtn.setOnAction(
                    _ -> {
                      int index = getIndex();
                      if (index >= 0 && !isEmpty()) {
                        table.getSelectionModel().select(index);
                        editGrid.setVisible(true);
                        editGrid.setManaged(true);
                        editMode.requestFocus();
                      }
                    });
              }

              @Override
              protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                  setGraphic(null);
                } else {
                  setGraphic(editBtn);
                }
              }
            });
    table.getColumns().setAll(colSym, colMode, colAmt, colInt, colDue, colAct, colEdit);
  }

  /** Refreshes the trading-day label from the current exchange state. */
  public void refresh() {
    updateDayLabel();
  }

  private void addPlan() {
    status.setText("");
    try {
      controller.addPlan(addAsset.getValue(), addMode.getValue(), addAmount.getText(), addInterval.getText());
      addAsset.setValue(null);
      addAmount.clear();
      addInterval.clear();
      afterModelChange.run();
    } catch (RuntimeException ex) {
      status.setText("Invalid amount or interval.");
    }
  }

  private void applyEdit() {
    status.setText("");
    RegularSavingsPlan plan = table.getSelectionModel().getSelectedItem();
    if (plan == null) {
      status.setText("Select a plan to edit.");
      return;
    }
    try {
      controller.applyEdit(
          plan,
          editMode.getValue(),
          editAmount.getText(),
          editInterval.getText(),
          editNextDue.getText(),
          editActive.isSelected());
      afterModelChange.run();
      hideEditForm();
    } catch (RuntimeException ex) {
      status.setText("Invalid edit: check numbers and positive values.");
    }
  }

  private void removeSelected() {
    status.setText("");
    int idx = table.getSelectionModel().getSelectedIndex();
    if (idx < 0) {
      status.setText("Select a plan to remove.");
      return;
    }
    if (controller.removePlanAt(idx)) {
      afterModelChange.run();
      hideEditForm();
    }
  }

  private void hideEditForm() {
    editGrid.setVisible(false);
    editGrid.setManaged(false);
    table.getSelectionModel().clearSelection();
  }

  private void updateDayLabel() {
    dayLabel.setText("Trading Day: " + controller.getTradingDay());
  }

  private static void configureAssetCombo(ComboBox<InvestableAsset> combo) {
    combo.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(InvestableAsset asset) {
            return formatAssetLabel(asset);
          }

          @Override
          public InvestableAsset fromString(String s) {
            return null;
          }
        });
    combo.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(InvestableAsset item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatAssetLabel(item));
              }
            });
    combo.setButtonCell(
        new ListCell<>() {
          @Override
          protected void updateItem(InvestableAsset item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : formatAssetLabel(item));
          }
        });
  }

  private static String formatAssetLabel(InvestableAsset asset) {
    if (asset == null) {
      return "";
    }
    return asset.getSymbol() + " — " + asset.getDisplayName() + " (" + asset.getAssetType() + ")";
  }
}
