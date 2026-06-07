package banana.republic.card;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.core.GameLog;
import banana.republic.core.GameState;
import banana.republic.core.LogEntry;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;

@DisplayName("MonopolyCard Effect Tests")
class MonopolyCardEffectTest {

    @Test
    @DisplayName("Monopoly card should log each steal from other players")
    void monopolyCardLogsEachSteal() {
        HumanPlayer player = new HumanPlayer("Monopolist", PlayerColor.RED);
        HumanPlayer other1 = new HumanPlayer("Alice", PlayerColor.BLUE);
        HumanPlayer other2 = new HumanPlayer("Bob", PlayerColor.GREEN);

        other1.addResource(ResourceType.WOOD, 3);
        other2.addResource(ResourceType.WOOD, 2);

        GameLog gameLog = new GameLog();
        GameState state = new MockGameState(List.of(player, other1, other2), gameLog);

        MonopolyCard card = new MonopolyCard();
        card.setTargetResource(ResourceType.WOOD);
        card.applyEffect(state, player);

        List<LogEntry> cardEntries = gameLog.getEntriesByType(LogEntry.EventType.CARD_PLAYED);
        assertEquals(2, cardEntries.size(), "Two steals should produce two CARD_PLAYED log entries");

        assertEquals(5, player.getResourceCount(ResourceType.WOOD));
        assertEquals(0, other1.getResourceCount(ResourceType.WOOD));
        assertEquals(0, other2.getResourceCount(ResourceType.WOOD));
    }

    @Test
    @DisplayName("Monopoly card should log edge case when no player has the target resource")
    void monopolyCardLogsEdgeCaseWhenNoTarget() {
        HumanPlayer player = new HumanPlayer("Monopolist", PlayerColor.RED);
        HumanPlayer other1 = new HumanPlayer("Alice", PlayerColor.BLUE);

        other1.addResource(ResourceType.BRICK, 1);

        GameLog gameLog = new GameLog();
        GameState state = new MockGameState(List.of(player, other1), gameLog);

        MonopolyCard card = new MonopolyCard();
        card.setTargetResource(ResourceType.WOOD);
        card.applyEffect(state, player);

        List<LogEntry> cardEntries = gameLog.getEntriesByType(LogEntry.EventType.CARD_PLAYED);
        assertEquals(1, cardEntries.size(), "Edge case should produce one CARD_PLAYED log entry");

        String message = cardEntries.get(0).getMessage();
        assertEquals(
            "Monopolist menggunakan Monopoli Nimon: tidak ada pemain memiliki Kayu",
            message
        );
    }

    private static final class MockGameState implements GameState {
        private final List<Player> players;
        private final GameLog gameLog;

        private MockGameState(List<Player> players, GameLog gameLog) {
            this.players = players;
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
        public banana.republic.board.Board getBoard() {
            return null;
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
        public Player getCurrentPlayer() {
            return players.isEmpty() ? null : players.get(0);
        }

        @Override
        public banana.republic.card.CardDeck getCardDeck() {
            return null;
        }

        @Override
        public banana.republic.board.HexTile getRobberPosition() {
            return null;
        }
    }
}
