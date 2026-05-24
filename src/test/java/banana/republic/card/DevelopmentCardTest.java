package banana.republic.card;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;

/**
 * Unit tests untuk DevelopmentCard dan subclasses.
 * Memverifikasi:
 * 1. Card state (revealed, newlyDrawn)
 * 2. Card playability rules
 * 3. Secret/Revealed status
 */
@DisplayName("DevelopmentCard Tests")
public class DevelopmentCardTest {
    private DevelopmentCard knightCard;
    private DevelopmentCard roadCard;
    private DevelopmentCard monopolyCard;
    private DevelopmentCard vpCard;

    // Mock objects untuk testing
    private MockGameState mockGameState;
    private MockPlayer mockPlayer;

    @BeforeEach
    void setUp() {
        knightCard = new KnightCard();
        roadCard = new RoadBuildingCard();
        monopolyCard = new MonopolyCard();
        vpCard = new VictoryPointCard();

        mockGameState = new MockGameState();
        mockPlayer = new MockPlayer("Test Player");
    }

    @Test
    @DisplayName("New card should be unmarked as revealed")
    void testNewCardNotRevealed() {
        assertFalse(knightCard.isRevealed(), "Kartu baru harus belum terungkap");
    }

    @Test
    @DisplayName("New card should be marked as newly drawn")
    void testNewCardIsNewlyDrawn() {
        assertTrue(knightCard.isNewlyDrawn(), "Kartu baru harus newlyDrawn = true");
    }

    @Test
    @DisplayName("Card reveal should update isRevealed()")
    void testCardReveal() {
        knightCard.reveal();
        assertTrue(knightCard.isRevealed(), "Setelah reveal(), isRevealed harus true");
    }

    @Test
    @DisplayName("Knight card should not be playable when newly drawn")
    void testKnightNotPlayableWhenNew() {
        assertFalse(knightCard.isPlayable(), "Knight baru tidak boleh langsung dimainkan");
    }

    @Test
    @DisplayName("Progress card should not be playable when newly drawn")
    void testProgressCardNotPlayableWhenNew() {
        assertFalse(roadCard.isPlayable(), "Progress card baru tidak bisa dimainkan");
    }

    @Test
    @DisplayName("Progress card should be playable after turn")
    void testProgressCardPlayableAfterTurn() {
        roadCard.setNewlyDrawn(false);
        assertTrue(roadCard.isPlayable(), "Progress card playable setelah 1 turn");
    }

    @Test
    @DisplayName("Victory point card should not be playable when newly drawn")
    void testVPCardNotPlayableWhenNew() {
        assertFalse(vpCard.isPlayable(), "VP card baru tidak boleh langsung dimainkan");
    }

    @Test
    @DisplayName("Victory point card should always be secret")
    void testVPCardAlwaysSecret() {
        assertTrue(vpCard.isSecret(), "VP card selalu secret sebelum reveal");
        vpCard.reveal();
        assertTrue(vpCard.isSecret(), "VP card selalu secret bahkan setelah reveal");
    }

    @Test
    @DisplayName("Knight card should be secret until revealed")
    void testKnightSecretUntilRevealed() {
        assertTrue(knightCard.isSecret(), "Knight secret sebelum dimainkan");
        knightCard.reveal();
        assertFalse(knightCard.isSecret(), "Knight tidak secret setelah dimainkan");
    }

    @Test
    @DisplayName("Knight card should increment player knights count")
    void testKnightIncrementCount() {
        assertEquals(0, mockPlayer.getKnightsPlayed());
        knightCard.applyEffect(mockGameState, mockPlayer);
        assertEquals(1, mockPlayer.getKnightsPlayed());
    }

    // ============ Mock Classes untuk Testing ============

    private static class MockGameState implements GameState {
        @Override
        public List<Player> getAllPlayers() {
            return List.of();
        }

        @Override
        public Bank getBank() {
            return new MockBank();
        }

        @Override
        public banana.republic.board.Board getBoard() {
            return null;
        }
    }

    private static class MockPlayer implements Player {
        private final String name;
        private int knightsPlayed = 0;
        private int resourceCount = 0;

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
            return resourceCount;
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
            resourceCount -= amount;
        }

        @Override
        public boolean hasResource(ResourceType type, int amount) {
            return resourceCount >= amount;
        }

        @Override
        public List<ExperimentCard> getHandCards() {
            return List.of();
        }

        @Override
        public void addCard(ExperimentCard card) {
        }

        @Override
        public void removeCard(ExperimentCard card) {
        }

        @Override
        public int getKnightsPlayed() {
            return knightsPlayed;
        }

        @Override
        public void incrementKnightsPlayed() {
            knightsPlayed++;
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

    private static class MockBank implements Bank {
        @Override
        public boolean hasResource(ResourceType type, int amount) {
            return true;
        }

        @Override
        public void takeResource(ResourceType type, int amount) {
        }

        @Override
        public void returnResource(ResourceType type, int amount) {
        }

        @Override
        public int getCount(ResourceType type) {
            return 19;
        }

        @Override
        public boolean canFulfillAll(java.util.Map<ResourceType, Integer> requests) {
            return true;
        }
    }
}



