package view;

import controller.RegularSavingsPanelController;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Player;
import model.market.Exchange;
import model.market.InvestableAsset;
import model.savings.RegularSavingsPlan;
import model.savings.RegularSavingsProcessor;
import model.savings.SavingsInstallmentMode;
import view.components.notification.NotificationService;
import view.components.toast.ToastMode;

/**
 * JavaFX panel for demoing {@link RegularSavingsPlan} against an {@link Exchange} and
 * {@link Player}: list plans, add (asset chosen from {@link RegularSavingsPanelController}
 * listing), edit in place (symbol stays fixed; use remove + add to change ticker), remove, and
 * advance the exchange one day while running {@link RegularSavingsProcessor}. Skipped installments
 * surface as warning toasts.
 *
 * @author kevindmazali
 * @version 1.0.0
 * @since 30-03-2026
 */
public class RegularSavingsPanel extends BorderPane {

  private static final String FIELD_STYLE =
      "-fx-border-radius: 6;"
          + "-fx-background-radius: 6;";

  private final Exchange exchange;
  private final RegularSavingsPanelController savingsController;
  private final Player player;
  private final NotificationService notifications;
  private final Runnable afterModelChange;

  private final Label dayLabel = new Label();
  private final TableView<RegularSavingsPlan> table = new TableView<>();
  private final ObservableList<RegularSavingsPlan> plans = FXCollections.observableArrayList();

  private final ComboBox<InvestableAsset> addAsset = new ComboBox<>();
  private final ComboBox<SavingsInstallmentMode> addMode = new ComboBox<>(
      FXCollections.observableArrayList(SavingsInstallmentMode.values()));
  private final TextField addAmount = new TextField();
  private final TextField addInterval = new TextField();

  private final ComboBox<SavingsInstallmentMode> editMode = new ComboBox<>(
      FXCollections.observableArrayList(SavingsInstallmentMode.values()));
  private final TextField editAmount = new TextField();
  private final TextField editInterval = new TextField();
  private final TextField editNextDue = new TextField();
  private final CheckBox editActive = new CheckBox("Active");

  private final Label status = new Label();

  /**
   * Builds a panel wired to the given exchange and player; mutations go through the domain API.
   *
   * @param exchange      exchange whose day advances and whose listed assets validate new symbols
   * @param player        player whose plans are listed and edited
   * @param notifications service used for insufficient-fund skips after advancing a day
   */
  public RegularSavingsPanel(Exchange exchange, Player player, NotificationService notifications) {
    this(exchange, player, notifications, () -> {
    });
  }

  /**
   * Builds a panel wired to the given exchange and player and invokes a callback after each
   * successful model mutation.
   *
   * @param exchange         exchange whose day advances and whose listed assets validate new
   *                         symbols
   * @param player           player whose plans are listed and edited
   * @param notifications    service used for insufficient-fund skips after advancing a day
   * @param afterModelChange callback invoked after a successful add, edit, remove, or day advance
   */
  public RegularSavingsPanel(
      Exchange exchange,
      Player player,
      NotificationService notifications,
      Runnable afterModelChange) {
    this.exchange = exchange;
    this.savingsController = new RegularSavingsPanelController(exchange);
    this.player = player;
    this.notifications = notifications;
    this.afterModelChange = afterModelChange;

    setPadding(new Insets(16));

    dayLabel.setText("Trading day: " + exchange.getDay());

    Button advanceBtn = new Button("Advance 1 trading day");
    styleButton(advanceBtn);
    advanceBtn.setOnAction(_ -> advanceOneDay());

    HBox top = new HBox(16, dayLabel, advanceBtn);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);

    buildTable();
    table.setItems(plans);
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    VBox.setVgrow(table, Priority.ALWAYS);
    setCenter(table);

    addMode.setValue(SavingsInstallmentMode.FIXED_SHARES);
    addAsset.setItems(savingsController.getListedAssets());
    addAsset.setPromptText("Asset");
    configureAssetCombo(addAsset);
    addAmount.setPromptText("Amount");
    addInterval.setPromptText("Interval (days)");
    styleCombo(addAsset);
    styleField(addAmount);
    styleField(addInterval);
    styleCombo(addMode);

    Button addBtn = new Button("Add plan");
    styleButton(addBtn);
    addBtn.setOnAction(_ -> addPlan());

