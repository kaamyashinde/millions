package view.dialogs;

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
import view.theme.ThemeStyles;

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
        ? "<p style='color:#94A3B8'>Welcome content could not be loaded.</p>"
        : HTML_RENDERER.render(MD_PARSER.parse(markdown));

    String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <style>
            body { font-family: system-ui, sans-serif; background: #0B1220; color: #F8FAFC;
                   margin: 16px 20px; line-height: 1.5; }
            h1 { color: #0EA5A4; font-size: 1.4rem; }
            h2 { color: #CBD5E1; font-size: 1.1rem; margin-top: 1.2em; }
            a { color: #0EA5A4; }
            code { background: #111827; padding: 2px 6px; border-radius: 4px; color: #F8FAFC; }
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

    Button close = new Button("Close");
    ThemeStyles.styleButton(close);
    BorderPane root = new BorderPane();
    ThemeStyles.addStyleClasses(root, "dialog-root");
    root.setCenter(scroll);
    root.setBottom(close);
    BorderPane.setMargin(close, new Insets(12, 16, 16, 16));

    Stage stage = new Stage();
    stage.setTitle("Welcome to us");
    stage.initModality(Modality.WINDOW_MODAL);
    if (owner != null) {
      stage.initOwner(owner);
    }
    Scene scene = new Scene(
        root,
        ThemeStyles.dialogDimension(owner, 0.50, 480, 640),
        ThemeStyles.dialogDimension(owner, 0.65, 400, 560));
    ThemeStyles.install(scene);
    stage.setScene(scene);
    close.setOnAction(_ -> stage.close());
    stage.showAndWait();
  }
}
