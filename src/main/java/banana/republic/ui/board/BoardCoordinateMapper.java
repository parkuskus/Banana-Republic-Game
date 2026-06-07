package banana.republic.ui.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.TerrainType;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class BoardCoordinateMapper {

    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final int[] BACKEND_TO_UI_CORNER = {1, 2, 3, 4, 5, 0};

    public Mapping map(Board board, Group parentMap) {
        Map<StackPane, HexTile> visualToModel = new HashMap<>();
        Map<Intersection, double[]> intersectionCoordinates = new HashMap<>();
        List<StackPane> mainHexes = collectMainHexes(parentMap);

        Map<HexTile, StackPane> modelToVisual = mapVisualTiles(board, mainHexes, visualToModel);
        mapIntersections(board, modelToVisual, intersectionCoordinates);
        return new Mapping(visualToModel, intersectionCoordinates, mainHexes);
    }

    public HexTile findHexTileByVisualFallback(Board board, StackPane visual, Set<HexTile> usedTiles) {
        TerrainType terrain = null;
        for (String style : visual.getStyleClass()) {
            terrain = parseTerrainFromStyle(style);
            if (terrain != null) break;
        }
        return findMatchingTile(board, terrain, parseTokenFromVisual(visual), usedTiles);
    }

    public double[][] getHexCorners(StackPane sp) {
        double w = sp.getPrefWidth() > 0 ? sp.getPrefWidth() : 94.0;
        double h = sp.getPrefHeight() > 0 ? sp.getPrefHeight() : 108.0;
        double x = sp.getLayoutX();
        double y = sp.getLayoutY();
        return new double[][] {
                {x + w / 2, y}, {x + w, y + h * 0.25}, {x + w, y + h * 0.75},
                {x + w / 2, y + h}, {x, y + h * 0.75}, {x, y + h * 0.25}
        };
    }

    private List<StackPane> collectMainHexes(Group parentMap) {
        List<StackPane> mainHexes = new ArrayList<>();
        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (node instanceof StackPane sp
                    && sp.getStyleClass().stream().anyMatch(s -> s.startsWith("hex-tile-"))) {
                mainHexes.add(sp);
            }
        }
        mainHexes.sort((a, b) -> {
            double ay = a.getLayoutY();
            double by = b.getLayoutY();
            if (Math.abs(ay - by) > 10) return Double.compare(ay, by);
            return Double.compare(a.getLayoutX(), b.getLayoutX());
        });
        return mainHexes;
    }

    private Map<HexTile, StackPane> mapVisualTiles(Board board, List<StackPane> mainHexes,
                                                   Map<StackPane, HexTile> visualToModel) {
        Map<HexTile, StackPane> modelToVisual = new HashMap<>();
        Set<HexTile> usedTiles = new HashSet<>();
        for (StackPane sp : mainHexes) {
            HexTile tile = findHexTileByVisualFallback(board, sp, usedTiles);
            if (tile != null) {
                applyVisualTileModel(sp, tile);
                visualToModel.put(sp, tile);
                modelToVisual.put(tile, sp);
                usedTiles.add(tile);
            }
        }
        return modelToVisual;
    }

    private void applyVisualTileModel(StackPane visual, HexTile tile) {
        visual.getStyleClass().removeIf(style -> style.startsWith("hex-tile-"));
        visual.getStyleClass().add(styleForTerrain(tile.getTerrainType()));

        boolean hasToken = tile.getNumberToken() != null;
        for (javafx.scene.Node child : visual.getChildren()) {
            if (child instanceof Circle circle) {
                circle.setVisible(hasToken);
                circle.setManaged(hasToken);
            }
            if (child instanceof Label label && isTerrainOrTokenLabel(label)) {
                label.setText(hasToken ? String.valueOf(tile.getNumberToken().getValue()) : "Desert");
            }
        }
    }

    private boolean isTerrainOrTokenLabel(Label label) {
        String text = label.getText();
        if (text == null) return false;
        if ("Desert".equalsIgnoreCase(text.trim())) return true;
        try {
            Integer.parseInt(text.trim());
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String styleForTerrain(TerrainType terrain) {
        return switch (terrain) {
            case DESERT -> "hex-tile-desert";
            case FOREST -> "hex-tile-wood";
            case HILL -> "hex-tile-brick";
            case FIELD -> "hex-tile-wheat";
            case MOUNTAIN -> "hex-tile-ore";
            case BANANA_PLANTATION -> "hex-tile-banana";
        };
    }

    private void mapIntersections(Board board, Map<HexTile, StackPane> modelToVisual,
                                  Map<Intersection, double[]> intersectionCoordinates) {
        Map<String, double[]> cornerSum = new LinkedHashMap<>();
        Map<String, Integer> cornerCount = new HashMap<>();

        for (HexTile tile : board.getAllHexTiles()) {
            StackPane sp = modelToVisual.get(tile);
            if (sp == null) continue;
            double[][] uiCorners = getHexCorners(sp);

            for (int cornerIndex = 0; cornerIndex < 6; cornerIndex++) {
                String key = getCornerKey(tile, cornerIndex);
                int uiIdx = BACKEND_TO_UI_CORNER[cornerIndex];

                double[] sum = cornerSum.getOrDefault(key, new double[]{0.0, 0.0});
                sum[0] += uiCorners[uiIdx][0];
                sum[1] += uiCorners[uiIdx][1];
                cornerSum.put(key, sum);
                cornerCount.put(key, cornerCount.getOrDefault(key, 0) + 1);
            }
        }

        List<Intersection> allIntersections = board.getAllIntersections();
        int idx = 0;
        for (Map.Entry<String, double[]> entry : cornerSum.entrySet()) {
            double[] sum = entry.getValue();
            int count = cornerCount.get(entry.getKey());
            if (idx < allIntersections.size()) {
                intersectionCoordinates.put(allIntersections.get(idx), new double[]{sum[0] / count, sum[1] / count});
                idx++;
            }
        }
    }

    private String getCornerKey(HexTile tile, int cornerIndex) {
        double centerX = SQRT_3 * (tile.getColumn() + tile.getRow() / 2.0);
        double centerY = 1.5 * tile.getRow();
        double angleDeg = 60.0 * cornerIndex - 30.0;
        double angleRad = Math.toRadians(angleDeg);
        double x = centerX + Math.cos(angleRad);
        double y = centerY + Math.sin(angleRad);
        return String.format(Locale.US, "%.6f,%.6f", x, y);
    }

    private TerrainType parseTerrainFromStyle(String style) {
        return switch (style) {
            case "hex-tile-desert" -> TerrainType.DESERT;
            case "hex-tile-wood" -> TerrainType.FOREST;
            case "hex-tile-brick" -> TerrainType.HILL;
            case "hex-tile-wheat" -> TerrainType.FIELD;
            case "hex-tile-ore" -> TerrainType.MOUNTAIN;
            case "hex-tile-banana" -> TerrainType.BANANA_PLANTATION;
            default -> null;
        };
    }

    private int parseTokenFromVisual(StackPane sp) {
        for (javafx.scene.Node child : sp.getChildren()) {
            if (child instanceof Label lbl) {
                try {
                    return Integer.parseInt(lbl.getText().trim());
                } catch (NumberFormatException ignored) {
                    // Continue scanning child labels.
                }
            }
        }
        return -1;
    }

    private HexTile findMatchingTile(Board board, TerrainType terrain, int tokenValue, Set<HexTile> usedTiles) {
        if (terrain == null) return null;
        for (HexTile tile : board.getAllHexTiles()) {
            if (usedTiles.contains(tile)) continue;
            if (tile.getTerrainType() != terrain) continue;
            if (terrain == TerrainType.DESERT) return tile;
            if (tile.getNumberToken() != null && tile.getNumberToken().getValue() == tokenValue) return tile;
        }
        return null;
    }

    public record Mapping(Map<StackPane, HexTile> visualToModelTile,
                          Map<Intersection, double[]> intersectionCoordinates,
                          List<StackPane> mainHexes) {
    }
}
