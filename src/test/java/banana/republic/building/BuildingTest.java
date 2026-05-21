package banana.republic.building;

import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildingTest {

    private Player owner;

    @BeforeEach
    void setUp() {
        owner = new HumanPlayer("Kebin", PlayerColor.RED);
    }

    @Test
    void testPosPantauCreation() {
        PosPantau s = new PosPantau(owner);
        assertSame(owner, s.getOwner());
        assertEquals(1, s.getVictoryPoints());
        assertEquals(1, s.getProductionAmount());
        assertEquals(BuildingType.POS_PANTAU, s.getBuildingType());
    }

    @Test
    void testLaboratoriumCreation() {
        Laboratorium c = new Laboratorium(owner);
        assertSame(owner, c.getOwner());
        assertEquals(2, c.getVictoryPoints());
        assertEquals(2, c.getProductionAmount());
        assertEquals(BuildingType.LABORATORIUM, c.getBuildingType());
    }

    @Test
    void testRoadCreation() {
        Road r = new Road(owner);
        assertSame(owner, r.getOwner());
    }

    @Test
    void testBuildingNullOwner() {
        assertThrows(IllegalArgumentException.class, () -> new PosPantau(null));
        assertThrows(IllegalArgumentException.class, () -> new Laboratorium(null));
        assertThrows(IllegalArgumentException.class, () -> new Road(null));
    }

    @Test
    void testBuildingTypeEnum() {
        assertEquals(3, BuildingType.values().length);
        assertEquals(BuildingType.POS_PANTAU, BuildingType.valueOf("POS_PANTAU"));
        assertEquals(BuildingType.LABORATORIUM, BuildingType.valueOf("LABORATORIUM"));
        assertEquals(BuildingType.ROAD, BuildingType.valueOf("ROAD"));
    }

    @Test
    void testPlayerSupplyInitialState() {
        PlayerSupply supply = new PlayerSupply(owner);
        assertEquals(5, supply.getPosPantauRemaining());
        assertEquals(4, supply.getLaboratoriumRemaining());
        assertEquals(15, supply.getRoadsRemaining());
        assertTrue(supply.canBuildPosPantau());
        assertTrue(supply.canBuildLaboratorium());
        assertTrue(supply.canBuildRoad());
    }

    @Test
    void testPlayerSupplyTakePosPantau() {
        PlayerSupply supply = new PlayerSupply(owner);
        PosPantau s = supply.takePosPantau();
        assertNotNull(s);
        assertSame(owner, s.getOwner());
        assertEquals(4, supply.getPosPantauRemaining());
    }

    @Test
    void testPlayerSupplyTakeLaboratorium() {
        PlayerSupply supply = new PlayerSupply(owner);
        Laboratorium c = supply.takeLaboratorium();
        assertNotNull(c);
        assertSame(owner, c.getOwner());
        assertEquals(3, supply.getLaboratoriumRemaining());
    }

    @Test
    void testPlayerSupplyTakeRoad() {
        PlayerSupply supply = new PlayerSupply(owner);
        Road r = supply.takeRoad();
        assertNotNull(r);
        assertSame(owner, r.getOwner());
        assertEquals(14, supply.getRoadsRemaining());
    }

    @Test
    void testPlayerSupplyExhaustPosPantau() {
        PlayerSupply supply = new PlayerSupply(owner);
        for (int i = 0; i < 5; i++) {
            supply.takePosPantau();
        }
        assertFalse(supply.canBuildPosPantau());
        assertThrows(IllegalStateException.class, supply::takePosPantau);
    }

    @Test
    void testPlayerSupplyExhaustLaboratorium() {
        PlayerSupply supply = new PlayerSupply(owner);
        for (int i = 0; i < 4; i++) {
            supply.takeLaboratorium();
        }
        assertFalse(supply.canBuildLaboratorium());
        assertThrows(IllegalStateException.class, supply::takeLaboratorium);
    }

    @Test
    void testPlayerSupplyExhaustRoad() {
        PlayerSupply supply = new PlayerSupply(owner);
        for (int i = 0; i < 15; i++) {
            supply.takeRoad();
        }
        assertFalse(supply.canBuildRoad());
        assertThrows(IllegalStateException.class, supply::takeRoad);
    }

    @Test
    void testPlayerSupplyReturnPosPantau() {
        PlayerSupply supply = new PlayerSupply(owner);
        PosPantau s = supply.takePosPantau();
        assertEquals(4, supply.getPosPantauRemaining());
        supply.returnPosPantau(s);
        assertEquals(5, supply.getPosPantauRemaining());
    }

    @Test
    void testPlayerSupplyReturnNullPosPantau() {
        PlayerSupply supply = new PlayerSupply(owner);
        assertThrows(IllegalArgumentException.class, () -> supply.returnPosPantau(null));
    }

    @Test
    void testPlayerSupplyReturnWrongOwner() {
        Player other = new HumanPlayer("Other", PlayerColor.BLUE);
        PlayerSupply supply = new PlayerSupply(owner);
        PosPantau s = new PosPantau(other);
        assertThrows(IllegalArgumentException.class, () -> supply.returnPosPantau(s));
    }

    @Test
    void testPlayerSupplyReturnOverMax() {
        PlayerSupply supply = new PlayerSupply(owner);
        assertThrows(IllegalStateException.class, () -> supply.returnPosPantau(new PosPantau(owner)));
    }

    @Test
    void testPlayerSupplyNullOwner() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerSupply(null));
    }
}
