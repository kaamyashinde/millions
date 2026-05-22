package view.pages.leaderboard;

import java.math.RoundingMode;
import java.util.List;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.session.LocalLeaderboardService.LeaderboardRow;
import model.session.SessionService;
import view.components.image.FileImageLoader;
import view.components.image.ImageLoader;
import view.components.image.ValidatingImageLoader;

/**
 * Local leaderboard ranked by net worth with avatars.
 */
public class LeaderboardPage extends BorderPane {

  private final SessionService sessionService;
  private final ImageLoader avatarLoader = new ValidatingImageLoader(new FileImageLoader());
  private final TableView<LeaderboardRow> table = new TableView<>();
  private final ObservableList<LeaderboardRow> rows = FXCollections.observableArrayList();

  public LeaderboardPage(SessionService sessionService) {
    this.sessionService = sessionService;
    setPadding(new Insets(16));

    Text heading = new Text("Leaderboard");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));

    BorderPane.setAlignment(heading, Pos.CENTER_LEFT);
    setTop(heading);

    TableColumn<LeaderboardRow, Integer> rankCol = new TableColumn<>("Rank");
    rankCol.setPrefWidth(56);
    rankCol.setCellValueFactory(c -> {
      int i = rows.indexOf(c.getValue());
      return new ReadOnlyObjectWrapper<>(i >= 0 ? i + 1 : 0);
    });

    TableColumn<LeaderboardRow, LeaderboardRow> avatarCol = new TableColumn<>(" ");
    avatarCol.setPrefWidth(56);
    avatarCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
    avatarCol.setCellFactory(col -> new TableCell<>() {
      private final ImageView imageView = new ImageView();

      {
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);
        setGraphic(imageView);
      }

      @Override
      protected void updateItem(LeaderboardRow row, boolean empty) {
        super.updateItem(row, empty);
        if (empty || row == null) {
          imageView.setImage(null);
          return;
        }
        if (!row.hasAvatar()) {
          imageView.setImage(null);
          return;
        }
        var path = sessionService.avatarPath(row.normalizedUsername());
        imageView.setImage(avatarLoader.load(path, 40));
      }
    });

    TableColumn<LeaderboardRow, String> nameCol = new TableColumn<>("Player");
    nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().displayName()));
    nameCol.setPrefWidth(200);

    TableColumn<LeaderboardRow, String> worthCol = new TableColumn<>("Net worth");
    worthCol.setCellValueFactory(
        c -> new SimpleStringProperty(
            c.getValue().netWorth().setScale(2, RoundingMode.HALF_UP).toPlainString()));
    worthCol.setPrefWidth(140);

    table.getColumns().setAll(List.of(rankCol, avatarCol, nameCol, worthCol));
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
