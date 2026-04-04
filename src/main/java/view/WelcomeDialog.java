package view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import util.MarkdownLoader;

/**
 * Modal dialog that renders the welcome / help markdown in a themed {@link WebView}.
 */
public final class WelcomeDialog {

  private static final String WELCOME_RESOURCE = "welcome/welcome.md";
  private static final Parser MD_PARSER = Parser.builder().build();
  private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder().build();

  private WelcomeDialog() {
  }

  /**
   * Shows the welcome content modally until the user closes the window.
   *
   * @param owner optional owner window for modality
   */
  public static void show(Window owner) {
    String markdown = MarkdownLoader.load(WELCOME_RESOURCE);
    String bodyHtml = markdown.isEmpty()
        ? "<p style='color:#bdbdbd'>Welcome content could not be loaded.</p>"
        : HTML_RENDERER.render(MD_PARSER.parse(markdown));

    String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <style>
            body { font-family: system-ui, sans-serif; background: #121212; color: #e0e0e0;
                   margin: 16px 20px; line-height: 1.5; }
            h1 { color: #fff; font-size: 1.4rem; }
            h2 { color: #b0bec5; font-size: 1.1rem; margin-top: 1.2em; }
            a { color: #64b5f6; }
            code { background: #1e1e1e; padding: 2px 6px; border-radius: 4px; }
          </style>
        </head>
        <body>%s</body>
        </html>
        """.formatted(bodyHtml);

    WebView webView = new WebView();
    webView.getEngine().loadContent(html);

    ScrollPane scroll = new ScrollPane(webView);
    scroll.setFitToWidth(true);
    scroll.setPrefSize(520, 420);
    scroll.setStyle("-fx-background: #121212;");

    Button close = new Button("Close");
    close.setStyle(
        "-fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
    BorderPane root = new BorderPane();
    root.setCenter(scroll);
    root.setBottom(close);
    BorderPane.setMargin(close, new Insets(12, 16, 16, 16));
    root.setStyle("-fx-background-color: #121212;");

    Stage stage = new Stage();
    stage.setTitle("Welcome to us");
    stage.initModality(Modality.WINDOW_MODAL);
    if (owner != null) {
      stage.initOwner(owner);
    }
    stage.setScene(new Scene(root, 560, 480));
    close.setOnAction(_ -> stage.close());
    stage.showAndWait();
  }
}
