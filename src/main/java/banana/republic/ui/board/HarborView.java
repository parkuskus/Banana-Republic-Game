package banana.republic.ui.board;

import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class HarborView {

    private static final double[][] HEX_POINTS = {
            {0.5, 0.0}, {1.0, 0.25}, {1.0, 0.75},
            {0.5, 1.0}, {0.0, 0.75}, {0.0, 0.25}
    };

    public void renderHarbor(StackPane hexTile,
                             int firstCorner,
                             int secondCorner,
                             String label,
                             Image anchorImage,
                             List<double[]> harborPoints) {
        if (hexTile == null) return;
        Group parentMap = (Group) hexTile.getParent();
        if (parentMap == null) return;

        double width = hexTile.getPrefWidth() > 0 ? hexTile.getPrefWidth() : 94.0;
        double height = hexTile.getPrefHeight() > 0 ? hexTile.getPrefHeight() : 108.0;
        double[][] points = scaledHexPoints(width, height);

        addAnchor(parentMap, hexTile, points[firstCorner], anchorImage, harborPoints);
        addAnchor(parentMap, hexTile, points[secondCorner], anchorImage, harborPoints);
        addStore(parentMap, hexTile, points, firstCorner, label, anchorImage);
    }

    private void addAnchor(Group parentMap,
                           StackPane hexTile,
                           double[] localPoint,
                           Image anchorImage,
                           List<double[]> harborPoints) {
        double x = hexTile.getLayoutX() + localPoint[0];
        double y = hexTile.getLayoutY() + localPoint[1];
        harborPoints.add(new double[]{x, y});

        StackPane container = new StackPane();
        Circle anchorCircle = new Circle(12);
        anchorCircle.setFill(Color.WHITE);
        anchorCircle.setStroke(Color.BLACK);
        anchorCircle.setStrokeWidth(1);
        container.getChildren().add(anchorCircle);

        ImageView iconView = anchorIcon(anchorImage, 14);
        if (iconView != null) container.getChildren().add(iconView);

        container.setLayoutX(x - 12);
        container.setLayoutY(y - 12);
        parentMap.getChildren().add(container);
    }

    private void addStore(Group parentMap,
                          StackPane hexTile,
                          double[][] points,
                          int sideIndex,
                          String labelText,
                          Image anchorImage) {
        double[] start = points[sideIndex];
        double[] end = points[(sideIndex + 1) % 6];
        double width = hexTile.getPrefWidth() > 0 ? hexTile.getPrefWidth() : 94.0;
        double height = hexTile.getPrefHeight() > 0 ? hexTile.getPrefHeight() : 108.0;

        double midX = (start[0] + end[0]) / 2.0;
        double midY = (start[1] + end[1]) / 2.0;
        double dx = midX - (width / 2.0);
        double dy = midY - (height / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);
        double pushX = (dx / distance) * 50.0;
        double pushY = (dy / distance) * 50.0;

        double absoluteX = hexTile.getLayoutX() + midX + pushX;
        double absoluteY = hexTile.getLayoutY() + midY + pushY;
        double startX = hexTile.getLayoutX() + start[0];
        double startY = hexTile.getLayoutY() + start[1];
        double endX = hexTile.getLayoutX() + end[0];
        double endY = hexTile.getLayoutY() + end[1];

        Line dock1 = dockLine(startX, startY, absoluteX, absoluteY);
        Line dock2 = dockLine(endX, endY, absoluteX, absoluteY);
        HBox store = storeLabel(labelText, anchorImage);
        store.setLayoutX(absoluteX);
        store.setLayoutY(absoluteY);
        store.translateXProperty().bind(store.widthProperty().divide(-2));
        store.translateYProperty().bind(store.heightProperty().divide(-2));
        parentMap.getChildren().addAll(dock1, dock2, store);
    }

    private double[][] scaledHexPoints(double width, double height) {
        double[][] points = new double[HEX_POINTS.length][2];
        for (int i = 0; i < HEX_POINTS.length; i++) {
            points[i][0] = HEX_POINTS[i][0] * width;
            points[i][1] = HEX_POINTS[i][1] * height;
        }
        return points;
    }

    private Line dockLine(double startX, double startY, double endX, double endY) {
        Line dock = new Line(startX, startY, endX, endY);
        dock.setStroke(Color.rgb(160, 100, 50, 0.8));
        dock.setStrokeWidth(4.0);
        return dock;
    }

    private HBox storeLabel(String labelText, Image anchorImage) {
        HBox box = new HBox(3);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: black; -fx-border-radius: 12; -fx-padding: 2 5 2 5;");
        box.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-family: 'Russo One'; -fx-font-size: 10px;");
        box.getChildren().add(label);

        ImageView iconView = anchorIcon(anchorImage, 12);
        if (iconView != null) box.getChildren().add(iconView);
        return box;
    }

    private ImageView anchorIcon(Image anchorImage, double size) {
        if (anchorImage == null) return null;
        ImageView iconView = new ImageView(anchorImage);
        iconView.setFitWidth(size);
        iconView.setFitHeight(size);
        iconView.setPreserveRatio(true);
        return iconView;
    }
}
