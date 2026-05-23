package banana.republic.resource;

import banana.republic.board.*;
import banana.republic.building.PosPantau;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceProductionServiceTest {

    private ResourceProductionService service;
    private Bank bank;
    private Player player1;
    private Player player2;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        service = new ResourceProductionService();
        bank = new BankImpl();
        player1 = new HumanPlayer("Kebin", PlayerColor.RED);
        player2 = new HumanPlayer("Stewart", PlayerColor.BLUE);
        players = List.of(player1, player2);
    }

    @Test
    void testDistributeNormalCase() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distribute(woodTile, board, players, bank);

        assertEquals(1, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(18, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeBlockedByRobber() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), true, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distribute(woodTile, board, players, bank);

        assertEquals(0, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(19, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeFiniteBankMultiplePlayersShortage() {
        // Setup: bank has only 1 wood, but 2 players each need 1
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection i1 = new Intersection(1, List.of(woodTile), new ArrayList<>());
        Intersection i2 = new Intersection(2, List.of(woodTile), new ArrayList<>());
        i1.placeBuilding(new PosPantau(player1));
        i2.placeBuilding(new PosPantau(player2));
        Board board = new Board(List.of(woodTile), List.of(i1, i2), new ArrayList<>(), new ArrayList<>());

        bank.takeResource(ResourceType.WOOD, 18); // leave only 1 in bank

        service.distribute(woodTile, board, players, bank);

        // Both need 1, total 2, but only 1 available -> nobody gets anything
        assertEquals(0, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(0, player2.getResourceCount(ResourceType.WOOD));
        assertEquals(1, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeFiniteBankSinglePlayerPartial() {
        // Setup: bank has only 1 wood, but 1 player needs 2 (has a city/lab)
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        banana.republic.building.Laboratorium lab = new banana.republic.building.Laboratorium(player1);
        Intersection i1 = new Intersection(1, List.of(woodTile), new ArrayList<>());
        i1.placeBuilding(lab); // Lab produces 2
        Board board = new Board(List.of(woodTile), List.of(i1), new ArrayList<>(), new ArrayList<>());

        bank.takeResource(ResourceType.WOOD, 18); // leave only 1 in bank

        service.distribute(woodTile, board, players, bank);

        // Single player affected, gets partial
        assertEquals(1, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(0, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeNoBuilding() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        // No building placed
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distribute(woodTile, board, players, bank);

        assertEquals(0, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(19, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeNullParameters() {
        assertThrows(IllegalArgumentException.class,
            () -> service.distribute(null, null, null, null));
    }

    @Test
    void testDistributeForRollNormal() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distributeForRoll(6, board, players, bank);

        assertEquals(1, player1.getResourceCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeForRollSeven() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        // Roll 7 should not produce anything regardless of tile tokens
        service.distributeForRoll(7, board, players, bank);

        assertEquals(0, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(19, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testDistributeForRollNullBoard() {
        assertThrows(IllegalArgumentException.class,
            () -> service.distributeForRoll(6, null, players, bank));
    }

    @Test
    void testCanDistributeNormal() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        assertTrue(service.canDistribute(woodTile, board, players, bank));
    }

    @Test
    void testCanDistributeBlockedByRobber() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), true, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        intersection.placeBuilding(new PosPantau(player1));
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        assertFalse(service.canDistribute(woodTile, board, players, bank));
    }

    @Test
    void testCanDistributeNoBuilding() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile), new ArrayList<>());
        Board board = new Board(List.of(woodTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        assertFalse(service.canDistribute(woodTile, board, players, bank));
    }

    @Test
    void testCanDistributeNullParameters() {
        assertFalse(service.canDistribute(null, null, null, null));
    }

    @Test
    void testDistributeInitialResourcesNormal() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        HexTile brickTile = new HexTile(2, TerrainType.HILL, new NumberToken(8), false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile, brickTile), new ArrayList<>());
        Board board = new Board(List.of(woodTile, brickTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distributeInitialResources(player1, intersection, bank, board);

        assertEquals(1, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(1, player1.getResourceCount(ResourceType.BRICK));
        assertEquals(18, bank.getCount(ResourceType.WOOD));
        assertEquals(18, bank.getCount(ResourceType.BRICK));
    }

    @Test
    void testDistributeInitialResourcesExcludesDesert() {
        HexTile woodTile = new HexTile(1, TerrainType.FOREST, new NumberToken(6), false, 0, 0);
        HexTile desertTile = new HexTile(2, TerrainType.DESERT, null, false, 0, 0);
        Intersection intersection = new Intersection(1, List.of(woodTile, desertTile), new ArrayList<>());
        Board board = new Board(List.of(woodTile, desertTile), List.of(intersection), new ArrayList<>(), new ArrayList<>());

        service.distributeInitialResources(player1, intersection, bank, board);

        assertEquals(1, player1.getResourceCount(ResourceType.WOOD));
        assertEquals(0, player1.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void testDistributeInitialResourcesNullParameters() {
        assertThrows(IllegalArgumentException.class,
            () -> service.distributeInitialResources(null, null, null, null));
    }
}
