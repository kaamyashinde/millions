package view.pages.leaderboard;

import java.math.RoundingMode;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import model.session.leaderboard.LocalLeaderboardService.LeaderboardRow;
import model.session.SessionService;

/**
 * Local leaderboard ranked by net worth with player levels.
 */
public class LeaderboardPage extends BorderPane {

  private final SessionService sessionService;
  private final TableView<LeaderboardRow> table = new TableView<>();
  private final ObservableList<LeaderboardRow> rows = FXCollections.observableArrayList();

  /**
   * Creates a leaderboard page backed by the session service.
   *
   * @param sessionService session service that loads local leaderboard rows
   */
  public LeaderboardPage(SessionService sessionService) {
    this.sessionService = sessionService;
    setPadding(new Insets(16));

    TableColumn<LeaderboardRow, Integer> rankCol = new TableColumn<>("Rank");
    rankCol.setPrefWidth(56);
    rankCol.setCellValueFactory(c -> {
      int i = rows.indexOf(c.getValue());
      return new ReadOnlyObjectWrapper<>(i >= 0 ? i + 1 : 0);
    });

    TableColumn<LeaderboardRow, String> levelCol = new TableColumn<>("Level");
    levelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().level()));
    levelCol.setPrefWidth(100);

    TableColumn<LeaderboardRow, String> nameCol = new TableColumn<>("Player");
    nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
    nameCol.setPrefWidth(200);

    TableColumn<LeaderboardRow, String> worthCol = new TableColumn<>("Net Worth");
    worthCol.setCellValueFactory(
        c -> new SimpleStringProperty(
            c.getValue().netWorth().setScale(2, RoundingMode.HALF_UP).toPlainString()));
    worthCol.setPrefWidth(140);

    table.getColumns().setAll(List.of(rankCol, levelCol, nameCol, worthCol));
    table.setItems(rows);
    table.setPlaceholder(new Label("No saved profiles yet."));
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    setCenter(table);

    refresh();
  }

  /**
   * Reloads rankings from disk.
   */
  public void refresh() {
    rows.setAll(sessionService.leaderboardService().loadRows());
    table.refresh();
  }
}
