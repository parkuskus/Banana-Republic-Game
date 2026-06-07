package banana.republic.ui.board;

public record IntersectionCoordinate(double x, double y) {
    public double distanceTo(IntersectionCoordinate other) {
        if (other == null) return Double.MAX_VALUE;
        return Math.hypot(x - other.x, y - other.y);
    }
}
