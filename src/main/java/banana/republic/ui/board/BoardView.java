package banana.republic.ui.board;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.building.BuildingType;
import banana.republic.core.Game;
import banana.republic.player.PlayerColor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

public class BoardView {

    private static final double ROBBER_SIZE = 36.0;
    private static final double ROBBER_NORTH_OFFSET_RATIO = 0.24;

    public Line roadLine(IntersectionCoordinate start, IntersectionCoordinate end, double width, Color color) {
        Line line = new Line(start.x(), start.y(), end.x(), end.y());
        line.setStrokeWidth(width);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStroke(color);
        return line;
    }

    public Circle settlement(IntersectionCoordinate coordinate, PlayerColor color) {
        Circle circle = new Circle(coordinate.x(), coordinate.y(), 16, Color.web(playerColorHex(color)));
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        return circle;
    }

    public Rectangle laboratory(IntersectionCoordinate coordinate, PlayerColor color) {
        Rectangle rect = new Rectangle(coordinate.x() - 14, coordinate.y() - 14, 28, 28);
        rect.setFill(Color.web(playerColorHex(color)));
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2.5);
        return rect;
    }

    public String playerColorHex(PlayerColor color) {
        if (color == null) return "#888888";
        return switch (color) {
            case RED -> "#c21a09";
            case BLUE -> "#305cde";
            case ORANGE -> "#ff7f00";
            case GREEN -> "#4fc978";
            default -> "#888888";
        };
    }

    public void renderPermanentBuildLayer(Game game,
                                          Pane layer,
                                          Map<Intersection, double[]> intersectionCoordinates,
                                          Map<StackPane, HexTile> visualToModelTile,
                                          Image robberImage) {
        if (layer == null || game == null) return;
        layer.getChildren().clear();
        renderRoads(layer, intersectionCoordinates);
        renderBuildings(layer, intersectionCoordinates);
        renderRobber(game, layer, visualToModelTile, robberImage);
        layer.toFront();
    }

    private void renderRoads(Pane layer, Map<Intersection, double[]> intersectionCoordinates) {
        Set<Path> allPaths = new HashSet<>();
        for (Intersection intersection : intersectionCoordinates.keySet()) {
            if (intersection.getAdjacentPaths() != null) allPaths.addAll(intersection.getAdjacentPaths());
        }

        for (Path path : allPaths) {
            if (path.getOwner() == null) continue;
            double[] a = intersectionCoordinates.get(path.getIntersectionA());
            double[] b = intersectionCoordinates.get(path.getIntersectionB());
            if (a == null || b == null) continue;
            Line road = roadLine(
                    new IntersectionCoordinate(a[0], a[1]),
                    new IntersectionCoordinate(b[0], b[1]),
                    12.0,
                    Color.web(playerColorHex(path.getOwner().getColor())));
            layer.getChildren().add(road);
        }
    }

    private void renderBuildings(Pane layer, Map<Intersection, double[]> intersectionCoordinates) {
        for (Map.Entry<Intersection, double[]> entry : intersectionCoordinates.entrySet()) {
            Intersection intersection = entry.getKey();
            if (intersection.getOwner() == null) continue;
            double[] coordinate = entry.getValue();
            IntersectionCoordinate point = new IntersectionCoordinate(coordinate[0], coordinate[1]);
            if (intersection.getBuilding().getBuildingType() == BuildingType.LABORATORIUM) {
                layer.getChildren().add(laboratory(point, intersection.getOwner().getColor()));
            } else {
                layer.getChildren().add(settlement(point, intersection.getOwner().getColor()));
            }
        }
    }

    private void renderRobber(Game game,
                              Pane layer,
                              Map<StackPane, HexTile> visualToModelTile,
                              Image robberImage) {
        if (robberImage == null || game.getRobber() == null || game.getRobber().getCurrentTile() == null) return;
        StackPane visualTile = findVisualTile(visualToModelTile, game.getRobber().getCurrentTile());
        if (visualTile == null) return;

        ImageView robberView = new ImageView(robberImage);
        robberView.setFitWidth(ROBBER_SIZE);
        robberView.setFitHeight(ROBBER_SIZE);
        robberView.setPreserveRatio(true);

        double w = visualTile.getPrefWidth() > 0 ? visualTile.getPrefWidth() : 94.0;
        double h = visualTile.getPrefHeight() > 0 ? visualTile.getPrefHeight() : 108.0;
        robberView.setLayoutX(visualTile.getLayoutX() + (w / 2) - (ROBBER_SIZE / 2));
        robberView.setLayoutY(visualTile.getLayoutY() + (h * ROBBER_NORTH_OFFSET_RATIO) - (ROBBER_SIZE / 2));
        robberView.setEffect(new DropShadow(10, Color.PURPLE));
        layer.getChildren().add(robberView);
    }

    private StackPane findVisualTile(Map<StackPane, HexTile> visualToModelTile, HexTile tile) {
        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            if (entry.getValue().equals(tile)) return entry.getKey();
        }
        return null;
    }
}
