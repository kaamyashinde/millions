package old_view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.persistence.SavedRunRecord;
import model.session.SessionService;

/**
 * Lists saved playthrough snapshots and allows saving, leaderboard selection, and deletion.
 */
public class SavedRunsPanel extends BorderPane {

  private static final String CARD_STYLE =
      "-fx-background-color: #f6f7fb;"
          + "-fx-border-color: #d7dce5;"
          + "-fx-background-radius: 12;"
          + "-fx-border-radius: 12;"
          + "-fx-padding: 14;";

  private final SessionService sessionService;
  private final Runnable afterMutation;
  private final TableView<SavedRunRow> table = new TableView<>();

  /**
   * @param sessionService session API for run persistence
   * @param afterMutation  invoked after save/delete/leaderboard change (e.g. persist parent state)
   */
  public SavedRunsPanel(SessionService sessionService, Runnable afterMutation) {
    this.sessionService = sessionService;
    this.afterMutation = afterMutation;

    setPadding(new Insets(16));
    setStyle(CARD_STYLE);

    Text heading = new Text("Saved playthroughs");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));

    Label hint = new Label(
        "Save snapshots to compare strategies. Trading day count is the in-game duration.");
    hint.setWrapText(true);
    hint.setStyle("-fx-text-fill: #444;");

    Button saveButton = new Button("Save current run");
    styleButton(saveButton);
    saveButton.setOnAction(_ -> promptSaveRun());

    TableColumn<SavedRunRow, String> savedCol = new TableColumn<>("Saved");
    savedCol.setCellValueFactory(c -> c.getValue().savedAtProperty());
    savedCol.setPrefWidth(160);

    TableColumn<SavedRunRow, String> labelCol = new TableColumn<>("Label");
    labelCol.setCellValueFactory(c -> c.getValue().labelProperty());
    labelCol.setPrefWidth(120);

    TableColumn<SavedRunRow, Number> daysCol = new TableColumn<>("Days");
    daysCol.setCellValueFactory(c -> c.getValue().tradingDaysProperty());
    daysCol.setPrefWidth(56);

    TableColumn<SavedRunRow, String> netWorthCol = new TableColumn<>("Net worth");
    netWorthCol.setCellValueFactory(c -> c.getValue().netWorthProperty());
    netWorthCol.setPrefWidth(110);

    TableColumn<SavedRunRow, String> returnCol = new TableColumn<>("Return %");
    returnCol.setCellValueFactory(c -> c.getValue().returnPercentProperty());
    returnCol.setPrefWidth(90);

    TableColumn<SavedRunRow, SavedRunRow> leaderboardCol = new TableColumn<>("Leaderboard");
    leaderboardCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
    leaderboardCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(SavedRunRow row, boolean empty) {
        super.updateItem(row, empty);
        if (empty || row == null) {
          setGraphic(null);
          return;
        }
        CheckBox box = new CheckBox();
        box.setSelected(row.record().eligibleForLeaderboard());
        box.setOnAction(ev -> {
          ev.consume();
          UUID id = UUID.fromString(row.record().runId());
          sessionService.setRunLeaderboardEligible(id, box.isSelected());
          afterMutation.run();
          refresh();
        });
        setGraphic(box);
      }
    });
    leaderboardCol.setPrefWidth(100);

    TableColumn<SavedRunRow, SavedRunRow> actionsCol = new TableColumn<>("Actions");
    actionsCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
    actionsCol.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(SavedRunRow row, boolean empty) {
        super.updateItem(row, empty);
        if (empty || row == null) {
          setGraphic(null);
          return;
        }
        Button delete = new Button("Delete");
        styleButton(delete);
        delete.setOnAction(ev -> confirmDelete(row.record()));
        setGraphic(delete);
      }
    });
    actionsCol.setPrefWidth(90);

    table.getColumns().setAll(
        List.of(savedCol, labelCol, daysCol, netWorthCol, returnCol, leaderboardCol, actionsCol));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPrefHeight(400);

    VBox top = new VBox(8, heading, hint, saveButton);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);
    setCenter(table);

    refresh();
  }

  /**
   * Reloads rows from disk.
   */
  public void refresh() {
    table.getItems().clear();
    for (SavedRunRecord record : sessionService.listSavedRuns()) {
      table.getItems().add(SavedRunRow.fromRecord(record));
    }
  }

  private void promptSaveRun() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Save playthrough");
    dialog.setHeaderText("Optional label (e.g. \"dividend focus\")");
    dialog.setContentText("Label:");
    dialog.showAndWait().ifPresent(label -> {
      sessionService.saveCurrentRun(label.trim());
      afterMutation.run();
      refresh();
    });
  }

  private void confirmDelete(SavedRunRecord record) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete saved run");
    alert.setHeaderText("Remove this playthrough snapshot?");
    alert.setContentText("Label: " + (record.label().isEmpty() ? "(none)" : record.label()));
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(_ -> {
      sessionService.deleteSavedRun(UUID.fromString(record.runId()));
      afterMutation.run();
      refresh();
    });
  }

  private static void styleButton(Button button) {
    button.setStyle(
        "-fx-border-radius: 6;"
            + "-fx-background-radius: 6;"
            + "-fx-cursor: hand;");
  }

  /**
   * Table row with JavaFX properties for columns.
   */
  public static final class SavedRunRow {
    private final SavedRunRecord record;
    private final StringProperty savedAt = new SimpleStringProperty();
    private final StringProperty label = new SimpleStringProperty();
    private final IntegerProperty tradingDays = new SimpleIntegerProperty();
    private final StringProperty netWorth = new SimpleStringProperty();
    private final StringProperty returnPercent = new SimpleStringProperty();

    private SavedRunRow(SavedRunRecord record) {
      this.record = record;
    }

    static SavedRunRow fromRecord(SavedRunRecord record) {
      SavedRunRow row = new SavedRunRow(record);
      row.savedAt.set(record.savedAt());
      row.label.set(record.label().isEmpty() ? "—" : record.label());
      row.tradingDays.set(record.tradingDays());
      row.netWorth.set(formatMoney(record.stats().netWorth()));
      row.returnPercent.set(formatReturn(record.stats().portfolioReturnPercent()));
      return row;
    }

    SavedRunRecord record() {
      return record;
    }

    StringProperty savedAtProperty() {
      return savedAt;
    }

    StringProperty labelProperty() {
      return label;
    }

    IntegerProperty tradingDaysProperty() {
      return tradingDays;
    }

    StringProperty netWorthProperty() {
      return netWorth;
    }

    StringProperty returnPercentProperty() {
      return returnPercent;
    }

    private static String formatMoney(BigDecimal v) {
      return v == null ? "—" : v.toPlainString();
    }

    private static String formatReturn(BigDecimal v) {
      if (v == null) {
        return "—";
      }
      return v.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }
  }
}
