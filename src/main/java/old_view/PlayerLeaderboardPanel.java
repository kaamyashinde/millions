package old_view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.session.PlayerLeaderboardEntry;
import model.session.PlayerLeaderboardMetric;
import model.session.PlayerLeaderboardRanking;
import old_view.components.table.AppTableView;

/**
 * Dedicated auth-screen leaderboard for comparing saved players.
 */
public class PlayerLeaderboardPanel extends BorderPane {

  private static final String PANEL_STYLE =
      "-fx-background-color: #f6f7fb;"
          + "-fx-border-color: #d7dce5;"
          + "-fx-background-radius: 12;"
          + "-fx-border-radius: 12;"
          + "-fx-padding: 14;";
  private static final String TOP_ONE_STYLE =
      "-fx-background-color: #fff4cc;"
          + "-fx-font-weight: bold;";
  private static final String TOP_TWO_STYLE =
      "-fx-background-color: #eef3f8;"
          + "-fx-font-weight: bold;";
  private static final String TOP_THREE_STYLE =
      "-fx-background-color: #fbe9dd;"
          + "-fx-font-weight: bold;";

  private final AppTableView<PlayerLeaderboardEntry> table =
      new AppTableView<>("No saved players to compare yet.");
  private final ObservableList<PlayerLeaderboardEntry> rows = FXCollections.observableArrayList();
  private final List<PlayerLeaderboardEntry> sourceEntries = new ArrayList<>();
  private final Map<String, Integer> rankByUsername = new HashMap<>();

  private final TableColumn<PlayerLeaderboardEntry, Integer> rankColumn = new TableColumn<>("Rank");
  private final TableColumn<PlayerLeaderboardEntry, String> playerColumn =
      AppTableView.createTextColumn("Player", PlayerLeaderboardEntry::username);
  private final TableColumn<PlayerLeaderboardEntry, BigDecimal> netWorthColumn =
      AppTableView.createNumericColumn(
          "Net worth",
          PlayerLeaderboardEntry::netWorth,
          PlayerLeaderboardPanel::formatCurrency);
  private final TableColumn<PlayerLeaderboardEntry, BigDecimal> returnColumn =
      AppTableView.createNumericColumn(
          "Return %",
          PlayerLeaderboardEntry::totalReturnPercent,
          PlayerLeaderboardPanel::formatPercent);

  private PlayerLeaderboardMetric activeMetric = PlayerLeaderboardMetric.NET_WORTH;
  private boolean ascending;

  /**
   * Creates a leaderboard panel from the supplied entries.
   *
   * @param entries saved-player leaderboard entries
   */
  public PlayerLeaderboardPanel(List<PlayerLeaderboardEntry> entries) {
    setStyle(PANEL_STYLE);
    setPadding(new Insets(0));
    Label heading = new Label("Player leaderboard");
    heading.setFont(Font.font("System", FontWeight.BOLD, 18));

    Label subheading = new Label("Compare saved profiles by net worth or total return.");
    subheading.setWrapText(true);

    VBox top = new VBox(6, heading, subheading);
    setTop(top);

    buildTable();
    table.setItems(rows);
    VBox.setVgrow(table, Priority.ALWAYS);
    setCenter(table);

    setEntries(entries);
    sortByNetWorthDescending();
  }

  /**
   * Replaces the leaderboard data source and reapplies the current sort.
   *
   * @param entries new leaderboard rows
   */
  public void setEntries(List<PlayerLeaderboardEntry> entries) {
    sourceEntries.clear();
    sourceEntries.addAll(entries);
    refreshRows();
  }

  /**
   * Sorts by net worth descending.
   */
  public void sortByNetWorthDescending() {
    applySort(netWorthColumn, SortType.DESCENDING);
  }

  /**
   * Sorts by net worth ascending.
   */
  public void sortByNetWorthAscending() {
    applySort(netWorthColumn, SortType.ASCENDING);
  }

  /**
   * Sorts by return percent descending.
   */
  public void sortByReturnDescending() {
    applySort(returnColumn, SortType.DESCENDING);
  }

  /**
   * Sorts by return percent ascending.
   */
  public void sortByReturnAscending() {
    applySort(returnColumn, SortType.ASCENDING);
  }

