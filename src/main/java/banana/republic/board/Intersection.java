package banana.republic.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import banana.republic.building.Building;
import banana.republic.player.Player;

public class Intersection {

    private final int id;
    private final List<HexTile> adjacentHexTiles;
    private final List<Path> adjacentPaths;
    private Building building;

    public Intersection(int id, List<HexTile> adjacentHexTiles, List<Path> adjacentPaths) {
        this.id = id;
        this.adjacentHexTiles = copyList(adjacentHexTiles);
        this.adjacentPaths = copyList(adjacentPaths);
    }

    public int getId() {
        return id;
    }

    public boolean isEmpty() {
        return building == null;
    }

    public boolean hasBuilding() {
        return building != null;
    }

    public Building getBuilding() {
        return building;
    }

    public void placeBuilding(Building building) {
        if (building == null) {
            throw new IllegalArgumentException("Building cannot be null");
        }
        if (this.building != null) {
            throw new IllegalStateException("Intersection already has a building");
        }
        this.building = building;
    }

    public Building removeBuilding() {
        Building removed = this.building;
        this.building = null;
        return removed;
    }

    public Player getOwner() {
        if (building == null) {
            return null;
        }
        return building.getOwner();
    }

    public List<HexTile> getAdjacentHexTiles() {
        return Collections.unmodifiableList(adjacentHexTiles);
    }

    public List<Path> getAdjacentPaths() {
        return Collections.unmodifiableList(adjacentPaths);
    }

    public void addAdjacentPath(Path path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        adjacentPaths.add(path);
    }

    public List<Intersection> getNeighboringIntersections() {
        Set<Intersection> neighbors = new LinkedHashSet<>();
        for (Path path : adjacentPaths) {
            if (path.getIntersectionA() != null && path.getIntersectionA() != this) {
                neighbors.add(path.getIntersectionA());
            }
            if (path.getIntersectionB() != null && path.getIntersectionB() != this) {
                neighbors.add(path.getIntersectionB());
            }
        }
        return new ArrayList<>(neighbors);
    }

    private <T> List<T> copyList(List<T> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }
}
