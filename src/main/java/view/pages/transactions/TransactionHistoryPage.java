package view.pages.transactions;

import static util.Validator.checkNotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.core.market.Exchange;
import model.core.player.Player;
import model.trading.transaction.Purchase;
import model.trading.transaction.Transaction;
import view.components.table.AppTableView;
import view.theme.ThemeStyles;

/**
 * Read-only list of all buys and sells for the current playthrough.
 */
public class TransactionHistoryPage extends BorderPane {

  private final Exchange exchange;
  private final Player player;
  private final AppTableView<TransactionRow> table =
      new AppTableView<>("No transactions yet.");

  public TransactionHistoryPage(Exchange exchange, Player player) {
    checkNotNull(exchange, "Exchange");
    checkNotNull(player, "Player");
    this.exchange = exchange;
    this.player = player;

    setPadding(new Insets(16));
    ThemeStyles.addStyleClasses(this, "finance-page", "finance-panel");

    Text heading = new Text("Transaction History");
    heading.setFont(Font.font("System", FontWeight.BOLD, 22));
    ThemeStyles.addStyleClasses(heading, "finance-page-title");

    Label hint = new Label(
        "All buys and sells for this playthrough, up to the current trading day.");
    hint.setWrapText(true);
    ThemeStyles.addStyleClasses(hint, "finance-meta");

    TableColumn<TransactionRow, Integer> dayCol =
        AppTableView.createNumericColumn(
            "Day",
            TransactionRow::day,
            Object::toString);
    dayCol.setPrefWidth(56);

    TableColumn<TransactionRow, String> typeCol =
        AppTableView.createTextColumn("Type", TransactionRow::type);
    typeCol.setPrefWidth(90);

    TableColumn<TransactionRow, String> symbolCol =
        AppTableView.createTextColumn("Symbol", TransactionRow::symbol);
    symbolCol.setPrefWidth(80);

    TableColumn<TransactionRow, BigDecimal> quantityCol =
        AppTableView.createNumericColumn(
            "Quantity",
            TransactionRow::quantity,
            TransactionHistoryPage::formatDecimal);
    quantityCol.setPrefWidth(100);

    TableColumn<TransactionRow, BigDecimal> priceCol =
        AppTableView.createNumericColumn(
            "Price",
            TransactionRow::price,
            TransactionHistoryPage::formatDecimal);
    priceCol.setPrefWidth(100);

    TableColumn<TransactionRow, BigDecimal> totalCol =
        AppTableView.createNumericColumn(
            "Total",
            TransactionRow::total,
            TransactionHistoryPage::formatDecimal);
    totalCol.setPrefWidth(110);

    table.getColumns().setAll(List.of(dayCol, typeCol, symbolCol, quantityCol, priceCol, totalCol));
    table.setPrefHeight(400);

    VBox top = new VBox(8, heading, hint);
    top.setAlignment(Pos.CENTER_LEFT);
    setTop(top);
    setCenter(table);

    refresh();
  }

  /**
   * Reloads transaction rows from the player's archive.
   */
  public void refresh() {
    List<Transaction> transactions =
        player.getTransactionArchive().getTransactions(exchange.getDay());
    List<TransactionRow> rows = new ArrayList<>(transactions.size());
    for (int i = transactions.size() - 1; i >= 0; i--) {
      rows.add(TransactionRow.from(transactions.get(i)));
    }
    table.getItems().setAll(rows);
  }

  private static String formatDecimal(BigDecimal value) {
    if (value == null) {
      return "—";
    }
    return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
  }

  private record TransactionRow(
      int day,
      String type,
      String symbol,
      BigDecimal quantity,
      BigDecimal price,
      BigDecimal total) {

    static TransactionRow from(Transaction transaction) {
      String type = transaction instanceof Purchase ? "Purchase" : "Sale";
      return new TransactionRow(
          transaction.getDay(),
          type,
          transaction.getShare().getAsset().getSymbol(),
          transaction.getShare().getQuantity(),
          transaction.getShare().getPurchasePrice(),
          transaction.getCalculator().calculateTotal());
    }
  }
}
