package banana.republic.robber;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.TerrainType;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

/**
 * Unit tests untuk Robber (Nimon Ungu).
 * Memverifikasi:
 * 1. Robber initialization dan positioning
 * 2. Move mechanics
 * 3. Eligible victims calculation
 * 4. Discard phase detection
 */
@DisplayName("Robber Tests")
public class RobberTest {
    private Robber robber;
    private HexTile desertTile;
    private HexTile forestTile;
    private MockPlayer thief;
    private MockPlayer victim;

    @BeforeEach
    void setUp() {
        // Create desert tile untuk starting position
        desertTile = new HexTile(0, TerrainType.DESERT, null, false, 0, 0);
        forestTile = new HexTile(1, TerrainType.FOREST, null, false, 1, 1);

        robber = new Robber(desertTile);
        thief = new MockPlayer("Thief");
        victim = new MockPlayer("Victim");
    }

    @Test
    @DisplayName("Robber should start at desert tile")
    void testRobberStartAtDesert() {
        assertEquals(desertTile, robber.getCurrentTile(),
                "Robber harus mulai di tile gurun");
        assertTrue(desertTile.hasRobber(),
                "Desert tile harus menandai hasRobber = true");
    }

    @Test
    @DisplayName("Robber move should update current tile")
    void testRobberMove() {
        robber.move(forestTile);

        assertEquals(forestTile, robber.getCurrentTile(),
                "Robber harus pindah ke forest tile");
        assertTrue(forestTile.hasRobber(),
                "Forest tile harus menandai hasRobber = true");
        assertFalse(desertTile.hasRobber(),
                "Desert tile harus menandai hasRobber = false");
    }

    @Test
    @DisplayName("Robber move should not allow same tile")
    void testRobberMoveSameTile() {
        assertThrows(AssertionError.class, () -> {
            robber.move(desertTile);
        }, "Robber tidak boleh tetap di tile yang sama");
    }

    @Test
    @DisplayName("Discard phase should identify players with >7 cards")
    void testDiscardPhaseThresholdCalculation() {
        MockPlayer player1 = new MockPlayer("P1");
        MockPlayer player2 = new MockPlayer("P2");
        MockPlayer player3 = new MockPlayer("P3");

        // P1: 5 kartu (tidak perlu discard)
        player1.resourceCount = 5;

        // P2: 8 kartu (harus discard 4)
        player2.resourceCount = 8;

        // P3: 15 kartu (harus discard 7)
        player3.resourceCount = 15;

        List<Player> players = List.of(player1, player2, player3);
        Map<Player, Integer> discardThresholds = robber.activateDiscardPhase(players);

        assertFalse(discardThresholds.containsKey(player1),
                "P1 (5 kartu) tidak perlu discard");
        assertEquals(4, discardThresholds.get(player2),
                "P2 (8 kartu) harus discard 4");
        assertEquals(7, discardThresholds.get(player3),
                "P3 (15 kartu) harus discard 7");
    }

    @Test
    @DisplayName("Discard threshold should use floor division")
    void testDiscardPhaseFloorDivision() {
        MockPlayer player = new MockPlayer("P");
        player.resourceCount = 9; // 9/2 = 4 (floor)

        List<Player> players = List.of(player);
        Map<Player, Integer> discardThresholds = robber.activateDiscardPhase(players);

        assertEquals(4, discardThresholds.get(player),
                "Discard calculation harus floor(9/2) = 4");
    }

    @Test
    @DisplayName("Steal should transfer resource from victim to thief")
    void testStealTransfersResource() {
        victim.addResource(ResourceType.WOOD, 3);
        thief.addResource(ResourceType.WOOD, 1);

        // Steal dari victim
        int victimBefore = victim.getTotalResourceCount();
        int thiefBefore = thief.getTotalResourceCount();

        ResourceType stolen = robber.stealRandomResource(thief, victim);

        assertEquals(victimBefore - 1, victim.getTotalResourceCount(),
                "Victim harus kehilangan 1 kartu");
        assertEquals(thiefBefore + 1, thief.getTotalResourceCount(),
                "Thief harus mendapat 1 kartu");
        assertNotNull(stolen, "Stolen resource harus valid");
    }

    @Test
    @DisplayName("Steal from empty victim should not be allowed")
    void testStealFromEmptyVictim() {
        victim.resourceCount = 0;

        assertThrows(AssertionError.class, () -> {
            robber.stealRandomResource(thief, victim);
        }, "Tidak boleh steal dari victim tanpa kartu");
    }

    // ============ Mock Player untuk Testing ============
    private static class MockPlayer implements Player {
        private final String name;
        int resourceCount = 0;

        public MockPlayer(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public banana.republic.player.PlayerColor getColor() {
            return null;
        }

        @Override
        public int getResourceCount(ResourceType type) {
            return resourceCount; // Simplified: total count
        }

        @Override
        public int getTotalResourceCount() {
            return resourceCount;
        }

        @Override
        public void addResource(ResourceType type, int amount) {
            resourceCount += amount;
        }

        @Override
        public void removeResource(ResourceType type, int amount) {
            resourceCount -= Math.min(amount, resourceCount);
        }

        @Override
        public boolean hasResource(ResourceType type, int amount) {
            return resourceCount >= amount;
        }

        @Override
        public List<banana.republic.card.ExperimentCard> getHandCards() {
            return List.of();
        }

        @Override
        public void addCard(banana.republic.card.ExperimentCard card) {
        }

        @Override
        public void removeCard(banana.republic.card.ExperimentCard card) {
        }

        @Override
        public int getKnightsPlayed() {
            return 0;
        }

        @Override
        public void incrementKnightsPlayed() {
        }

        @Override
        public int getLongestRoadLength() {
            return 0;
        }

        @Override
        public void setLongestRoadLength(int length) {
        }

        @Override
        public boolean hasSpecialCard(banana.republic.player.SpecialCardType type) {
            return false;
        }

        @Override
        public void setSpecialCard(banana.republic.player.SpecialCardType type, boolean owned) {
        }

        @Override
        public boolean isBot() {
            return false;
        }
    }
}

