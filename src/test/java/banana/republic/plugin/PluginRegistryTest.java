package banana.republic.plugin;

import banana.republic.card.ExperimentCard;
import banana.republic.card.CardType;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.player.PlayerStrategy;
import banana.republic.player.Action;
import banana.republic.board.HexTile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

/**
 * Unit tests untuk PluginRegistry.
 */
@DisplayName("PluginRegistry Tests")
class PluginRegistryTest {

    private PluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PluginRegistry();
    }

    // -------------------------------------------------------------------------
    // ExperimentCard
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Registry awal: tidak ada kartu")
    void testInitialState_noCards() {
        assertFalse(registry.hasCards());
        assertTrue(registry.getLoadedCards().isEmpty());
    }

    @Test
    @DisplayName("registerCard: menambah satu kartu")
    void testRegisterCard_single() {
        ExperimentCard card = mockCard("A");
        registry.registerCard(card);

        assertTrue(registry.hasCards());
        assertEquals(1, registry.getLoadedCards().size());
        assertSame(card, registry.getLoadedCards().get(0));
    }

    @Test
    @DisplayName("registerCards: menambah banyak kartu sekaligus")
    void testRegisterCards_multiple() {
        List<ExperimentCard> cards = List.of(mockCard("A"), mockCard("B"), mockCard("C"));
        registry.registerCards(cards);

        assertEquals(3, registry.getLoadedCards().size());
        assertTrue(registry.hasCards());
    }

    @Test
    @DisplayName("registerCard: null → throw")
    void testRegisterCard_null() {
        assertThrows(IllegalArgumentException.class, () -> registry.registerCard(null));
    }

    @Test
    @DisplayName("registerCards: null list → throw")
    void testRegisterCards_nullList() {
        assertThrows(IllegalArgumentException.class, () -> registry.registerCards(null));
    }

    @Test
    @DisplayName("getLoadedCards: return unmodifiable list")
    void testGetLoadedCards_unmodifiable() {
        registry.registerCard(mockCard("A"));
        List<ExperimentCard> cards = registry.getLoadedCards();
        assertThrows(UnsupportedOperationException.class, () -> cards.add(mockCard("B")));
    }

    // -------------------------------------------------------------------------
    // MapGeneratorPlugin
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Registry awal: tidak ada map generator")
    void testInitialState_noMapGenerator() {
        assertFalse(registry.hasMapGenerator());
        assertEquals(Optional.empty(), registry.getMapGenerator());
    }

    @Test
    @DisplayName("setMapGenerator: menyimpan generator")
    void testSetMapGenerator() {
        MapGeneratorPlugin gen = mockMapGenerator();
        registry.setMapGenerator(gen);

        assertTrue(registry.hasMapGenerator());
        assertTrue(registry.getMapGenerator().isPresent());
        assertSame(gen, registry.getMapGenerator().get());
    }

    @Test
    @DisplayName("setMapGenerator: null → throw")
    void testSetMapGenerator_null() {
        assertThrows(IllegalArgumentException.class, () -> registry.setMapGenerator(null));
    }

    @Test
    @DisplayName("setMapGenerator dua kali: menimpa yang pertama")
    void testSetMapGenerator_overwrite() {
        MapGeneratorPlugin gen1 = mockMapGenerator();
        MapGeneratorPlugin gen2 = mockMapGenerator();
        registry.setMapGenerator(gen1);
        registry.setMapGenerator(gen2);

        assertSame(gen2, registry.getMapGenerator().get());
    }

    // -------------------------------------------------------------------------
    // PlayerStrategy
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Registry awal: tidak ada bot strategy")
    void testInitialState_noBotStrategy() {
        assertFalse(registry.hasBotStrategy());
        assertEquals(Optional.empty(), registry.getBotStrategy());
    }

    @Test
    @DisplayName("setBotStrategy: menyimpan strategi")
    void testSetBotStrategy() {
        PlayerStrategy strategy = mockStrategy();
        registry.setBotStrategy(strategy);

        assertTrue(registry.hasBotStrategy());
        assertTrue(registry.getBotStrategy().isPresent());
        assertSame(strategy, registry.getBotStrategy().get());
    }

    @Test
    @DisplayName("setBotStrategy: null → throw")
    void testSetBotStrategy_null() {
        assertThrows(IllegalArgumentException.class, () -> registry.setBotStrategy(null));
    }

    // -------------------------------------------------------------------------
    // clear()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("clear: menghapus semua registrasi")
    void testClear() {
        registry.registerCard(mockCard("A"));
        registry.setMapGenerator(mockMapGenerator());
        registry.setBotStrategy(mockStrategy());

        registry.clear();

        assertFalse(registry.hasCards());
        assertFalse(registry.hasMapGenerator());
        assertFalse(registry.hasBotStrategy());
        assertTrue(registry.getLoadedCards().isEmpty());
    }

    @Test
    @DisplayName("toString: format yang informatif")
    void testToString() {
        registry.registerCard(mockCard("A"));
        String str = registry.toString();
        assertNotNull(str);
        assertTrue(str.contains("cards=1"));
        assertTrue(str.contains("none")); // map dan bot belum diset
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ExperimentCard mockCard(String name) {
        return new ExperimentCard() {
            @Override public String getCardName() { return name; }
            @Override public String getDescription() { return "desc"; }
            @Override public void applyEffect(GameState s, Player p) {}
            @Override public boolean isPlayable() { return true; }
            @Override public boolean isSecret() { return false; }
            @Override public CardType getCardType() { return CardType.KNIGHT; }
        };
    }

    private MapGeneratorPlugin mockMapGenerator() {
        return () -> null; // generateBoard() returns null — only testing registry storage
    }

    private PlayerStrategy mockStrategy() {
        return new PlayerStrategy() {
            @Override public List<Action> takeTurn(GameState s) { return List.of(); }
            @Override public Player chooseRobberTarget(GameState s, List<Player> c) { return null; }
            @Override public HexTile chooseRobberPlacement(GameState s) { return null; }
        };
    }
}
