package banana.republic.ui.board;

import java.util.Map;
import java.util.function.Consumer;

import banana.republic.board.HexTile;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class RobberOverlayView {

    private static final double MARKER_NORTH_OFFSET_RATIO = 0.24;

    public Pane createOverlay(Map<StackPane, HexTile> visualToModelTile,
                              HexTile currentRobberTile,
                              Consumer<StackPane> clickHandler) {
        Pane overlay = new Pane();
        overlay.setPickOnBounds(false);

        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            StackPane visualTile = entry.getKey();
            HexTile tile = entry.getValue();
            Circle marker = createMarker(visualTile, tile, currentRobberTile, clickHandler);
            overlay.getChildren().add(marker);
        }
        return overlay;
    }

    private Circle createMarker(StackPane visualTile,
                                HexTile tile,
                                HexTile currentRobberTile,
                                Consumer<StackPane> clickHandler) {
        double width = visualTile.getPrefWidth() > 0 ? visualTile.getPrefWidth() : 94.0;
        double height = visualTile.getPrefHeight() > 0 ? visualTile.getPrefHeight() : 108.0;
        double x = visualTile.getLayoutX() + width / 2;
        double y = visualTile.getLayoutY() + height * MARKER_NORTH_OFFSET_RATIO;

        Circle marker = new Circle(x, y, 20);
        if (tile.equals(currentRobberTile)) {
            marker.setFill(Color.rgb(80, 0, 80, 0.25));
            marker.setStroke(Color.rgb(160, 60, 160, 0.4));
            marker.setStrokeWidth(1.5);
            marker.setStyle("-fx-cursor: default;");
            marker.setOnMouseClicked(event -> event.consume());
            return marker;
        }

        marker.setFill(Color.rgb(128, 0, 128, 0.45));
        marker.setStroke(Color.rgb(210, 100, 210, 0.85));
        marker.setStrokeWidth(2.0);
        marker.setStyle("-fx-cursor: hand;");
        DropShadow glow = new DropShadow(14, Color.PURPLE);
        marker.setOnMouseEntered(event -> {
            marker.setFill(Color.rgb(200, 0, 220, 0.70));
            marker.setEffect(glow);
            marker.setRadius(23);
        });
        marker.setOnMouseExited(event -> {
            marker.setFill(Color.rgb(128, 0, 128, 0.45));
            marker.setEffect(null);
            marker.setRadius(20);
        });
        marker.setOnMouseClicked(event -> {
            clickHandler.accept(visualTile);
            event.consume();
        });
        return marker;
    }
}
