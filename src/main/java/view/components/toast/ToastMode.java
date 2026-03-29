package view.components.toast;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Enum representing the different modes (severity levels) of a toast notification. Each mode
 * defines its own color, symbol, and icon shape.
 *
 * @author kaamyashinde
 * @version 1.0.0
 * @since 2026-03-29
 */
public enum ToastMode {
  ERROR("#FF4444", "i") {
    @Override
    public Shape createShape(Color color) {
      double r = ICON_SIZE / 2.0;
      Polygon hex = new Polygon();
      for (int i = 0; i < 6; i++) {
        double angle = Math.toRadians(-30 + 60 * i);
        hex.getPoints().addAll(r * Math.cos(angle), r * Math.sin(angle));
      }
      hex.setFill(Color.TRANSPARENT);
      hex.setStroke(color);
      hex.setStrokeWidth(STROKE);
      return hex;
    }
  },

  WARNING("#FFA500", "i") {
    @Override
    public Shape createShape(Color color) {
      double h = ICON_SIZE / 2.0;
      Polygon triangle = new Polygon(
          0.0, -h,
          h, h,
          -h, h
      );
      triangle.setFill(Color.TRANSPARENT);
      triangle.setStroke(color);
      triangle.setStrokeWidth(STROKE);
      return triangle;
    }
  },

  INFO("#2196F3", "i") {
    @Override
    public Shape createShape(Color color) {
      Rectangle rect = new Rectangle(ICON_SIZE, ICON_SIZE);
      rect.setArcWidth(6);
      rect.setArcHeight(6);
      rect.setFill(Color.TRANSPARENT);
      rect.setStroke(color);
      rect.setStrokeWidth(STROKE);
      return rect;
    }
  },

  SUCCESS("#4CAF50", "\u2713") {
    @Override
    public Shape createShape(Color color) {
      Circle circle = new Circle(ICON_SIZE / 2.0);
      circle.setFill(Color.TRANSPARENT);
      circle.setStroke(color);
      circle.setStrokeWidth(STROKE);
      return circle;
    }
  };

  static final double ICON_SIZE = 40;
  static final double STROKE = 2;

  private final String colorHex;
  private final String symbol;

  ToastMode(String colorHex, String symbol) {
    this.colorHex = colorHex;
    this.symbol = symbol;
  }

  /**
   * Builds and returns the outline shape for this mode, styled with the given color.
   *
   * @param color the color to apply to the shape's stroke
   * @return the styled shape
   */
  public abstract Shape createShape(Color color);

  public String getColorHex() {
    return colorHex;
  }

  public String getSymbol() {
    return symbol;
  }
}
