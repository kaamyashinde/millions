package view.layout;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import view.theme.ThemePalette;
import view.theme.ThemeStyles;

/**
 * Themed modal shell with a content area and a bottom action row.
 */
public class DialogLayout extends BorderPane {

  /**
   * Builds a dialog layout with the given content and primary close action.
   *
   * @param content center content node
   * @param closeLabel label for the close button
   * @param onClose invoked when the close button is pressed
   */
  public DialogLayout(Node content, String closeLabel, Runnable onClose) {
    setStyle("-fx-background-color: " + ThemePalette.BACKGROUND + ";");
    setPadding(new Insets(16));
    setCenter(content);

    Button close = new Button(closeLabel);
    ThemeStyles.styleButton(close);
    close.setOnAction(_ -> onClose.run());

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    HBox bottom = new HBox(12, spacer, close);
    bottom.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
    bottom.setPadding(new Insets(12, 0, 0, 0));
    setBottom(bottom);
  }
}
