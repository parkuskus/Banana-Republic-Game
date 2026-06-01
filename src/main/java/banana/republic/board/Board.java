package banana.republic.board;

import banana.republic.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public Optional<HexTile> getHexTileById(int id) {
        for (HexTile tile : hexTiles) {
            if (tile.getId() == id) {
                return Optional.of(tile);
            }
        }
        return Optional.empty();
    }

    public Optional<Intersection> getIntersectionById(int id) {
        for (Intersection intersection : intersections) {
            if (intersection.getId() == id) {
                return Optional.of(intersection);
            }
        }
        return Optional.empty();
    }

    public Optional<Path> getPathById(int id) {
        for (Path path : paths) {
            if (path.getId() == id) {
                return Optional.of(path);
            }
        }
        return Optional.empty();
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
        for (Intersection neighbor :
             intersection.getNeighboringIntersections()) {
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

    public List<HexTile> getAllHexTiles() {
        return Collections.unmodifiableList(hexTiles);
    }

    public List<Harbor> getHarbors() {
        return Collections.unmodifiableList(harbors);
    }

    public java.util.Optional<HexTile> getRobberTile() {
        for (HexTile tile : hexTiles) {
            if (tile.hasRobber()) {
                return java.util.Optional.of(tile);
            }
        }
        return java.util.Optional.empty();
    }

    public void moveRobber(HexTile target) {
        if (target == null) {
            throw new IllegalArgumentException("Robber target cannot be null");
        }

        for (HexTile tile : hexTiles) {
            if (tile.hasRobber()) {
                tile.setRobber(false);
            }
        }

        target.setRobber(true);
    }

    public List<Path> getBuildableRoadPaths(Player player) {
        if (player == null) {
            return List.of();
        }

        List<Path> result = new ArrayList<>();
        for (Path path : paths) {
            if (!path.isEmpty()) {
                continue;
            }
            if (isPathConnectedToPlayerNetwork(path, player)) {
                result.add(path);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Mengembalikan daftar intersections yang bisa dibangun Pos Pantau
     * oleh pemain (kosong, distance rule valid, dan terhubung ke road
     * milik pemain).
     */
    public List<Intersection> getBuildableSettlements(Player player) {
        if (player == null) {
            return List.of();
        }

        List<Intersection> result = new ArrayList<>();
        for (Intersection intersection : intersections) {
            if (intersection.hasBuilding()) {
                continue;
            }
            if (!isDistanceRuleValid(intersection)) {
                continue;
            }
            // Harus terhubung ke minimal 1 road milik pemain
            boolean connected = intersection.getAdjacentPaths().stream().anyMatch(
                p -> p.hasRoad() && player.equals(p.getOwner()));
            if (connected) {
                result.add(intersection);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Mengembalikan daftar intersections yang memiliki Pos Pantau milik
     * pemain dan bisa di-upgrade menjadi Laboratorium.
     */
    public List<Intersection> getBuildableCities(Player player) {
        if (player == null) {
            return List.of();
        }

        List<Intersection> result = new ArrayList<>();
        for (Intersection intersection : intersections) {
            if (!intersection.hasBuilding()) {
                continue;
            }
            if (!player.equals(intersection.getOwner())) {
                continue;
            }
            if (intersection.getBuilding().getBuildingType() !=
                banana.republic.building.BuildingType.POS_PANTAU) {
                continue;
            }
            result.add(intersection);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Mengembalikan true jika path terhubung ke jaringan road/bangunan
     * milik player.
     *
     * Dipakai untuk validasi buildRoad.
     */
    public boolean isPathConnectedToPlayer(Path path, Player player) {
        return isPathConnectedToPlayerNetwork(path, player);
    }

    private <T> List<T> copyList(List<T> source) {
        if (source == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(source);
    }

    private boolean isPathConnectedToPlayerNetwork(Path path, Player player) {
        if (path == null || player == null) {
            return false;
        }

        for (Intersection intersection : getAdjacentIntersections(path)) {
            if (player.equals(intersection.getOwner())) {
                return true;
            }

            for (Path adjacentPath : intersection.getAdjacentPaths()) {
                if (adjacentPath.hasRoad() &&
                    player.equals(adjacentPath.getOwner())) {
                    return true;
                }
            }
        }

        return false;
    }
}
