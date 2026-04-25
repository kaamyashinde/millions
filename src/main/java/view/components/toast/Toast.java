package view.components.toast;

import static model.utils.Validator.checkNotNull;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A reusable JavaFX toast notification component. Supports four severity levels (ERROR, WARNING,
 * INFO, SUCCESS) each with a distinct icon shape, a mandatory title, and optional description and
 * action button.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 */
public class Toast extends HBox {

  private static final double ICON_SIZE = 20;
  private static final double TOAST_WIDTH = 360;
  private static final double MIN_TOAST_HEIGHT = 64;
  private static final double SPACING = 12;
  private static final double PADDING = 12;
  private static final double SYMBOL_FONT_SIZE = 10;

  /**
   * Creates a toast with title only.
   *
   * @param mode  the severity mode
   * @param title the title text
   */
  public Toast(ToastMode mode, String title) {
    this(mode, title, null, null, null);
  }

  /**
   * Creates a toast with title and description.
   *
   * @param mode        the severity mode
   * @param title       the title text
   * @param description the optional description text
   */
  public Toast(ToastMode mode, String title, String description) {
    this(mode, title, description, null, null);
  }

  /**
   * Creates a toast with all fields.
   *
   * @param mode        the severity mode (required)
   * @param title       the title text (required)
   * @param description optional description text
   * @param actionLabel optional label for the action button
   * @param onAction    optional callback for the action button
   */
  public Toast(ToastMode mode, String title, String description, String actionLabel,
      Runnable onAction) {
    checkNotNull(mode, "Mode");
    checkNotNull(title, "Title");

    setAlignment(Pos.CENTER_LEFT);
    setSpacing(SPACING);
    setPadding(new Insets(PADDING));
    setMinWidth(TOAST_WIDTH);
    setPrefWidth(TOAST_WIDTH);
    setMaxWidth(TOAST_WIDTH);
    setMinHeight(MIN_TOAST_HEIGHT);

    StackPane iconPane = buildIconPane(mode);
    VBox textBox = new VBox(4);
    textBox.setAlignment(Pos.CENTER_LEFT);
    textBox.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(textBox, Priority.ALWAYS);

    Label titleLabel = new Label(title);
    titleLabel.setWrapText(true);
    if (description != null) {
      Label descLabel = new Label(description);
      descLabel.setWrapText(true);
      boolean _ = textBox.getChildren().addAll(titleLabel, descLabel);
    } else {
      boolean _ = textBox.getChildren().addAll(titleLabel);
    }

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    getChildren().addAll(iconPane, textBox, spacer);

    if (actionLabel != null) {
      Button actionButton = new Button(actionLabel);
      if (onAction != null) {
        actionButton.setOnAction(_ -> onAction.run());
      }
      getChildren().add(actionButton);
    }
  }

  /**
   * Builds the icon pane for the toast by combining the mode-specific outline and symbol.
   *
   * @param mode the toast mode that defines the icon shape and symbol
   * @return the configured icon pane
   */
  private StackPane buildIconPane(ToastMode mode) {
    StackPane pane = new StackPane();
    pane.setPrefSize(ICON_SIZE, ICON_SIZE);

    Shape outline = mode.createShape();
    Text symbol = new Text(mode.getSymbol());
    symbol.setFont(Font.font(SYMBOL_FONT_SIZE));
    boolean _ = pane.getChildren().addAll(outline, symbol);

    return pane;
  }
}
