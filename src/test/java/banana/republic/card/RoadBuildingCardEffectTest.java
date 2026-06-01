package banana.republic.card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.board.Board;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.building.PosPantau;
import banana.republic.core.GameState;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.Bank;

@DisplayName("RoadBuildingCard Effect Tests")
class RoadBuildingCardEffectTest {

    @Test
    @DisplayName("Road building card should place up to two free roads")
    void roadBuildingCardPlacesTwoRoads() {
        HumanPlayer player = new HumanPlayer("Builder", PlayerColor.ORANGE);

        Intersection a = new Intersection(1, List.of(), List.of());
        Intersection b = new Intersection(2, List.of(), List.of());
        Intersection c = new Intersection(3, List.of(), List.of());
        Path ab = new Path(1, a, b);
        Path ac = new Path(2, a, c);
        a.addAdjacentPath(ab);
        a.addAdjacentPath(ac);
        b.addAdjacentPath(ab);
        c.addAdjacentPath(ac);
        a.placeBuilding(new PosPantau(player));

        Board board = new Board(List.of(), List.of(a, b, c), List.of(ab, ac), List.of());
        GameState state = new MockGameState(board, List.of(player), List.of(ab, ac));

        RoadBuildingCard card = new RoadBuildingCard();
        card.applyEffect(state, player);

        assertEquals(13, player.getSupply().getRoadsRemaining());
        assertSame(player, ab.getOwner());
        assertSame(player, ac.getOwner());
    }

    private static final class MockGameState implements GameState {
        private final Board board;
        private final List<Player> players;
        private final List<Path> selectedPaths;

        private MockGameState(Board board, List<Player> players, List<Path> selectedPaths) {
            this.board = board;
            this.players = players;
            this.selectedPaths = selectedPaths;
        }

        @Override
        public List<Player> getAllPlayers() {
            return players;
        }

        @Override
        public Player getActivePlayer() {
            return players.isEmpty() ? null : players.get(0);
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
        public banana.republic.core.GamePhase getCurrentPhase() {
            return null;
        }

        @Override
        public banana.republic.core.GameLog getGameLog() {
            return null;
        }

        @Override
        public int getTurnNumber() {
            return 1;
        }

        @Override
        public List<Path> chooseRoadBuildingPaths(Player player, List<Path> candidates, int maxPlacements) {
            return selectedPaths;
        }

        @Override
        public Player getCurrentPlayer() {
            return players.isEmpty() ? null : players.get(0);
        }

        @Override
        public banana.republic.card.CardDeck getCardDeck() {
            return null;
        }

        @Override
        public banana.republic.board.HexTile getRobberPosition() {
            return board != null
                ? board.getRobberTile().orElse(null)
                : null;
        }

        @Override
        public banana.republic.board.HexTile chooseKnightTarget(Player player, List<banana.republic.board.HexTile> candidates) {
            return null;
        }

        @Override
        public Player chooseKnightVictim(Player player, banana.republic.board.HexTile target, List<Player> candidates) {
            return null;
        }
    }
}