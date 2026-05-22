package banana.republic.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import banana.republic.player.Player;

public class Board {

    private final List<HexTile> hexTiles;
    private final List<Intersection> intersections;
    private final List<Path> paths;
    private final List<Harbor> harbors;

    public Board(List<HexTile> hexTiles, List<Intersection> intersections,
                 List<Path> paths, List<Harbor> harbors) {
        this.hexTiles = copyList(hexTiles);
        this.intersections = copyList(intersections);
        this.paths = copyList(paths);
        this.harbors = copyList(harbors);
    }

    public HexTile getHexTileById(int id) {
        for (HexTile tile : hexTiles) {
            if (tile.getId() == id) {
                return tile;
            }
        }
        return null;
    }

    public Intersection getIntersectionById(int id) {
        for (Intersection intersection : intersections) {
            if (intersection.getId() == id) {
                return intersection;
            }
        }
        return null;
    }

    public Path getPathById(int id) {
        for (Path path : paths) {
            if (path.getId() == id) {
                return path;
            }
        }
        return null;
    }

    public List<Intersection> getAdjacentIntersections(HexTile hex) {
        if (hex == null) {
            return List.of();
        }
        List<Intersection> result = new ArrayList<>();
        for (Intersection intersection : intersections) {
            if (intersection.getAdjacentHexTiles().contains(hex)) {
                result.add(intersection);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public List<HexTile> getAdjacentHexTiles(Intersection intersection) {
        if (intersection == null) {
            return List.of();
        }
        return intersection.getAdjacentHexTiles();
    }

    public List<Path> getAdjacentPaths(Intersection intersection) {
        if (intersection == null) {
            return List.of();
        }
        return intersection.getAdjacentPaths();
    }

    public List<Intersection> getAdjacentIntersections(Path path) {
        if (path == null) {
            return List.of();
        }
        List<Intersection> result = new ArrayList<>();
        result.add(path.getIntersectionA());
        result.add(path.getIntersectionB());
        return Collections.unmodifiableList(result);
    }

    public List<HexTile> getTilesWithToken(int number) {
        List<HexTile> result = new ArrayList<>();
        for (HexTile tile : hexTiles) {
            NumberToken token = tile.getNumberToken();
            if (token != null && token.getValue() == number) {
                result.add(tile);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public boolean isDistanceRuleValid(Intersection intersection) {
        if (intersection == null) {
            return false;
        }
        for (Intersection neighbor : intersection.getNeighboringIntersections()) {
            if (neighbor.hasBuilding()) {
                return false;
            }
        }
        return true;
    }

    public List<Path> getConnectedRoads(Player player) {
        if (player == null) {
            return List.of();
        }
        List<Path> result = new ArrayList<>();
        for (Path path : paths) {
            if (path.hasRoad() && player.equals(path.getOwner())) {
                result.add(path);
            }
        }
        return Collections.unmodifiableList(result);
    }

    public Optional<Harbor> getHarborAt(Intersection intersection) {
        if (intersection == null) {
            return Optional.empty();
        }
        for (Harbor harbor : harbors) {
            if (harbor.getAdjacentIntersections().contains(intersection)) {
                return Optional.of(harbor);
            }
        }
        return Optional.empty();
    }

    public List<Intersection> getAllIntersections() {
        return Collections.unmodifiableList(intersections);
    }

    public List<Path> getAllPaths() {
        return Collections.unmodifiableList(paths);
    }

    private <T> List<T> copyList(List<T> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }
}
