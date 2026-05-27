package banana.republic.core;

import static org.junit.jupiter.api.Assertions.*;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.board.TerrainType;
import banana.republic.building.PosPantau;
import banana.republic.building.Road;
import banana.republic.card.ExperimentCard;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests untuk Game (orchestrator utama). 1. Constructor validation 2.
 * Setup phase mechanics 3. Main game mechanics (roll dice, build, trade) 4.
 * Turn management 5. Resource management
 */
@DisplayName("Game Tests")
public class GameTest {

    private Game game;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new HumanPlayer("Alice", PlayerColor.RED));
        players.add(new HumanPlayer("Bob", PlayerColor.BLUE));
        players.add(new HumanPlayer("Charlie", PlayerColor.ORANGE));

        // Create game with default map generator
        game = new Game(players, null);
    }

    @Test
    @DisplayName("Constructor should reject null players")
    void testConstructorRejectsNullPlayers() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(null, null);
        }, "Constructor harus reject null players");
    }

    @Test
    @DisplayName("Constructor should reject empty players list")
    void testConstructorRejectsEmptyPlayers() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(new ArrayList<>(), null);
        }, "Constructor harus reject empty players list");
    }

    @Test
    @DisplayName("Constructor should reject less than 3 players")
    void testConstructorRejectsTooFewPlayers() {
        List<Player> twoPlayers =
            List.of(new HumanPlayer("P1", PlayerColor.RED),
                    new HumanPlayer("P2", PlayerColor.BLUE));
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(twoPlayers, null);
        }, "Constructor harus reject < 3 players");
    }

    @Test
    @DisplayName("Constructor should reject more than 4 players")
    void testConstructorRejectsTooManyPlayers() {
        List<Player> fivePlayers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            fivePlayers.add(new HumanPlayer("P" + i, PlayerColor.RED));
        }
        assertThrows(IllegalArgumentException.class, () -> {
            new Game(fivePlayers, null);
        }, "Constructor harus reject > 4 players");
    }

    @Test
    @DisplayName("Game should initialize with correct defaults")
    void testGameInitialization() {
        assertNotNull(game.getBoard(), "Board harus terinisialisasi");
        assertNotNull(game.getBank(), "Bank harus terinisialisasi");
        assertNotNull(game.getCardDeck(), "CardDeck harus terinisialisasi");
        assertNotNull(game.getDice(), "Dice harus terinisialisasi");
        assertNotNull(game.getRobber(), "Robber harus terinisialisasi");
        assertNotNull(game.getGameLog(), "GameLog harus terinisialisasi");
        assertNotNull(game.getTurnManager(),
                      "TurnManager harus terinisialisasi");

        assertEquals(GamePhase.SETUP_FIRST_ROUND, game.getCurrentPhase(),
                     "Fase awal harus SETUP_FIRST_ROUND");
        assertEquals(1, game.getTurnNumber(), "Turn number awal harus 1");
        assertNull(game.getWinner(), "Winner awal harus null");
        assertNull(game.getLastDiceResult(),
                   "Last dice result awal harus null");
    }

    @Test
    @DisplayName("getState should return GameState adapter")
    void testGetStateReturnsAdapter() {
        GameState state = game.getState();
        assertNotNull(state, "getState harus return non-null");
        assertSame(state, game.getState(),
                   "getState harus return instance yang sama (singleton)");
    }

    @Test
    @DisplayName("startSetupPhase should set phase correctly")
    void testStartSetupPhase() {
        game.startSetupPhase();

        assertTrue(game.getCurrentPhase().isSetupPhase(),
                   "Setelah startSetupPhase, phase harus setup phase");
        assertNotNull(game.getActivePlayer(),
                      "Harus ada active player setelah setup");
    }

    @Test
    @DisplayName("getPlayers should return unmodifiable list")
    void testGetPlayersReturnsUnmodifiableList() {
        List<Player> returnedPlayers = game.getPlayers();
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedPlayers.add(new HumanPlayer("New", PlayerColor.RED));
        }, "getPlayers harus return unmodifiable list");
    }

    @Test
    @DisplayName("getSupply should return supply for each player")
    void testGetSupplyForEachPlayer() {
        for (Player player : players) {
            assertNotNull(game.getSupply(player),
                          "getSupply harus return non-null untuk " +
                              player.getName());
        }
    }

    @Test
    @DisplayName("getSupply should return null for unknown player")
    void testGetSupplyUnknownPlayer() {
        Player unknown = new HumanPlayer("Unknown", PlayerColor.ORANGE);
        assertNull(
            game.getSupply(unknown),
            "getSupply harus return null untuk player yang tidak dikenal");
    }

    @Test
    @DisplayName("Victory points constant should be 10")
    void testVictoryPointsConstant() {
        assertEquals(10, Game.VICTORY_POINTS_TO_WIN,
                     "VICTORY_POINTS_TO_WIN harus 10");
    }

    @Test
    @DisplayName("Hand limit constant should be 7")
    void testHandLimitConstant() {
        assertEquals(7, Game.HAND_LIMIT, "HAND_LIMIT harus 7");
    }

    @Test
    @DisplayName("activateRobber should throw IllegalArgumentException for null target")
    void testActivateRobberRejectsNullTarget() {
        assertThrows(IllegalArgumentException.class, () -> {
            game.activateRobber(null, null);
        }, "activateRobber harus throw IllegalArgumentException untuk null target");
    }

    @Test
    @DisplayName("processDiscardPhase should work without errors")
    void testProcessDiscardPhase() {
        // Should not throw any exception
        assertDoesNotThrow(() -> game.processDiscardPhase(),
            "processDiscardPhase harus berjalan tanpa error");
    }

    @Test
    @DisplayName("playCard should throw IllegalStateException when not in TRADE_BUILD phase")
    void testPlayCardWrongPhase() {
        assertThrows(IllegalStateException.class, () -> {
            game.playCard(null, null);
        }, "playCard harus throw IllegalStateException saat bukan fase TRADE_BUILD");
    }

    @Test
    @DisplayName("checkVictory should work without errors")
    void testCheckVictory() {
        // Should not throw any exception
        assertDoesNotThrow(() -> game.checkVictory(),
            "checkVictory harus berjalan tanpa error");
    }

    @Test
    @DisplayName("saveGame should throw UnsupportedOperationException")
    void testSaveGameNotImplemented() {
        assertThrows(UnsupportedOperationException.class, () -> {
            game.saveGame("test.save");
        }, "saveGame harus throw UnsupportedOperationException");
    }

    @Test
    @DisplayName("loadGame should throw UnsupportedOperationException")
    void testLoadGameNotImplemented() {
        assertThrows(UnsupportedOperationException.class, () -> {
            Game.loadGame("test.save");
        }, "loadGame harus throw UnsupportedOperationException");
    }

    @Test
    @DisplayName("Game should have proper toString representation")
    void testToString() {
        String str = game.toString();
        assertNotNull(str);
        assertTrue(str.contains("Game["), "toString harus mengandung 'Game['");
        assertTrue(str.contains("phase="),
                   "toString harus mengandung 'phase='");
        assertTrue(str.contains("turn="), "toString harus mengandung 'turn='");
        assertTrue(str.contains("players=3"),
                   "toString harus mengandung jumlah players");
    }
}