    Label addHeading = new Label("New plan");
    GridPane addGrid = new GridPane();
    addGrid.setHgap(8);
    addGrid.setVgap(8);
    addGrid.addRow(0, addHeading, addAsset, addMode, addAmount, addInterval, addBtn);

    editMode.setValue(SavingsInstallmentMode.FIXED_SHARES);
    styleCombo(editMode);
    styleField(editAmount);
    styleField(editInterval);
    styleField(editNextDue);

    Button applyBtn = new Button("Apply to selected");
    Button removeBtn = new Button("Remove selected");
    styleButton(applyBtn);
    styleButton(removeBtn);
    applyBtn.setOnAction(_ -> applyEdit());
    removeBtn.setOnAction(_ -> removeSelected());

    Label editHeading = new Label("Edit selected");
    GridPane editGrid = new GridPane();
    editGrid.setHgap(8);
    editGrid.setVgap(8);
    editGrid.addRow(0, editHeading, editMode, editAmount, editInterval, editNextDue, editActive,
        applyBtn, removeBtn);

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
    setBottom(bottom);

    refreshTable();
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

    table.getColumns()
        .setAll(Arrays.<TableColumn<RegularSavingsPlan, ?>>asList(
            colSym, colMode, colAmt, colInt, colDue, colAct));
  }

  private void advanceOneDay() {
    status.setText("");
    int before = exchange.getDay();
    exchange.advance();
    dayLabel.setText("Trading day: " + exchange.getDay());
    List<String> skipped =
        RegularSavingsProcessor.run(exchange, player, before, exchange.getDay());
    refreshTable();
    if (!skipped.isEmpty()) {
      status.setText(
          "Regular savings skipped (insufficient funds): "
              + String.join(", ", skipped)
              + ". See Notifications.");
    }
    exchange.getLastMarketEvent().ifPresent(event -> notifications.show(
        ToastMode.INFO,
        event.title(),
        event.description()));
    for (String sym : skipped) {
      notifications.show(ToastMode.WARNING, "Regular savings skipped",
          "Insufficient funds for " + sym + ".");
    }
    afterModelChange.run();
  }

  private void addPlan() {
    status.setText("");
    InvestableAsset picked = addAsset.getValue();
    if (picked == null) {
      status.setText("Choose an asset.");
      return;
    }
    String sym = picked.getSymbol();
    SavingsInstallmentMode mode = addMode.getValue();
    if (mode == null) {
      status.setText("Choose a mode.");
      return;
    }
    try {
      BigDecimal amount = new BigDecimal(addAmount.getText().trim());
      int interval = Integer.parseInt(addInterval.getText().trim());
      RegularSavingsPlan plan =
          new RegularSavingsPlan(sym, mode, amount, interval, exchange.getDay());
      player.addRegularSavingsPlan(plan);
      refreshTable();
      addAsset.setValue(null);
      addAmount.clear();
      addInterval.clear();
      afterModelChange.run();
    } catch (RuntimeException ex) {
      status.setText("Invalid amount or interval.");
    }
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

  private void applyEdit() {
    status.setText("");
    RegularSavingsPlan p = table.getSelectionModel().getSelectedItem();
    if (p == null) {
      status.setText("Select a plan to edit.");
      return;
    }
    SavingsInstallmentMode mode = editMode.getValue();
    if (mode == null) {
      status.setText("Choose a mode.");
      return;
    }
    try {
      p.setMode(mode);
      p.setAmount(new BigDecimal(editAmount.getText().trim()));
      p.setIntervalDays(Integer.parseInt(editInterval.getText().trim()));
      p.setNextDueDay(Integer.parseInt(editNextDue.getText().trim()));
      p.setActive(editActive.isSelected());
      refreshTable();
      table.getSelectionModel().select(p);
      afterModelChange.run();
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
    if (player.removeRegularSavingsPlanAt(idx + 1)) {
      refreshTable();
      afterModelChange.run();
    }
  }

  private void refreshTable() {
    plans.setAll(player.getRegularSavingsPlans());
  }

  private static void styleField(TextField f) {
    f.setStyle(FIELD_STYLE);
  }

  private static void styleCombo(ComboBox<?> c) {
    c.setStyle(FIELD_STYLE);
  }

  private static void styleButton(Button b) {
    b.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }
}
