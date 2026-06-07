package banana.republic.ui.board;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class BuildOverlayView {

    private final BoardView boardView;

    public BuildOverlayView(BoardView boardView) {
        this.boardView = boardView;
    }

    public Line roadOption(IntersectionCoordinate start,
                           IntersectionCoordinate end,
                           Color normalColor,
                           Color hoverColor,
                           Runnable action) {
        Line line = boardView.roadLine(start, end, 18.0, normalColor);
        line.setStyle("-fx-cursor: hand;");
        DropShadow glow = new DropShadow(12, hoverColor);
        line.setOnMouseEntered(event -> {
            line.setEffect(glow);
            line.setStroke(hoverColor);
            line.setStrokeWidth(22.0);
        });
        line.setOnMouseExited(event -> {
            line.setEffect(null);
            line.setStroke(normalColor);
            line.setStrokeWidth(18.0);
        });
        line.setOnMouseClicked(event -> {
            action.run();
            event.consume();
        });
        return line;
    }

    public StackPane settlementOption(double x,
                                      double y,
                                      boolean harbor,
                                      Image anchorImage,
                                      Color hoverColor,
                                      Runnable action) {
        StackPane container = optionContainer(x, y);
        Circle dot = new Circle(14);
        if (harbor) {
            dot.setFill(Color.rgb(255, 255, 255, 0.4));
            dot.setStroke(Color.rgb(40, 40, 40, 0.8));
            dot.setStrokeWidth(2.5);
        } else {
            dot.setFill(Color.rgb(255, 255, 255, 0.85));
            dot.setStroke(Color.rgb(40, 40, 40, 0.8));
            dot.setStrokeWidth(1.5);
        }
        container.getChildren().add(dot);
        if (harbor && anchorImage != null) {
            ImageView anchorView = new ImageView(anchorImage);
            anchorView.setFitWidth(16);
            anchorView.setFitHeight(16);
            anchorView.setPreserveRatio(true);
            container.getChildren().add(anchorView);
        }
        DropShadow glow = new DropShadow(15, hoverColor);
        container.setOnMouseEntered(event -> dot.setEffect(glow));
        container.setOnMouseExited(event -> dot.setEffect(null));
        container.setOnMouseClicked(event -> {
            action.run();
            event.consume();
        });
        return container;
    }

    public StackPane laboratoryOption(double x, double y, Color playerColor, Runnable action) {
        StackPane container = optionContainer(x, y);
        Rectangle labRect = new Rectangle(24, 24);
        labRect.setFill(playerColor.deriveColor(0, 0.8, 1.2, 0.7));
        labRect.setStroke(Color.WHITE);
        labRect.setStrokeWidth(2);
        container.getChildren().add(labRect);
        container.setOnMouseEntered(event -> labRect.setEffect(new DropShadow(15, Color.CYAN)));
        container.setOnMouseExited(event -> labRect.setEffect(null));
        container.setOnMouseClicked(event -> {
            action.run();
            event.consume();
        });
        return container;
    }

    private StackPane optionContainer(double x, double y) {
        StackPane container = new StackPane();
        container.setLayoutX(x - 18);
        container.setLayoutY(y - 18);
        container.setPrefSize(36, 36);
        container.setStyle("-fx-cursor: hand;");
        return container;
    }
}
