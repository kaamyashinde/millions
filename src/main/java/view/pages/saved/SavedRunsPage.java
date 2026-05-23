package view.pages.saved;

import java.math.BigDecimal;
import java.util.List;
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
import model.persistence.ProfileFile;
import model.session.SessionService;
import view.theme.ThemeStyles;

/**
 * Lists saved playthrough snapshots and allows saving, leaderboard selection, and deletion.
 */
public class SavedRunsPage extends BorderPane {

  private final SessionService sessionService;
  private final Runnable afterMutation;
  private final TableView<SavedRunRow> table = new TableView<>();

  public SavedRunsPage(SessionService sessionService, Runnable afterMutation) {
    this.sessionService = sessionService;
    this.afterMutation = afterMutation;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page", "finance-panel");

    Text heading = new Text("Saved Playthroughs");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    Label hint = new Label(
        "Save snapshots to compare strategies. Trading day count is the in-game duration.");
    hint.setWrapText(true);
    ThemeStyles.addStyleClasses(hint, "finance-meta");

    Button saveButton = new Button("Save Current Run");
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

    TableColumn<SavedRunRow, String> cashCol = new TableColumn<>("Cash");
    cashCol.setCellValueFactory(c -> c.getValue().cashProperty());
    cashCol.setPrefWidth(100);

    TableColumn<SavedRunRow, String> netWorthCol = new TableColumn<>("Net Worth");
    netWorthCol.setCellValueFactory(c -> c.getValue().netWorthProperty());
    netWorthCol.setPrefWidth(110);

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
          sessionService.setRunLeaderboardEligible(row.record().id(), box.isSelected());
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
        List.of(savedCol, labelCol, daysCol, cashCol, netWorthCol, leaderboardCol, actionsCol));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    table.setPrefHeight(400);

    VBox top = new VBox(8, heading, hint, saveButton);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);
    setCenter(table);

    refresh();
  }

  public void refresh() {
    table.getItems().clear();
    for (ProfileFile.SavedRunRow record : sessionService.listSavedRuns()) {
      table.getItems().add(SavedRunRow.fromRecord(record));
    }
  }

  private void promptSaveRun() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Save playthrough");
    dialog.setHeaderText("Optional label (e.g. \"dividend focus\")");
    dialog.setContentText("Label:");
    ThemeStyles.installOnDialog(dialog);
    dialog.showAndWait().ifPresent(label -> {
      sessionService.saveCurrentRun(label.trim());
      afterMutation.run();
      refresh();
    });
  }

  private void confirmDelete(ProfileFile.SavedRunRow record) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Delete saved run");
    alert.setHeaderText("Remove this playthrough snapshot?");
    alert.setContentText("Label: " + (record.label().isEmpty() ? "(none)" : record.label()));
    ThemeStyles.installOnDialog(alert);
    alert.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(_ -> {
      sessionService.deleteSavedRun(record.id());
      afterMutation.run();
      refresh();
    });
  }

  private static void styleButton(Button button) {
    ThemeStyles.styleButton(button);
  }

  public static final class SavedRunRow {
    private final ProfileFile.SavedRunRow record;
    private final StringProperty savedAt = new SimpleStringProperty();
    private final StringProperty label = new SimpleStringProperty();
    private final IntegerProperty tradingDays = new SimpleIntegerProperty();
    private final StringProperty cash = new SimpleStringProperty();
    private final StringProperty netWorth = new SimpleStringProperty();

    private SavedRunRow(ProfileFile.SavedRunRow record) {
      this.record = record;
    }

    static SavedRunRow fromRecord(ProfileFile.SavedRunRow record) {
      SavedRunRow row = new SavedRunRow(record);
      row.savedAt.set(record.savedAt());
      row.label.set(record.label().isEmpty() ? "—" : record.label());
      row.tradingDays.set(record.day());
      row.cash.set(formatMoney(record.cash()));
      row.netWorth.set(formatMoney(record.netWorth()));
      return row;
    }

    ProfileFile.SavedRunRow record() {
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

    StringProperty cashProperty() {
      return cash;
    }

    StringProperty netWorthProperty() {
      return netWorth;
    }

    private static String formatMoney(BigDecimal value) {
      return value == null ? "—" : value.toPlainString();
    }
  }
}
