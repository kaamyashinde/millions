package view.components.learning;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.learning.content.LearningResource;
import util.ExternalLinkOpener;
import view.theme.ThemeStyles;

/**
 * Clickable card for a curated external {@link LearningResource}.
 */
public final class LearningResourceCard {

  private LearningResourceCard() {}

  /**
   * Builds a card that opens the resource URL in the system browser when clicked.
   *
   * @param resource curated external resource
   * @return clickable card node
   */
  public static VBox create(LearningResource resource) {
    Label sourceLabel = new Label(resource.sourceLabel());
    ThemeStyles.addStyleClasses(sourceLabel, "learning-source-badge");

    Label title = new Label(resource.title());
    ThemeStyles.addStyleClasses(title, "learning-resource-title");
    title.setWrapText(true);

    Label desc = new Label(resource.description());
    ThemeStyles.addStyleClasses(desc, "learning-card-summary");
    desc.setWrapText(true);

    Label cta = new Label("Open →");
    ThemeStyles.addStyleClasses(cta, "learning-resource-cta");

    VBox card = new VBox(6, sourceLabel, title, desc, cta);
    card.setPadding(new Insets(12));
    card.setMaxWidth(Double.MAX_VALUE);
    ThemeStyles.addStyleClasses(card, "learning-card-resource");
    card.setOnMouseClicked(unused -> ExternalLinkOpener.open(resource.url()));
    return card;
  }
}
