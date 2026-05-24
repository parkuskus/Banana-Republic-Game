package banana.republic.card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.TerrainType;
import banana.republic.building.PosPantau;
import banana.republic.core.GameState;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;

@DisplayName("KnightCard Effect Tests")
class KnightCardEffectTest {

    @Test
    @DisplayName("Knight card should move robber and steal one resource when victim exists")
    void knightCardMovesRobberAndSteals() {
        HumanPlayer attacker = new HumanPlayer("Attacker", PlayerColor.RED);
        HumanPlayer victim = new HumanPlayer("Victim", PlayerColor.BLUE);
        victim.addResource(ResourceType.WOOD, 2);

        HexTile currentTile = new HexTile(1, TerrainType.FOREST, null, true, 0, 0);
        HexTile victimTile = new HexTile(2, TerrainType.HILL, null, false, 1, 0);
        Intersection victimIntersection = new Intersection(1, List.of(victimTile), List.of());
        victimIntersection.placeBuilding(new PosPantau(victim));

        Board board = new Board(
            List.of(currentTile, victimTile),
            List.of(victimIntersection),
            List.of(),
            List.of()
        );

        GameState state = new MockGameState(board, List.of(attacker, victim), victimTile, victim);
        KnightCard card = new KnightCard();

        card.applyEffect(state, attacker);

        assertEquals(1, attacker.getKnightsPlayed());
        assertNotNull(board.getRobberTile().orElse(null));
        assertSame(victimTile, board.getRobberTile().orElse(null));
        assertEquals(1, victim.getResourceCount(ResourceType.WOOD));
        assertEquals(1, attacker.getResourceCount(ResourceType.WOOD));
    }

    private static final class MockGameState implements GameState {
        private final Board board;
        private final List<Player> players;
        private final HexTile selectedTarget;
        private final Player selectedVictim;

        private MockGameState(Board board, List<Player> players, HexTile selectedTarget, Player selectedVictim) {
            this.board = board;
            this.players = players;
            this.selectedTarget = selectedTarget;
            this.selectedVictim = selectedVictim;
        }

        @Override
        public List<Player> getAllPlayers() {
            return players;
        }

        @Override
        public Bank getBank() {
            return null;
        }

        @Override
        public Board getBoard() {
            return board;
        }

        @Override
        public HexTile chooseKnightTarget(Player player, List<HexTile> candidates) {
            return selectedTarget;
        }

        @Override
        public Player chooseKnightVictim(Player player, HexTile target, List<Player> candidates) {
            return selectedVictim;
        }
    }
}