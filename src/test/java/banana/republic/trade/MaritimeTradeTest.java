package banana.republic.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;

import banana.republic.board.Board;
import banana.republic.board.Harbor;
import banana.republic.board.HarborType;
import banana.republic.board.Intersection;
import banana.republic.building.PosPantau;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Maritime Trade Tests")
class MaritimeTradeTest {

    private final MaritimeTrade maritimeTrade = new MaritimeTrade();

    @Test
    @DisplayName("default bank trade ratio is 4:1 without harbor")
    void defaultRatioWithoutHarbor() {
        Player player = new HumanPlayer("Alice", PlayerColor.RED);
        Intersection ownedIntersection = ownedIntersection(player);
        Board board = new Board(List.of(), List.of(ownedIntersection), List.of(), List.of());

        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.WOOD, board));
        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.BANANA, board));
    }

    @Test
    @DisplayName("generic harbor gives 3:1 for every resource")
    void genericHarborAppliesToEveryResource() {
        Player player = new HumanPlayer("Alice", PlayerColor.RED);
        Intersection harborIntersection = ownedIntersection(player);
        Harbor harbor = new Harbor(1, HarborType.GENERIC_3TO1, List.of(harborIntersection));
        Board board = new Board(List.of(), List.of(harborIntersection), List.of(), List.of(harbor));

        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.WOOD, board));
        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.BRICK, board));
        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.WHEAT, board));
        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.ORE, board));
        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.BANANA, board));
    }

    @Test
    @DisplayName("specific harbor gives 2:1 only for its printed resource")
    void specificHarborOnlyAppliesToPrintedResource() {
        Player player = new HumanPlayer("Alice", PlayerColor.RED);
        Intersection harborIntersection = ownedIntersection(player);
        Harbor harbor = new Harbor(1, HarborType.WOOD_2TO1, List.of(harborIntersection));
        Board board = new Board(List.of(), List.of(harborIntersection), List.of(), List.of(harbor));

        assertEquals(2, maritimeTrade.getBestRatio(player, ResourceType.WOOD, board));
        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.BRICK, board));
        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.WHEAT, board));
        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.ORE, board));
        assertEquals(4, maritimeTrade.getBestRatio(player, ResourceType.BANANA, board));
    }

    @Test
    @DisplayName("generic harbor is still best fallback for non-specific resources")
    void genericHarborIsFallbackWhenSpecificDoesNotMatch() {
        Player player = new HumanPlayer("Alice", PlayerColor.RED);
        Intersection genericIntersection = ownedIntersection(player);
        Intersection woodIntersection = ownedIntersection(player);
        Harbor genericHarbor = new Harbor(1, HarborType.GENERIC_3TO1, List.of(genericIntersection));
        Harbor woodHarbor = new Harbor(2, HarborType.WOOD_2TO1, List.of(woodIntersection));
        Board board = new Board(
                List.of(),
                List.of(genericIntersection, woodIntersection),
                List.of(),
                List.of(genericHarbor, woodHarbor)
        );

        assertEquals(2, maritimeTrade.getBestRatio(player, ResourceType.WOOD, board));
        assertEquals(3, maritimeTrade.getBestRatio(player, ResourceType.ORE, board));
    }

    private Intersection ownedIntersection(Player player) {
        Intersection intersection = new Intersection(1, List.of(), List.of());
        intersection.placeBuilding(new PosPantau(player));
        return intersection;
    }
}
