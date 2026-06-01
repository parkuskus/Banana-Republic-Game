package banana.republic.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import banana.republic.board.Board;
import banana.republic.board.Harbor;
import banana.republic.board.HarborType;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.NumberToken;
import banana.republic.board.Path;
import banana.republic.board.TerrainType;

public class StandardMapGenerator implements MapGeneratorPlugin {

    private static final double SQRT_3 = Math.sqrt(3.0);

    private static final TerrainType[] FIXED_HEX_LAYOUT = new TerrainType[] {
        // Row -2: top row in game.fxml
        TerrainType.FOREST,
        TerrainType.HILL,
        TerrainType.FOREST,
        // Row -1
        TerrainType.MOUNTAIN,
        TerrainType.BANANA_PLANTATION,
        TerrainType.FIELD,
        TerrainType.FOREST,
        // Row 0
        TerrainType.FOREST,
        TerrainType.HILL,
        TerrainType.DESERT,
        TerrainType.FIELD,
        TerrainType.MOUNTAIN,
        // Row 1
        TerrainType.FOREST,
        TerrainType.HILL,
        TerrainType.FOREST,
        TerrainType.BANANA_PLANTATION,
        // Row 2
        TerrainType.FIELD,
        TerrainType.MOUNTAIN,
        TerrainType.FOREST
    };

    private static final int[] FIXED_TOKEN_LAYOUT = new int[] {
        5, 2, 6,
        3, 8, 10, 9,
        12, 11, 4, 8,
        10, 9, 4, 5,
        6, 3, 11
    };

    private static final HarborType[] HARBOR_CONFIG = new HarborType[] {
        HarborType.GENERIC_3TO1,
        HarborType.GENERIC_3TO1,
        HarborType.GENERIC_3TO1,
        HarborType.GENERIC_3TO1,
        HarborType.BANANA_2TO1,
        HarborType.WOOD_2TO1,
        HarborType.BRICK_2TO1,
        HarborType.WHEAT_2TO1,
        HarborType.ORE_2TO1
    };

    @Override
    public Board generateBoard() {
        List<HexTile> hexTiles = buildHexTiles();
        Map<String, Intersection> intersectionsByKey = buildIntersections(hexTiles);
        List<Path> paths = buildPaths(hexTiles, intersectionsByKey);
        List<Harbor> harbors = buildHarbors(paths);
        List<Intersection> intersections = new ArrayList<>(intersectionsByKey.values());
        return new Board(hexTiles, intersections, paths, harbors);
    }

    private List<HexTile> buildHexTiles() {
        if (FIXED_HEX_LAYOUT.length != 19) {
            throw new IllegalStateException("Fixed hex layout must contain 19 tiles");
        }
        if (FIXED_TOKEN_LAYOUT.length != 18) {
            throw new IllegalStateException("Fixed token layout must contain 18 tokens");
        }

        List<HexTile> tiles = new ArrayList<>();
        List<AxialCoord> coords = buildAxialCoords();
        int tokenIndex = 0;

        for (int i = 0; i < coords.size(); i++) {
            TerrainType terrainType = FIXED_HEX_LAYOUT[i];
            AxialCoord coord = coords.get(i);
            NumberToken token = null;
            boolean hasRobber = false;

            if (terrainType == TerrainType.DESERT) {
                hasRobber = true;
            } else {
                token = new NumberToken(FIXED_TOKEN_LAYOUT[tokenIndex]);
                tokenIndex++;
            }

            HexTile tile = new HexTile(i + 1, terrainType, token, hasRobber, coord.q, coord.r);
            tiles.add(tile);
        }

        return tiles;
    }

    private Map<String, Intersection> buildIntersections(List<HexTile> hexTiles) {
        Map<String, CornerData> cornerData = new LinkedHashMap<>();

        for (HexTile tile : hexTiles) {
            for (int cornerIndex = 0; cornerIndex < 6; cornerIndex++) {
                String key = getCornerKey(tile, cornerIndex);
                CornerData data = cornerData.get(key);
                if (data == null) {
                    data = new CornerData();
                    cornerData.put(key, data);
                }
                data.addTile(tile);
            }
        }

        Map<String, Intersection> intersectionsByKey = new LinkedHashMap<>();
        int id = 1;
        for (Map.Entry<String, CornerData> entry : cornerData.entrySet()) {
            Intersection intersection = new Intersection(id, entry.getValue().adjacentTiles,
                new ArrayList<>());
            intersectionsByKey.put(entry.getKey(), intersection);
            id++;
        }

        return intersectionsByKey;
    }

    private List<Path> buildPaths(List<HexTile> hexTiles, Map<String, Intersection> intersections) {
        Map<String, Path> pathMap = new HashMap<>();
        List<Path> paths = new ArrayList<>();
        int id = 1;

        for (HexTile tile : hexTiles) {
            for (int cornerIndex = 0; cornerIndex < 6; cornerIndex++) {
                String keyA = getCornerKey(tile, cornerIndex);
                String keyB = getCornerKey(tile, (cornerIndex + 1) % 6);
                Intersection intersectionA = intersections.get(keyA);
                Intersection intersectionB = intersections.get(keyB);
                if (intersectionA == null || intersectionB == null) {
                    continue;
                }
                String pathKey = buildPathKey(intersectionA.getId(), intersectionB.getId());
                if (pathMap.containsKey(pathKey)) {
                    continue;
                }
                Path path = new Path(id, intersectionA, intersectionB);
                pathMap.put(pathKey, path);
                paths.add(path);
                intersectionA.addAdjacentPath(path);
                intersectionB.addAdjacentPath(path);
                id++;
            }
        }

        return paths;
    }

    private List<Harbor> buildHarbors(List<Path> paths) {
        List<Path> coastalPaths = new ArrayList<>();
        for (Path path : paths) {
            if (path.isCoastal()) {
                coastalPaths.add(path);
            }
        }

        int harborCount = Math.min(HARBOR_CONFIG.length, coastalPaths.size());
        List<Harbor> harbors = new ArrayList<>();
        for (int i = 0; i < harborCount; i++) {
            Path path = coastalPaths.get(i);
            List<Intersection> adjacent = List.of(path.getIntersectionA(), path.getIntersectionB());
            Harbor harbor = new Harbor(i + 1, HARBOR_CONFIG[i], adjacent);
            harbors.add(harbor);
        }

        return harbors;
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

    private String buildPathKey(int a, int b) {
        int min = Math.min(a, b);
        int max = Math.max(a, b);
        return min + "-" + max;
    }

    private List<AxialCoord> buildAxialCoords() {
        List<AxialCoord> coords = new ArrayList<>();
        int radius = 2;
        for (int r = -radius; r <= radius; r++) {
            int qMin = Math.max(-radius, -r - radius);
            int qMax = Math.min(radius, -r + radius);
            for (int q = qMin; q <= qMax; q++) {
                coords.add(new AxialCoord(q, r));
            }
        }
        return coords;
    }

    private static final class AxialCoord {
        private final int q;
        private final int r;

        private AxialCoord(int q, int r) {
            this.q = q;
            this.r = r;
        }
    }

    private static final class CornerData {
        private final List<HexTile> adjacentTiles = new ArrayList<>();

        private void addTile(HexTile tile) {
            if (!adjacentTiles.contains(tile)) {
                adjacentTiles.add(tile);
            }
        }
    }
}
