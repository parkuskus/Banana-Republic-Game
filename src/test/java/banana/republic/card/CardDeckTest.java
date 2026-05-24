package banana.republic.card;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests untuk CardDeck.
 * Memverifikasi:
 * 1. Default deck initialization (25 kartu)
 * 2. Draw mechanism (FIFO dari deque)
 * 3. Shuffle randomness
 * 4. Discard pile tracking
 */
@DisplayName("CardDeck Tests")
public class CardDeckTest {
    private CardDeck deck;

    @BeforeEach
    void setUp() {
        deck = new CardDeck();
    }

    @Test
    @DisplayName("Default deck should have 25 cards")
    void testDefaultDeckSize() {
        deck.buildDefaultDeck();
        assertEquals(25, deck.drawPileSize(), "Default deck harus 25 kartu");
    }

    @Test
    @DisplayName("Default deck should have correct composition")
    void testDefaultDeckComposition() {
        deck.buildDefaultDeck();

        // Shuffle supaya mix, kemudian cek total type
        int knightCount = 0, roadCount = 0, monopolyCount = 0, vpCount = 0;

        while (!deck.isEmpty()) {
            ExperimentCard card = deck.draw();
            switch (card.getCardType()) {
                case KNIGHT -> knightCount++;
                case ROAD_BUILDING -> roadCount++;
                case MONOPOLY -> monopolyCount++;
                case VICTORY_POINT -> vpCount++;
            }
        }

        assertEquals(14, knightCount, "Knights: 14");
        assertEquals(3, roadCount, "Road Building: 3");
        assertEquals(3, monopolyCount, "Monopoly: 3");
        assertEquals(5, vpCount, "Victory Points: 5");
    }

    @Test
    @DisplayName("Drawing from empty deck should return null")
    void testDrawFromEmptyDeck() {
        assertNull(deck.draw(), "Draw dari empty deck harus null");
    }

    @Test
    @DisplayName("Shuffle should change order (probabilistically)")
    void testShuffleRandomness() {
        deck.buildDefaultDeck();

        // Build 2 decks dan shuffle keduanya
        CardDeck deck1 = new CardDeck();
        deck1.buildDefaultDeck();
        deck1.shuffle();

        CardDeck deck2 = new CardDeck();
        deck2.buildDefaultDeck();
        deck2.shuffle();

        // Ambil order dari deck1 dan deck2
        StringBuilder order1 = new StringBuilder();
        StringBuilder order2 = new StringBuilder();

        while (!deck1.isEmpty()) {
            order1.append(deck1.draw().getCardType());
        }
        while (!deck2.isEmpty()) {
            order2.append(deck2.draw().getCardType());
        }

        // Orders seharusnya berbeda (atau sangat jarang sama)
        // Note: Ada probabilitas kecil keduanya sama, tapi sangat kecil
        assertNotEquals(order1.toString(), order2.toString(),
                "Shuffle seharusnya menghasilkan order berbeda (probabilistically)");
    }

    @Test
    @DisplayName("Add card to deck")
    void testAddCard() {
        ExperimentCard card = new KnightCard();
        deck.addCard(card);

        assertEquals(1, deck.drawPileSize());
        assertEquals(card, deck.draw());
    }

    @Test
    @DisplayName("Discard pile tracking")
    void testDiscardPileTracking() {
        ExperimentCard card = new KnightCard();
        deck.addToDiscardPile(card);

        assertEquals(1, deck.getDiscardPile().size());
        assertTrue(deck.getDiscardPile().contains(card));
    }

    @Test
    @DisplayName("Drawing card updates size")
    void testDrawUpdatesSize() {
        deck.buildDefaultDeck();
        int initialSize = deck.drawPileSize();

        deck.draw();
        assertEquals(initialSize - 1, deck.drawPileSize());
    }
}