  /**
   * Returns the usernames in the currently displayed order.
   *
   * @return visible usernames
   */
  public List<String> getDisplayedUsernames() {
    return rows.stream().map(PlayerLeaderboardEntry::username).toList();
  }

  /**
   * Returns the visible rank values in row order.
   *
   * @return rank labels in display order
   */
  public List<Integer> getDisplayedRanks() {
    return rows.stream().map(entry -> rankByUsername.get(entry.username())).toList();
  }

  /**
   * Returns the usernames currently highlighted as the top three.
   *
   * @return highlighted usernames in best-first ranking order
   */
  public List<String> getHighlightedUsernames() {
    return sourceEntries.stream()
        .sorted(PlayerLeaderboardRanking.bestFirstComparator(activeMetric))
        .limit(3)
        .map(PlayerLeaderboardEntry::username)
        .toList();
  }

  /**
   * Returns how many rows are currently visible.
   *
   * @return displayed row count
   */
  public int getRowCount() {
    return rows.size();
  }

  /**
   * Returns the formatted net worth for a specific username, if present.
   *
   * @param username target username
   * @return formatted value or {@code null} if missing
   */
  public String getDisplayedNetWorthForUser(String username) {
    return sourceEntries.stream()
        .filter(entry -> entry.username().equals(username))
        .findFirst()
        .map(PlayerLeaderboardPanel::formatCurrency)
        .orElse(null);
  }

  private void buildTable() {
    rankColumn.setCellValueFactory(cell ->
        new ReadOnlyObjectWrapper<>(rankByUsername.getOrDefault(cell.getValue().username(), 0)));
    rankColumn.setSortable(false);
    playerColumn.setSortable(false);

    table.getColumns().setAll(List.of(rankColumn, playerColumn, netWorthColumn, returnColumn));
    table.setRowStyleProvider(this::rowStyleFor);
    table.setSortPolicy(_ -> {
      syncSortStateFromTable();
      refreshRows();
      return true;
    });
  }

  private void applySort(TableColumn<PlayerLeaderboardEntry, ?> column, SortType sortType) {
    column.setSortType(sortType);
    table.getSortOrder().setAll(column);
    table.sort();
  }

  private void syncSortStateFromTable() {
    TableColumn<PlayerLeaderboardEntry, ?> primaryColumn = table.getSortOrder().isEmpty()
        ? netWorthColumn
        : table.getSortOrder().getFirst();
    activeMetric = primaryColumn == returnColumn
        ? PlayerLeaderboardMetric.TOTAL_RETURN_PERCENT
        : PlayerLeaderboardMetric.NET_WORTH;
    SortType sortType = primaryColumn.getSortType() == null ? SortType.DESCENDING : primaryColumn.getSortType();
    ascending = sortType == SortType.ASCENDING;
  }

  private void refreshRows() {
    List<PlayerLeaderboardEntry> ranked = new ArrayList<>(sourceEntries);
    ranked.sort(PlayerLeaderboardRanking.bestFirstComparator(activeMetric));
    rankByUsername.clear();
    for (int index = 0; index < ranked.size(); index++) {
      rankByUsername.put(ranked.get(index).username(), index + 1);
    }

    List<PlayerLeaderboardEntry> displayOrder = new ArrayList<>(sourceEntries);
    displayOrder.sort(PlayerLeaderboardRanking.displayComparator(activeMetric, ascending));
    rows.setAll(displayOrder);
    table.refresh();
  }

  private String rowStyleFor(PlayerLeaderboardEntry entry) {
    int rank = rankByUsername.getOrDefault(entry.username(), Integer.MAX_VALUE);
    return switch (rank) {
      case 1 -> TOP_ONE_STYLE;
      case 2 -> TOP_TWO_STYLE;
      case 3 -> TOP_THREE_STYLE;
      default -> "";
    };
  }

  private static String formatCurrency(PlayerLeaderboardEntry entry) {
    return formatCurrency(entry.netWorth());
  }

  private static String formatCurrency(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private static String formatPercent(BigDecimal value) {
    return value.multiply(BigDecimal.valueOf(100))
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString() + "%";
  }
}
