package banana.republic.board;

import banana.republic.building.Road;
import banana.republic.player.Player;

public class Path {

    private final int id;
    private final Intersection intersectionA;
    private final Intersection intersectionB;
    private Road road;

    public Path(int id, Intersection intersectionA, Intersection intersectionB) {
        if (intersectionA == null || intersectionB == null) {
            throw new IllegalArgumentException("Path intersections cannot be null");
        }
        this.id = id;
        this.intersectionA = intersectionA;
        this.intersectionB = intersectionB;
    }

    public int getId() {
        return id;
    }

    public boolean isEmpty() {
        return road == null;
    }

    public boolean hasRoad() {
        return road != null;
    }

    public Road getRoad() {
        return road;
    }

    public void placeRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("Road cannot be null");
        }
        if (this.road != null) {
            throw new IllegalStateException("Path already has a road");
        }
        this.road = road;
    }

    public Player getOwner() {
        if (road == null) {
            return null;
        }
        return road.getOwner();
    }

    public Intersection getIntersectionA() {
        return intersectionA;
    }

    public Intersection getIntersectionB() {
        return intersectionB;
    }

    public boolean isCoastal() {
        return isEdgeIntersection(intersectionA) || isEdgeIntersection(intersectionB);
    }

    private boolean isEdgeIntersection(Intersection intersection) {
        if (intersection == null) {
            return false;
        }
        return intersection.getAdjacentHexTiles().size() < 3;
    }
}
