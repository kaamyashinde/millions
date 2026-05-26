package view.pages.notifications;

import static util.Validator.checkNotNull;

import java.util.List;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import view.components.notification.NotificationItem;
import view.components.notification.NotificationService;
import view.components.table.AppTableView;
import view.theme.ThemeStyles;

/**
 * Read-only view of all notifications shown during the current workspace session.
 */
public class NotificationHistoryPage extends BorderPane {

  private static final String EMPTY_STATE = "No notifications yet.";

  private final NotificationService notifications;
  private final AppTableView<NotificationItem> table =
      new AppTableView<>(EMPTY_STATE);
  private final Button clearButton = new Button("Clear notifications");

  /**
   * Creates a notification history page backed by the session notification service.
   *
   * @param notifications session-scoped notification service
   */
  public NotificationHistoryPage(NotificationService notifications) {
    checkNotNull(notifications, "Notifications");
    this.notifications = notifications;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page", "finance-panel");

    buildHeader();
    buildTable();
  }

  /**
   * Returns the rows currently displayed by the history table.
   *
   * @return immutable snapshot of displayed notification items
   */
  public List<NotificationItem> getDisplayedNotifications() {
    return List.copyOf(table.getItems());
  }

  /**
   * Returns the empty-state text shown when no notifications are available.
   *
   * @return empty-state table text
   */
  public String getEmptyStateText() {
    return EMPTY_STATE;
  }

  private void buildHeader() {
    Text heading = new Text("Notifications");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    Label hint =
        new Label("Notifications from this session. This list is cleared when you log out.");
    hint.setWrapText(true);
    ThemeStyles.addStyleClasses(hint, "finance-meta");

    clearButton.setId("clear-notifications-button");
    ThemeStyles.styleButton(clearButton);
    clearButton.setOnAction(unused -> notifications.clearHistory());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox headingRow = new HBox(12, heading, spacer, clearButton);
    headingRow.setAlignment(Pos.CENTER_LEFT);

    VBox top = new VBox(8, headingRow, hint);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);
  }

  private void buildTable() {
    TableColumn<NotificationItem, String> modeCol = createColumn(
        "Type",
        item -> item.mode().name());
    modeCol.setPrefWidth(110);

    TableColumn<NotificationItem, String> titleCol = createColumn(
        "Title",
        NotificationItem::title);
    titleCol.setPrefWidth(180);

    TableColumn<NotificationItem, String> descriptionCol = createColumn(
        "Description",
        item -> item.description() == null ? "" : item.description());
    descriptionCol.setPrefWidth(360);

    table.getColumns().setAll(List.of(modeCol, titleCol, descriptionCol));
    table.setItems(notifications.getHistoryItems());
    table.setPrefHeight(400);
    setCenter(table);
  }

  private static TableColumn<NotificationItem, String> createColumn(
      String title,
      Function<NotificationItem, String> extractor) {
    TableColumn<NotificationItem, String> column = new TableColumn<>(title);
    column.setCellValueFactory(
        cell -> new SimpleStringProperty(extractor.apply(cell.getValue())));
    return column;
  }
}
