package banana.republic.player;

import banana.republic.card.ExperimentCard;
import banana.republic.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HumanPlayerTest {

    private HumanPlayer player;

    @BeforeEach
    void setUp() {
        player = new HumanPlayer("Kebin", PlayerColor.RED);
    }

    @Test
    void testConstructorValid() {
        assertEquals("Kebin", player.getName());
        assertEquals(PlayerColor.RED, player.getColor());
        assertFalse(player.isBot());
    }

    @Test
    void testConstructorNullName() {
        assertThrows(IllegalArgumentException.class, () -> new HumanPlayer(null, PlayerColor.BLUE));
    }

    @Test
    void testConstructorBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new HumanPlayer("   ", PlayerColor.BLUE));
    }

    @Test
    void testConstructorNullColor() {
        assertThrows(IllegalArgumentException.class, () -> new HumanPlayer("Test", null));
    }

    @Test
    void testAddAndRemoveResource() {
        player.addResource(ResourceType.WOOD, 5);
        assertEquals(5, player.getResourceCount(ResourceType.WOOD));
        assertEquals(5, player.getTotalResourceCount());

        player.removeResource(ResourceType.WOOD, 3);
        assertEquals(2, player.getResourceCount(ResourceType.WOOD));
    }

    @Test
    void testRemoveResourceInsufficient() {
        player.addResource(ResourceType.WOOD, 2);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> player.removeResource(ResourceType.WOOD, 5));
        assertTrue(ex.getMessage().contains("Cannot remove"));
    }

    @Test
    void testAddResourceNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> player.addResource(ResourceType.WOOD, -1));
    }

    @Test
    void testRemoveResourceNegative() {
        assertThrows(IllegalArgumentException.class,
            () -> player.removeResource(ResourceType.WOOD, -1));
    }

    @Test
    void testHasResource() {
        player.addResource(ResourceType.WOOD, 3);
        assertTrue(player.hasResource(ResourceType.WOOD, 3));
        assertTrue(player.hasResource(ResourceType.WOOD, 2));
        assertFalse(player.hasResource(ResourceType.WOOD, 4));
        assertFalse(player.hasResource(null, 1));
        assertFalse(player.hasResource(ResourceType.WOOD, -1));
    }

    @Test
    void testHandCards() {
        ExperimentCard card = new ExperimentCard() {
            @Override public String getCardName() { return "Knight"; }
            @Override public String getDescription() { return "A knight card"; }
            @Override public void applyEffect(banana.republic.core.GameState state, banana.republic.player.Player player) {}

            @Override
            public boolean isPlayable() {
                return true;
            }

            @Override
            public boolean isSecret() {
                return false;
            }

            @Override
            public banana.republic.card.CardType getCardType() {
                return banana.republic.card.CardType.KNIGHT;
            }
        };
        player.addCard(card);
        assertEquals(1, player.getHandCards().size());
        player.removeCard(card);
        assertEquals(0, player.getHandCards().size());
    }

    @Test
    void testRemoveCardNotInHand() {
        ExperimentCard card = new ExperimentCard() {
            @Override public String getCardName() { return "Knight"; }
            @Override public String getDescription() { return "A knight card"; }
            @Override public void applyEffect(banana.republic.core.GameState state, banana.republic.player.Player player) {}

            @Override
            public boolean isPlayable() {
                return true;
            }

            @Override
            public boolean isSecret() {
                return false;
            }

            @Override
            public banana.republic.card.CardType getCardType() {
                return banana.republic.card.CardType.KNIGHT;
            }
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> player.removeCard(card));
        assertTrue(ex.getMessage().contains("Card not found"));
    }

    @Test
    void testAddNullCard() {
        assertThrows(IllegalArgumentException.class, () -> player.addCard(null));
    }

    @Test
    void testKnightsPlayed() {
        assertEquals(0, player.getKnightsPlayed());
        player.incrementKnightsPlayed();
        assertEquals(1, player.getKnightsPlayed());
    }

    @Test
    void testLongestRoadLength() {
        player.setLongestRoadLength(5);
        assertEquals(5, player.getLongestRoadLength());
        assertThrows(IllegalArgumentException.class, () -> player.setLongestRoadLength(-1));
    }

    @Test
    void testSpecialCards() {
        assertFalse(player.hasSpecialCard(SpecialCardType.LONGEST_ROAD));
        player.setSpecialCard(SpecialCardType.LONGEST_ROAD, true);
        assertTrue(player.hasSpecialCard(SpecialCardType.LONGEST_ROAD));
        player.setSpecialCard(SpecialCardType.LONGEST_ROAD, false);
        assertFalse(player.hasSpecialCard(SpecialCardType.LONGEST_ROAD));
    }

    @Test
    void testSetSpecialCardNull() {
        assertThrows(IllegalArgumentException.class,
            () -> player.setSpecialCard(null, true));
    }

    @Test
    void testToString() {
        String s = player.toString();
        assertTrue(s.contains("HumanPlayer"));
        assertTrue(s.contains("Kebin"));
    }
}
