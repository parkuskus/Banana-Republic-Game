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
import banana.republic.core.GameLog;
import banana.republic.core.GameState;
import banana.republic.core.LogEntry;
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

        GameLog gameLog = new GameLog();
        GameState state = new MockGameState(board, List.of(attacker, victim), victimTile, victim, gameLog);
        KnightCard card = new KnightCard();

        card.applyEffect(state, attacker);

        assertEquals(1, attacker.getKnightsPlayed());
        assertNotNull(board.getRobberTile().orElse(null));
        assertSame(victimTile, board.getRobberTile().orElse(null));
        assertEquals(1, victim.getResourceCount(ResourceType.WOOD));
        assertEquals(1, attacker.getResourceCount(ResourceType.WOOD));
        assertEquals(1, gameLog.getEntriesByType(LogEntry.EventType.ROBBER).size());
        assertEquals(1, gameLog.getEntriesByType(LogEntry.EventType.STEAL).size());
    }

    private static final class MockGameState implements GameState {
        private final Board board;
        private final List<Player> players;
        private final HexTile selectedTarget;
        private final Player selectedVictim;
        private final GameLog gameLog;

        private MockGameState(Board board, List<Player> players, HexTile selectedTarget, Player selectedVictim, GameLog gameLog) {
            this.board = board;
            this.players = players;
            this.selectedTarget = selectedTarget;
            this.selectedVictim = selectedVictim;
            this.gameLog = gameLog;
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
            return gameLog;
        }

        @Override
        public int getTurnNumber() {
            return 1;
        }

        @Override
        public HexTile chooseKnightTarget(Player player, List<HexTile> candidates) {
            return selectedTarget;
        }

        @Override
        public Player chooseKnightVictim(Player player, HexTile target, List<Player> candidates) {
            return selectedVictim;
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
        public HexTile getRobberPosition() {
            return board != null
                ? board.getRobberTile().orElse(null)
                : null;
        }

        @Override
        public List<banana.republic.board.Path> chooseRoadBuildingPaths(Player player, List<banana.republic.board.Path> candidates, int maxPlacements) {
            return List.of();
        }

        @Override
        public void activateRobber(banana.republic.board.HexTile target, banana.republic.player.Player activePlayer, banana.republic.player.Player victim) {
            board.moveRobber(target);
            gameLog.addEntry(LogEntry.EventType.ROBBER, activePlayer.getName(),
                activePlayer.getName() + " memindahkan Nimon Ungu ke tile #" + target.getId());
            if (victim != null && victim.getTotalResourceCount() > 0) {
                for (ResourceType type : ResourceType.values()) {
                    if (victim.getResourceCount(type) > 0) {
                        victim.removeResource(type, 1);
                        activePlayer.addResource(type, 1);
                        gameLog.addEntry(LogEntry.EventType.STEAL, activePlayer.getName(),
                            activePlayer.getName() + " mencuri 1 " + type.getDisplayName() + " dari " + victim.getName());
                        break;
                    }
                }
            }
        }
    }
}