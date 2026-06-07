package banana.republic.ui.board;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.resource.ResourceType;
import javafx.scene.layout.StackPane;

public class BuildInteractionController {

    private static final double ADJACENT_BUILDING_DISTANCE = 60.0;
    private static final double INTERSECTION_HIT_RADIUS = 20.0;
    private static final double HARBOR_HIT_RADIUS = 20.0;

    public boolean isVisualDistanceRuleValid(Intersection candidate,
                                             Map<Intersection, IntersectionCoordinate> coordinates) {
        IntersectionCoordinate candidateCoordinate = coordinates.get(candidate);
        if (candidateCoordinate == null) return true;
        for (Map.Entry<Intersection, IntersectionCoordinate> entry : coordinates.entrySet()) {
            Intersection other = entry.getKey();
            if (other == candidate || !other.hasBuilding()) continue;
            if (candidateCoordinate.distanceTo(entry.getValue()) < ADJACENT_BUILDING_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    public boolean isVisualDistanceRuleValidFromRaw(Intersection candidate,
                                                    Map<Intersection, double[]> coordinates) {
        IntersectionCoordinate candidateCoordinate = toCoordinate(coordinates.get(candidate));
        if (candidateCoordinate == null) return true;
        for (Map.Entry<Intersection, double[]> entry : coordinates.entrySet()) {
            Intersection other = entry.getKey();
            if (other == candidate || !other.hasBuilding()) continue;
            IntersectionCoordinate otherCoordinate = toCoordinate(entry.getValue());
            if (candidateCoordinate.distanceTo(otherCoordinate) < ADJACENT_BUILDING_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    public Intersection findClosestIntersection(double x, double y, Map<Intersection, double[]> coordinates) {
        Intersection closest = null;
        double minDist = Double.MAX_VALUE;
        for (Map.Entry<Intersection, double[]> entry : coordinates.entrySet()) {
            double[] coordinate = entry.getValue();
            double dist = Math.hypot(coordinate[0] - x, coordinate[1] - y);
            if (dist < minDist) {
                minDist = dist;
                closest = entry.getKey();
            }
        }
        return minDist < INTERSECTION_HIT_RADIUS ? closest : null;
    }

    public Path findPathBetween(Intersection a, Intersection b) {
        if (a == null || b == null || a.getAdjacentPaths() == null) return null;
        for (Path path : a.getAdjacentPaths()) {
            if (path == null) continue;
            if (path.getIntersectionA().getId() == b.getId() || path.getIntersectionB().getId() == b.getId()) {
                return path;
            }
        }
        return null;
    }

    public boolean isNearHarbor(double x, double y, List<double[]> harborPoints) {
        return harborPoints.stream().anyMatch(point -> Math.hypot(point[0] - x, point[1] - y) < HARBOR_HIT_RADIUS);
    }

    public Map<ResourceType, Integer> visualAdjacentInitialResources(Intersection intersection,
                                                                     Map<Intersection, double[]> intersectionCoordinates,
                                                                     Map<StackPane, HexTile> visualToModelTile,
                                                                     BoardCoordinateMapper coordinateMapper) {
        Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
        double[] coordinates = intersectionCoordinates.get(intersection);
        if (coordinates == null) return resources;

        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            HexTile modelTile = entry.getValue();
            if (modelTile == null || !modelTile.canProduce()) continue;

            double[][] corners = coordinateMapper.getHexCorners(entry.getKey());
            for (double[] corner : corners) {
                if (Math.hypot(corner[0] - coordinates[0], corner[1] - coordinates[1]) < 28.0) {
                    ResourceType type = modelTile.getResourceType();
                    if (type != null) resources.merge(type, 1, Integer::sum);
                    break;
                }
            }
        }
        return resources;
    }

    private IntersectionCoordinate toCoordinate(double[] coordinate) {
        if (coordinate == null || coordinate.length < 2) return null;
        return new IntersectionCoordinate(coordinate[0], coordinate[1]);
    }
}
