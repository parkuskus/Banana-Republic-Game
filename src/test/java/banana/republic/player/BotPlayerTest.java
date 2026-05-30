package banana.republic.player;

import banana.republic.board.HexTile;
import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.resource.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BotPlayerTest {

    private PlayerStrategy dummyStrategy;

    @BeforeEach
    void setUp() {
        dummyStrategy = new PlayerStrategy() {
            @Override
            public List<Action> takeTurn(GameState state) {
                return Collections.emptyList();
            }

            @Override
            public Player chooseRobberTarget(GameState state, List<Player> candidates) {
                return candidates.isEmpty() ? null : candidates.get(0);
            }

            @Override
            public HexTile chooseRobberPlacement(GameState state) {
                return null;
            }
        };
    }

    @Test
    void testConstructorValid() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertEquals("Bot1", bot.getName());
        assertEquals(PlayerColor.BLUE, bot.getColor());
        assertTrue(bot.isBot());
        assertSame(dummyStrategy, bot.getStrategy());
    }

    @Test
    void testConstructorNullStrategy() {
        assertThrows(IllegalArgumentException.class,
            () -> new BotPlayer("Bot1", PlayerColor.BLUE, null));
    }

    @Test
    void testExecuteTurnNullState() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertThrows(IllegalArgumentException.class, () -> bot.executeTurn(null));
    }

    @Test
    void testSetStrategyNull() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertThrows(IllegalArgumentException.class, () -> bot.setStrategy(null));
    }

    @Test
    void testResourceManagement() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        bot.addResource(ResourceType.WOOD, 5);
        assertEquals(5, bot.getResourceCount(ResourceType.WOOD));
        bot.removeResource(ResourceType.WOOD, 2);
        assertEquals(3, bot.getResourceCount(ResourceType.WOOD));
    }

    @Test
    void testRemoveResourceInsufficient() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertThrows(IllegalArgumentException.class,
            () -> bot.removeResource(ResourceType.WOOD, 1));
    }

    @Test
    void testCards() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
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
        bot.addCard(card);
        assertEquals(1, bot.getHandCards().size());
        bot.removeCard(card);
        assertEquals(0, bot.getHandCards().size());
    }

    @Test
    void testKnightsPlayed() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertEquals(0, bot.getKnightsPlayed());
        bot.incrementKnightsPlayed();
        assertEquals(1, bot.getKnightsPlayed());
    }

    @Test
    void testLongestRoadLength() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        bot.setLongestRoadLength(4);
        assertEquals(4, bot.getLongestRoadLength());
        assertThrows(IllegalArgumentException.class, () -> bot.setLongestRoadLength(-1));
    }

    @Test
    void testSpecialCards() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        assertFalse(bot.hasSpecialCard(SpecialCardType.LARGEST_ARMY));
        bot.setSpecialCard(SpecialCardType.LARGEST_ARMY, true);
        assertTrue(bot.hasSpecialCard(SpecialCardType.LARGEST_ARMY));
    }

    @Test
    void testToString() {
        BotPlayer bot = new BotPlayer("Bot1", PlayerColor.BLUE, dummyStrategy);
        String s = bot.toString();
        assertTrue(s.contains("BotPlayer"));
        assertTrue(s.contains("Bot1"));
    }
}
