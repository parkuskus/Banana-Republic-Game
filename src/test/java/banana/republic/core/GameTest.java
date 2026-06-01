package banana.republic.core;

import static org.junit.jupiter.api.Assertions.*;

import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.Bank;
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

    // ============================================================
    // Build functionality tests (end-to-end through Game)
    // ============================================================

    private void completeSetup() {
        game.startSetupPhase();
        for (int round = 0; round < 2; round++) {
            for (Player player : players) {
                Intersection s = game.getBoard().getAllIntersections().stream()
                    .filter(i -> !i.hasBuilding() && game.getBoard().isDistanceRuleValid(i))
                    .findFirst().orElse(null);
                assertNotNull(s, "Harus ada intersection kosong untuk setup");
                game.placeInitialSettlement(player, s);
                Path r = s.getAdjacentPaths().stream()
                    .filter(Path::isEmpty).findFirst().orElse(null);
                assertNotNull(r, "Harus ada path kosong untuk road");
                game.placeInitialRoad(player, r);
            }
        }
        assertEquals(GamePhase.RESOURCE_GATHERING, game.getCurrentPhase(), "Setup harus selesai dan masuk RESOURCE_GATHERING");
    }

    private void enterTradeBuildPhase() {
        if (game.getCurrentPhase() != GamePhase.RESOURCE_GATHERING) {
            throw new IllegalStateException("Harus di RESOURCE_GATHERING untuk roll dice");
        }
        game.getDice().setManualMode(true);
        game.getDice().setManualValues(3, 4);
        game.rollDice();
        game.getDice().setManualMode(false);
        assertEquals(GamePhase.TRADE_BUILD, game.getCurrentPhase(), "Setelah roll dice harus masuk TRADE_BUILD");
    }

    private void giveResourceFromBank(Player p, ResourceType type, int amount) {
        Bank bank = game.getBank();
        int available = bank.getCount(type);
        int toGive = Math.min(amount, available);
        if (toGive > 0) {
            bank.takeResource(type, toGive);
            p.addResource(type, toGive);
        }
    }

    @Test
    @DisplayName("buildRoad should throw IllegalStateException when not in TRADE_BUILD")
    void testBuildRoadWrongPhase() {
        // Default phase is SETUP_FIRST_ROUND
        Path anyPath = game.getBoard().getAllPaths().get(0);
        assertThrows(IllegalStateException.class, () -> {
            game.buildRoad(players.get(0), anyPath);
        }, "buildRoad harus throw IllegalStateException saat bukan TRADE_BUILD");
    }

    @Test
    @DisplayName("buildSettlement should throw IllegalStateException when not in TRADE_BUILD")
    void testBuildSettlementWrongPhase() {
        Intersection anyIntersection = game.getBoard().getAllIntersections().get(0);
        assertThrows(IllegalStateException.class, () -> {
            game.buildSettlement(players.get(0), anyIntersection);
        }, "buildSettlement harus throw IllegalStateException saat bukan TRADE_BUILD");
    }

    @Test
    @DisplayName("buildCity should throw IllegalStateException when not in TRADE_BUILD")
    void testBuildCityWrongPhase() {
        Intersection anyIntersection = game.getBoard().getAllIntersections().get(0);
        assertThrows(IllegalStateException.class, () -> {
            game.buildCity(players.get(0), anyIntersection);
        }, "buildCity harus throw IllegalStateException saat bukan TRADE_BUILD");
    }

    @Test
    @DisplayName("buyDevelopmentCard should throw IllegalStateException when not in TRADE_BUILD")
    void testBuyDevelopmentCardWrongPhase() {
        assertThrows(IllegalStateException.class, () -> {
            game.buyDevelopmentCard(players.get(0));
        }, "buyDevelopmentCard harus throw IllegalStateException saat bukan TRADE_BUILD");
    }

    @Test
    @DisplayName("buildRoad should deduct resources correctly in TRADE_BUILD")
    void testBuildRoadDeductsResources() {
        Player p = players.get(0);
        completeSetup();

        int woodBefore = p.getResourceCount(ResourceType.WOOD);
        int brickBefore = p.getResourceCount(ResourceType.BRICK);
        giveResourceFromBank(p, ResourceType.WOOD, 5);
        giveResourceFromBank(p, ResourceType.BRICK, 5);

        enterTradeBuildPhase();

        Path buildablePath = game.getBoard().getBuildableRoadPaths(p).stream()
            .findFirst().orElse(null);
        assertNotNull(buildablePath, "Harus ada path yang bisa dibangun");

        game.buildRoad(p, buildablePath);

        assertEquals(woodBefore + Math.min(5, game.getBank().getCount(ResourceType.WOOD) + 1) - 1, p.getResourceCount(ResourceType.WOOD), "Wood harus berkurang 1");
        assertEquals(brickBefore + Math.min(5, game.getBank().getCount(ResourceType.BRICK) + 1) - 1, p.getResourceCount(ResourceType.BRICK), "Brick harus berkurang 1");
    }

    @Test
    @DisplayName("buildSettlement should deduct resources correctly in TRADE_BUILD")
    void testBuildSettlementDeductsResources() {
        Player p = players.get(0);
        completeSetup();

        // Bangun road tambahan untuk membuka intersection buildable
        giveResourceFromBank(p, ResourceType.WOOD, 10);
        giveResourceFromBank(p, ResourceType.BRICK, 10);

        enterTradeBuildPhase();

        // Bangun road sebanyak mungkin untuk membuka buildable intersections
        List<Path> buildableRoads = game.getBoard().getBuildableRoadPaths(p);
        for (Path path : buildableRoads) {
            if (p.hasResource(ResourceType.WOOD, 1) && p.hasResource(ResourceType.BRICK, 1)) {
                game.buildRoad(p, path);
            }
        }

        Intersection target = game.getBoard().getBuildableSettlements(p).stream()
            .findFirst().orElse(null);
        if (target != null) {
            int woodBefore = p.getResourceCount(ResourceType.WOOD);
            int brickBefore = p.getResourceCount(ResourceType.BRICK);
            int wheatBefore = p.getResourceCount(ResourceType.WHEAT);
            int bananaBefore = p.getResourceCount(ResourceType.BANANA);

            giveResourceFromBank(p, ResourceType.WOOD, 5);
            giveResourceFromBank(p, ResourceType.BRICK, 5);
            giveResourceFromBank(p, ResourceType.WHEAT, 5);
            giveResourceFromBank(p, ResourceType.BANANA, 5);

            game.buildSettlement(p, target);

            assertEquals(woodBefore + Math.min(5, game.getBank().getCount(ResourceType.WOOD) + 1) - 1, p.getResourceCount(ResourceType.WOOD));
            assertEquals(brickBefore + Math.min(5, game.getBank().getCount(ResourceType.BRICK) + 1) - 1, p.getResourceCount(ResourceType.BRICK));
            assertEquals(wheatBefore + Math.min(5, game.getBank().getCount(ResourceType.WHEAT) + 1) - 1, p.getResourceCount(ResourceType.WHEAT));
            assertEquals(bananaBefore + Math.min(5, game.getBank().getCount(ResourceType.BANANA) + 1) - 1, p.getResourceCount(ResourceType.BANANA));
        }
    }

    @Test
    @DisplayName("buildCity should upgrade settlement and deduct resources correctly")
    void testBuildCityDeductsResources() {
        Player p = players.get(0);
        completeSetup();

        Intersection owned = game.getBoard().getBuildableCities(p).stream()
            .findFirst().orElse(null);
        assertNotNull(owned, "Harus ada settlement milik pemain untuk di-upgrade");

        giveResourceFromBank(p, ResourceType.WHEAT, 5);
        giveResourceFromBank(p, ResourceType.ORE, 5);

        enterTradeBuildPhase();

        int wheatBefore = p.getResourceCount(ResourceType.WHEAT);
        int oreBefore = p.getResourceCount(ResourceType.ORE);

        game.buildCity(p, owned);

        assertEquals(wheatBefore - 2, p.getResourceCount(ResourceType.WHEAT));
        assertEquals(oreBefore - 3, p.getResourceCount(ResourceType.ORE));
        assertEquals(banana.republic.building.BuildingType.LABORATORIUM,
                     owned.getBuilding().getBuildingType(),
                     "Bangunan harus menjadi Laboratorium");
    }

    @Test
    @DisplayName("buyDevelopmentCard should deduct resources correctly in TRADE_BUILD")
    void testBuyDevelopmentCardDeductsResources() {
        Player p = players.get(0);
        giveResourceFromBank(p, ResourceType.ORE, 3);
        giveResourceFromBank(p, ResourceType.WHEAT, 3);
        giveResourceFromBank(p, ResourceType.BANANA, 3);

        completeSetup();
        enterTradeBuildPhase();

        int oreBefore = p.getResourceCount(ResourceType.ORE);
        int wheatBefore = p.getResourceCount(ResourceType.WHEAT);
        int bananaBefore = p.getResourceCount(ResourceType.BANANA);
        int cardsBefore = p.getHandCards().size();

        game.buyDevelopmentCard(p);

        assertEquals(oreBefore - 1, p.getResourceCount(ResourceType.ORE));
        assertEquals(wheatBefore - 1, p.getResourceCount(ResourceType.WHEAT));
        assertEquals(bananaBefore - 1, p.getResourceCount(ResourceType.BANANA));
        assertEquals(cardsBefore + 1, p.getHandCards().size());
    }
}
