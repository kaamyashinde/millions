package old_view.components.table;

import java.util.Objects;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Small reusable table wrapper for consistent placeholder, sizing, and row styling.
 *
 * @param <T> row type
 */
public class AppTableView<T> extends TableView<T> {

  private final Label placeholderLabel = new Label();
  private Function<T, String> rowStyleProvider = item -> "";

  /**
   * Creates a table with the supplied empty-state placeholder.
   *
   * @param placeholderText text shown when the table has no rows
   */
  public AppTableView(String placeholderText) {
    placeholderLabel.setText(placeholderText);
    setPlaceholder(placeholderLabel);
    setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    setRowFactory(_ -> new TableRow<>() {
      @Override
      protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setStyle("");
          return;
        }
        setStyle(rowStyleProvider.apply(item));
      }
    });
  }

  /**
   * Sets the empty-state text.
   *
   * @param placeholderText placeholder text
   */
  public void setPlaceholderText(String placeholderText) {
    placeholderLabel.setText(placeholderText);
  }

  /**
   * Supplies inline row styles for the current item.
   *
   * @param rowStyleProvider function returning a style string
   */
  public void setRowStyleProvider(Function<T, String> rowStyleProvider) {
    this.rowStyleProvider = Objects.requireNonNullElse(rowStyleProvider, item -> "");
    refresh();
  }

  /**
   * Creates a left-aligned text column.
   *
   * @param title column title
   * @param extractor value extractor
   * @param <T> row type
   * @return configured table column
   */
  public static <T> TableColumn<T, String> createTextColumn(
      String title,
      Function<T, String> extractor) {
    TableColumn<T, String> column = new TableColumn<>(title);
    column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(extractor.apply(cell.getValue())));
    return column;
  }

  /**
   * Creates a numeric column with custom formatting and natural-value sorting.
   *
   * @param title column title
   * @param extractor raw numeric value extractor
   * @param formatter cell text formatter
   * @param <T> row type
   * @param <N> comparable numeric type
   * @return configured numeric table column
   */
  public static <T, N extends Comparable<? super N>> TableColumn<T, N> createNumericColumn(
      String title,
      Function<T, N> extractor,
      Function<N, String> formatter) {
    TableColumn<T, N> column = new TableColumn<>(title);
    column.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(extractor.apply(cell.getValue())));
    column.setComparator((left, right) -> {
      if (left == right) {
        return 0;
      }
      if (left == null) {
        return -1;
      }
      if (right == null) {
        return 1;
      }
      return left.compareTo(right);
    });
    column.setCellFactory(_ -> new TableCell<>() {
      @Override
      protected void updateItem(N item, boolean empty) {
        super.updateItem(item, empty);
        setAlignment(Pos.CENTER_RIGHT);
        setText(empty || item == null ? null : formatter.apply(item));
      }
    });
    return column;
  }
}
