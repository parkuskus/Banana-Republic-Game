package banana.republic.core;

import static org.junit.jupiter.api.Assertions.*;

import banana.republic.board.Intersection;
import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Path;
import banana.republic.resource.Bank;
import banana.republic.board.Intersection;
import banana.republic.building.PosPantau;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;
import banana.republic.trade.TradeOffer;
import banana.republic.trade.ValidationResult;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    @DisplayName("startSetupPhase should log exactly one 'Fase Setup dimulai' entry (no duplicate)")
    void testStartSetupPhaseLogsSetupMessageOnce() {
        game.startSetupPhase();

        long count = game.getGameLog().getEntries().stream()
            .filter(e -> e.getMessage().contains("Fase Setup dimulai"))
            .count();

        assertEquals(1, count,
            "startSetupPhase harus menghasilkan tepat 1 log 'Fase Setup dimulai' (tidak boleh duplikat)");
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
    @DisplayName("makeTradeOffer log should include resource details")
    void testMakeTradeOfferLogIncludesResources() {
        Game normalGame = new Game(players, null);
        Game tradeGame = new Game(players, normalGame.getBoard(), normalGame.getBank(),
            normalGame.getCardDeck(), GamePhase.TRADE_BUILD, 1, 0, 0, null);

        Player alice = players.get(0);
        Player bob = players.get(1);

        alice.addResource(ResourceType.WOOD, 2);
        alice.addResource(ResourceType.BRICK, 1);
        bob.addResource(ResourceType.WHEAT, 1);

        Map<ResourceType, Integer> offer = new EnumMap<>(ResourceType.class);
        offer.put(ResourceType.WOOD, 2);
        offer.put(ResourceType.BRICK, 1);
        Map<ResourceType, Integer> request = new EnumMap<>(ResourceType.class);
        request.put(ResourceType.WHEAT, 1);

        TradeOffer tradeOffer = new TradeOffer(alice, bob, offer, request);
        ValidationResult result = tradeGame.makeTradeOffer(tradeOffer);
        assertTrue(result.isValid(), "makeTradeOffer harus valid");

        List<LogEntry> entries = tradeGame.getGameLog().getEntriesByType(LogEntry.EventType.TRADE);
        assertFalse(entries.isEmpty(), "Harus ada log entry TRADE");
        String msg = entries.get(0).getMessage();
        assertTrue(msg.contains("2 Kayu"),
            "Log harus menyebut jumlah/resource yang ditawarkan: '2 Kayu'");
        assertTrue(msg.contains("1 Batu Bata"),
            "Log harus menyebut jumlah/resource yang ditawarkan: '1 Batu Bata'");
        assertTrue(msg.contains("1 Gandum"),
            "Log harus menyebut jumlah/resource yang diminta: '1 Gandum'");
        assertTrue(msg.contains("Bob"),
            "Log harus menyebut target: Bob");
    }

    @Test
    @DisplayName("acceptTradeOffer log should include resource details")
    void testAcceptTradeOfferLogIncludesResources() {
        Game normalGame = new Game(players, null);
        Game tradeGame = new Game(players, normalGame.getBoard(), normalGame.getBank(),
            normalGame.getCardDeck(), GamePhase.TRADE_BUILD, 1, 0, 0, null);

        Player alice = players.get(0);
        Player bob = players.get(1);

        alice.addResource(ResourceType.WOOD, 2);
        bob.addResource(ResourceType.WHEAT, 1);

        Map<ResourceType, Integer> offer = new EnumMap<>(ResourceType.class);
        offer.put(ResourceType.WOOD, 2);
        Map<ResourceType, Integer> request = new EnumMap<>(ResourceType.class);
        request.put(ResourceType.WHEAT, 1);

        TradeOffer tradeOffer = new TradeOffer(alice, bob, offer, request);
        assertTrue(tradeGame.makeTradeOffer(tradeOffer).isValid(), "makeTradeOffer harus valid");

        ValidationResult result = tradeGame.acceptTradeOffer(bob);
        assertTrue(result.isValid(), "acceptTradeOffer harus valid");

        List<LogEntry> entries = tradeGame.getGameLog().getEntriesByType(LogEntry.EventType.TRADE);
        assertFalse(entries.isEmpty(), "Harus ada log entry TRADE");
        String msg = entries.get(entries.size() - 1).getMessage();
        assertTrue(msg.contains("2 Kayu"),
            "Log accept harus menyebut resource yang ditawarkan: '2 Kayu'");
        assertTrue(msg.contains("Alice"),
            "Log accept harus menyebut offerer: Alice");
    }

    @Test
    @DisplayName("counterTradeOffer log should include resource details")
    void testCounterTradeOfferLogIncludesResources() {
        Game normalGame = new Game(players, null);
        Game tradeGame = new Game(players, normalGame.getBoard(), normalGame.getBank(),
            normalGame.getCardDeck(), GamePhase.TRADE_BUILD, 1, 0, 0, null);

        Player alice = players.get(0);
        Player bob = players.get(1);

        alice.addResource(ResourceType.WOOD, 2);
        alice.addResource(ResourceType.BRICK, 1);
        bob.addResource(ResourceType.WHEAT, 1);
        bob.addResource(ResourceType.ORE, 3);

        Map<ResourceType, Integer> offer = new EnumMap<>(ResourceType.class);
        offer.put(ResourceType.WOOD, 2);
        Map<ResourceType, Integer> request = new EnumMap<>(ResourceType.class);
        request.put(ResourceType.WHEAT, 1);
        TradeOffer aliceOffer = new TradeOffer(alice, bob, offer, request);
        assertTrue(tradeGame.makeTradeOffer(aliceOffer).isValid(), "makeTradeOffer harus valid");

        Map<ResourceType, Integer> counterOffer = new EnumMap<>(ResourceType.class);
        counterOffer.put(ResourceType.ORE, 2);
        Map<ResourceType, Integer> counterRequest = new EnumMap<>(ResourceType.class);
        counterRequest.put(ResourceType.BRICK, 1);
        TradeOffer bobCounter = new TradeOffer(bob, alice, counterOffer, counterRequest);
        ValidationResult result = tradeGame.counterTradeOffer(bobCounter);
        assertTrue(result.isValid(), "counterTradeOffer harus valid");

        List<LogEntry> entries = tradeGame.getGameLog().getEntriesByType(LogEntry.EventType.TRADE);
        assertTrue(entries.size() >= 2, "Harus ada minimal 2 TRADE log entries");
        String msg = entries.get(1).getMessage();
        assertTrue(msg.contains("2 Bijih"),
            "Log counter harus menyebut resource yang ditawarkan: '2 Bijih'");
        assertTrue(msg.contains("1 Batu Bata"),
            "Log counter harus menyebut resource yang diminta: '1 Batu Bata'");
        assertTrue(msg.contains("counter-offer"),
            "Log counter harus menyebut 'counter-offer'");
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

    @Test
    @DisplayName("placeInitialSettlement in SETUP_SECOND_ROUND should log RESOURCE_PRODUCTION")
    void testPlaceInitialSettlementLogsResourceProduction() {
        Game setupGame = new Game(players, game.getBoard(), game.getBank(),
            game.getCardDeck(), GamePhase.SETUP_SECOND_ROUND, 1, 0, 3, null);
        Player alice = players.get(0);

        // Find first valid empty intersection
        Intersection intersection = null;
        for (Intersection i : setupGame.getBoard().getAllIntersections()) {
            if (i.isEmpty() && setupGame.getBoard().isDistanceRuleValid(i)) {
                intersection = i;
                break;
            }
        }
        assertNotNull(intersection, "Harus ada intersection yang valid");

        setupGame.placeInitialSettlement(alice, intersection);

        List<LogEntry> productionEntries = setupGame.getGameLog()
            .getEntriesByType(LogEntry.EventType.RESOURCE_PRODUCTION);
        assertFalse(productionEntries.isEmpty(),
            "Harus ada RESOURCE_PRODUCTION log entry saat SETUP_SECOND_ROUND");

        String msg = productionEntries.get(0).getMessage();
        assertTrue(msg.contains("Resource awal dari Pos Pantau kedua"),
            "Log RESOURCE_PRODUCTION harus menyebut 'Resource awal dari Pos Pantau kedua'");
    }

    @Test
    @DisplayName("rollDice should log RESOURCE_PRODUCTION for each player receiving resources")
    void testRollDiceLogsResourceProduction() {
        // Create a reference game to get a valid board
        Game refGame = new Game(players, null);
        Board board = refGame.getBoard();

        // Find a non-desert tile with a known number token (4 is common on standard map)
        List<HexTile> tiles = board.getTilesWithToken(4);
        assertFalse(tiles.isEmpty(), "Board harus memiliki tile dengan token 4");

        // Pick first tile that can produce (not desert, no robber)
        HexTile targetTile = null;
        for (HexTile tile : tiles) {
            if (tile.canProduce()) {
                targetTile = tile;
                break;
            }
        }
        assertNotNull(targetTile, "Harus ada tile non-desert dengan token 4");

        // Find an adjacent intersection for building placement
        List<Intersection> adjIntersections = board.getAdjacentIntersections(targetTile);
        assertFalse(adjIntersections.isEmpty(), "Tile harus memiliki adjacent intersections");

        Intersection targetIntersection = adjIntersections.get(0);

        // Create test game at RESOURCE_GATHERING phase with same board
        Game testGame = new Game(players, board, refGame.getBank(), refGame.getCardDeck(),
            GamePhase.RESOURCE_GATHERING, 1, 0, 0, null);

        // Place a PosPantau for Alice on the target intersection
        Player alice = players.get(0);
        PosPantau settlement = new PosPantau(alice);
        targetIntersection.placeBuilding(settlement);

        // Set dice to manual mode matching the tile's token (2+2=4)
        testGame.getDice().setManualMode(true);
        testGame.getDice().setManualValues(2, 2);

        // Roll the dice
        testGame.rollDice();

        // Verify RESOURCE_PRODUCTION log entries exist
        List<LogEntry> productionEntries = testGame.getGameLog()
            .getEntriesByType(LogEntry.EventType.RESOURCE_PRODUCTION);
        assertFalse(productionEntries.isEmpty(),
            "rollDice harus menghasilkan log RESOURCE_PRODUCTION untuk pemain yang mendapat resource");

        String msg = productionEntries.get(0).getMessage();
        assertTrue(msg.contains("menerima"),
            "Log RESOURCE_PRODUCTION harus menyebut 'menerima': " + msg);
        assertTrue(msg.contains("dari lemparan dadu"),
            "Log RESOURCE_PRODUCTION harus menyebut 'dari lemparan dadu': " + msg);
        assertTrue(msg.contains("Alice"),
            "Log RESOURCE_PRODUCTION harus menyebut nama pemain: Alice");
    }

    @Test
    @DisplayName("Build logs should include cost info (biaya) - Bug #10")
    void testBuildLogsIncludeBiaya() {
        // Setup: create game and place initial settlement + road for Alice
        Game setupGame = new Game(players, null);
        Board board = setupGame.getBoard();
        Player alice = players.get(0);

        // Find a valid intersection for initial settlement
        Intersection settlementA = null;
        for (Intersection i : board.getAllIntersections()) {
            if (i.isEmpty() && board.isDistanceRuleValid(i)) {
                settlementA = i;
                break;
            }
        }
        assertNotNull(settlementA, "Need valid intersection for initial settlement");
        setupGame.placeInitialSettlement(alice, settlementA);

        // Place initial road from A to B
        Path pathAB = settlementA.getAdjacentPaths().get(0);
        assertNotNull(pathAB, "Need valid adjacent path for initial road");
        setupGame.placeInitialRoad(alice, pathAB);

        // Find B (the other endpoint of pathAB)
        Intersection B = pathAB.getIntersectionA().equals(settlementA)
            ? pathAB.getIntersectionB()
            : pathAB.getIntersectionA();

        // Create game in TRADE_BUILD phase with the same board (which now has buildings)
        Game testGame = new Game(players, board, setupGame.getBank(),
            setupGame.getCardDeck(), GamePhase.TRADE_BUILD, 1, 0, 0, null);

        // Build default card deck (not done by Game constructor)
        testGame.getCardDeck().buildDefaultDeck();

        // Drain bank so returned build resources don't exceed capacity
        Bank bank = testGame.getBank();
        bank.takeResource(ResourceType.WOOD, 5);
        bank.takeResource(ResourceType.BRICK, 5);
        bank.takeResource(ResourceType.WHEAT, 5);
        bank.takeResource(ResourceType.BANANA, 5);
        bank.takeResource(ResourceType.ORE, 5);

        // Give Alice enough resources for all build actions
        alice.addResource(ResourceType.WOOD, 3);
        alice.addResource(ResourceType.BRICK, 3);
        alice.addResource(ResourceType.WHEAT, 5);
        alice.addResource(ResourceType.BANANA, 3);
        alice.addResource(ResourceType.ORE, 4);

        // 1. Build Road on path B-C (connected to Alice's network via pathAB)
        Path roadBC = null;
        for (Path p : B.getAdjacentPaths()) {
            if (!p.hasRoad() && !p.equals(pathAB)) {
                roadBC = p;
                break;
            }
        }
        assertNotNull(roadBC, "Need a path from B for road build");
        testGame.buildRoad(alice, roadBC);

        // Find C (the other endpoint of roadBC)
        Intersection C = roadBC.getIntersectionA().equals(B)
            ? roadBC.getIntersectionB()
            : roadBC.getIntersectionA();

        // 2. Build Settlement at C (must be empty and pass distance rule)
        assertTrue(C.isEmpty(), "Intersection C must be empty");
        assertTrue(board.isDistanceRuleValid(C), "C must pass distance rule");
        testGame.buildSettlement(alice, C);

        // 3. Build City at C (upgrade the settlement just built)
        testGame.buildCity(alice, C);

        // 4. Buy Development Card
        testGame.buyDevelopmentCard(alice);

        // Verify all BUILD log entries contain "biaya"
        List<LogEntry> buildEntries = testGame.getGameLog()
            .getEntriesByType(LogEntry.EventType.BUILD);
        assertEquals(3, buildEntries.size(),
            "Should have 3 BUILD entries (road, settlement, city)");
        long biayaBuildCount = buildEntries.stream()
            .filter(e -> e.getMessage().contains("biaya"))
            .count();
        assertEquals(3, biayaBuildCount,
            "All BUILD entries should contain 'biaya'");

        // Verify CARD_BOUGHT log entry contains "biaya"
        List<LogEntry> cardEntries = testGame.getGameLog()
            .getEntriesByType(LogEntry.EventType.CARD_BOUGHT);
        assertEquals(1, cardEntries.size(),
            "Should have 1 CARD_BOUGHT entry");
        assertTrue(cardEntries.get(0).getMessage().contains("biaya"),
            "CARD_BOUGHT entry should contain 'biaya'");
    }

    @Test
    @DisplayName("rollDice should log resource shortage to GameLog when bank is insufficient")
    void testRollDiceLogsShortage() {
        Game refGame = new Game(players, null);
        Board board = refGame.getBoard();
        Bank bank = refGame.getBank();

        List<HexTile> tiles = board.getTilesWithToken(4);
        HexTile targetTile = null;
        for (HexTile tile : tiles) {
            if (tile.canProduce()) {
                targetTile = tile;
                break;
            }
        }
        assertNotNull(targetTile, "Harus ada tile non-desert dengan token 4");

        List<Intersection> adjIntersections = board.getAdjacentIntersections(targetTile);
        assertTrue(adjIntersections.size() >= 2,
            "Tile harus memiliki minimal 2 adjacent intersections");

        Intersection i1 = adjIntersections.get(0);
        Intersection i2 = adjIntersections.get(1);
        Player alice = players.get(0);
        Player bob = players.get(1);
        i1.placeBuilding(new PosPantau(alice));
        i2.placeBuilding(new PosPantau(bob));

        ResourceType rt = targetTile.getResourceType();
        bank.takeResource(rt, 18);

        Game testGame = new Game(players, board, bank, refGame.getCardDeck(),
            GamePhase.RESOURCE_GATHERING, 1, 0, 0, null);

        testGame.getDice().setManualMode(true);
        testGame.getDice().setManualValues(2, 2);

        testGame.rollDice();

        List<LogEntry> productionEntries = testGame.getGameLog()
            .getEntriesByType(LogEntry.EventType.RESOURCE_PRODUCTION);
        boolean hasShortageEntry = productionEntries.stream()
            .anyMatch(e -> e.getMessage().contains("Resource shortage"));
        assertTrue(hasShortageEntry,
            "rollDice harus mencatat resource shortage ke GameLog ketika bank tidak mencukupi");
    }
}
