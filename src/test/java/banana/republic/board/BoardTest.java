package banana.republic.board;

import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.building.Road;
import banana.republic.building.PosPantau;
import banana.republic.plugin.StandardMapGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Buildable Location Tests")
class BoardTest {

    private Board board;
    private Player player;
    private Player other;

    @BeforeEach
    void setUp() {
        StandardMapGenerator generator = new StandardMapGenerator();
        board = generator.generateBoard();
        player = new HumanPlayer("Alice", PlayerColor.RED);
        other = new HumanPlayer("Bob", PlayerColor.BLUE);
    }

    @Test
    @DisplayName("getBuildableRoadPaths should return empty when player has no roads")
    void testBuildableRoadsEmptyInitially() {
        List<Path> roads = board.getBuildableRoadPaths(player);
        assertTrue(roads.isEmpty(), "Tanpa road, tidak ada path yang bisa dibangun");
    }

    @Test
    @DisplayName("getBuildableRoadPaths should include paths adjacent to owned settlement")
    void testBuildableRoadsAfterSettlement() {
        Intersection intersection = board.getAllIntersections().get(0);
        intersection.placeBuilding(new PosPantau(player));

        List<Path> roads = board.getBuildableRoadPaths(player);
        assertFalse(roads.isEmpty(), "Setelah menempatkan settlement, ada path yang bisa dibangun");
    }

    @Test
    @DisplayName("getBuildableRoadPaths should include paths adjacent to owned road")
    void testBuildableRoadsAfterRoad() {
        // Tempatkan settlement di intersection pertama
        Intersection settlement = board.getAllIntersections().get(0);
        settlement.placeBuilding(new PosPantau(player));

        // Bangun road di path pertama yang terhubung
        Path firstPath = settlement.getAdjacentPaths().get(0);
        firstPath.placeRoad(new Road(player));

        // Sekarang seharusnya ada path lain yang bisa dibangun dari road tersebut
        List<Path> roads = board.getBuildableRoadPaths(player);
        assertTrue(roads.size() >= 1, "Setelah road, ada path lain yang terhubung");
        assertFalse(roads.contains(firstPath), "Path yang sudah ada road tidak muncul");
    }

    @Test
    @DisplayName("getBuildableRoadPaths should reject paths blocked by opponent settlement")
    void testBuildableRoadsBlockedByOpponent() {
        Intersection intersection = board.getAllIntersections().get(0);
        intersection.placeBuilding(new PosPantau(other));

        List<Path> roads = board.getBuildableRoadPaths(player);
        assertTrue(roads.isEmpty(), "Tidak bisa bangun road dekat settlement lawan tanpa road sendiri");
    }

    @Test
    @DisplayName("getBuildableSettlements should return empty initially")
    void testBuildableSettlementsEmptyInitially() {
        List<Intersection> settlements = board.getBuildableSettlements(player);
        assertTrue(settlements.isEmpty(), "Tanpa road tidak bisa bangun settlement");
    }

    @Test
    @DisplayName("getBuildableSettlements should include valid intersections connected by road")
    void testBuildableSettlementsAfterRoad() {
        // Setup: tempatkan settlement di intersection 0
        Intersection start = board.getAllIntersections().get(0);
        start.placeBuilding(new PosPantau(player));

        // Bangun rantai road dari start
        Path road1 = start.getAdjacentPaths().get(0);
        road1.placeRoad(new Road(player));
        Intersection middle = road1.getIntersectionA().equals(start) ? road1.getIntersectionB() : road1.getIntersectionA();

        // Middle adalah neighbor langsung dari start, jadi tidak bisa dibangun karena distance rule
        List<Intersection> buildable = board.getBuildableSettlements(player);
        assertFalse(buildable.contains(middle), "Neighbor langsung tidak boleh dibangun");

        // Bangun road kedua dari middle (jika ada)
        Path road2 = middle.getAdjacentPaths().stream()
            .filter(p -> !p.equals(road1) && p.isEmpty())
            .findFirst().orElse(null);
        if (road2 != null) {
            road2.placeRoad(new Road(player));
            Intersection end = road2.getIntersectionA().equals(middle) ? road2.getIntersectionB() : road2.getIntersectionA();
            buildable = board.getBuildableSettlements(player);
            // End bisa dibangun jika bukan neighbor langsung dari start
            if (!start.getNeighboringIntersections().contains(end)) {
                assertTrue(buildable.contains(end), "Intersection di ujung rantai road harus bisa dibangun");
            }
        }

        // Pastikan semua buildable intersections memang terhubung road dan valid distance
        for (Intersection i : buildable) {
            assertTrue(i.getAdjacentPaths().stream().anyMatch(
                p -> p.hasRoad() && player.equals(p.getOwner())),
                "Setiap buildable intersection harus terhubung ke road milik pemain");
            assertTrue(board.isDistanceRuleValid(i),
                "Setiap buildable intersection harus memenuhi distance rule");
        }
    }

    @Test
    @DisplayName("getBuildableSettlements should respect distance rule")
    void testBuildableSettlementsDistanceRule() {
        // Setup: bangun settlement di intersection 0 dan road ke neighbor
        Intersection i0 = board.getAllIntersections().get(0);
        i0.placeBuilding(new PosPantau(player));

        // Cari neighbor yang terhubung langsung
        Intersection neighbor = i0.getNeighboringIntersections().get(0);
        // Neighbor tidak boleh bisa dibangun karena distance rule

        List<Intersection> buildable = board.getBuildableSettlements(player);
        assertFalse(buildable.contains(neighbor), "Neighbor langsung tidak boleh dibangun (distance rule)");
    }

    @Test
    @DisplayName("getBuildableCities should return empty when no settlements")
    void testBuildableCitiesEmptyInitially() {
        List<Intersection> cities = board.getBuildableCities(player);
        assertTrue(cities.isEmpty(), "Tanpa settlement tidak ada yang bisa di-upgrade");
    }

    @Test
    @DisplayName("getBuildableCities should include owned Pos Pantau")
    void testBuildableCitiesWithSettlement() {
        Intersection intersection = board.getAllIntersections().get(0);
        intersection.placeBuilding(new PosPantau(player));

        List<Intersection> cities = board.getBuildableCities(player);
        assertEquals(1, cities.size(), "Settlement milik sendiri bisa di-upgrade");
        assertEquals(intersection, cities.get(0));
    }

    @Test
    @DisplayName("getBuildableCities should exclude opponent settlements")
    void testBuildableCitiesExcludesOpponent() {
        Intersection intersection = board.getAllIntersections().get(0);
        intersection.placeBuilding(new PosPantau(other));

        List<Intersection> cities = board.getBuildableCities(player);
        assertTrue(cities.isEmpty(), "Settlement lawan tidak bisa di-upgrade");
    }

    @Test
    @DisplayName("getBuildableRoadPaths should return empty for null player")
    void testBuildableRoadsNullPlayer() {
        assertTrue(board.getBuildableRoadPaths(null).isEmpty());
    }

    @Test
    @DisplayName("getBuildableSettlements should return empty for null player")
    void testBuildableSettlementsNullPlayer() {
        assertTrue(board.getBuildableSettlements(null).isEmpty());
    }

    @Test
    @DisplayName("getBuildableCities should return empty for null player")
    void testBuildableCitiesNullPlayer() {
        assertTrue(board.getBuildableCities(null).isEmpty());
    }
}
