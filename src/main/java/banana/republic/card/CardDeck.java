package banana.republic.card;

import java.util.*;

/**
 * Deck management for experiment cards (Kartu Temuan Dr. Neroifa).
 *
 * Komposisi deck default:
 * - 14x KnightCard
 * - 3x RoadBuildingCard
 * - 3x MonopolyCard
 * - 5x VictoryPointCard
 * Total: 25 kartu
 *
 * Struktur internal:
 * - drawPile: Deque untuk draw efisien dari atas (LIFO)
 * - discardPile: List untuk track kartu yang sudah dimainkan
 */
public class CardDeck {
    private Deque<ExperimentCard> drawPile;
    private List<ExperimentCard> discardPile;

    /**
     * Constructor: inisialisasi deck kosong.
     */
    public CardDeck() {
        this.drawPile = new ArrayDeque<>();
        this.discardPile = new ArrayList<>();
    }

    /**
     * Tarik 1 kartu dari atas deck (draw pile).
     * Jika draw pile kosong, return null (dan UI harus handle kondisi ini).
     */
    public ExperimentCard draw() {
        if (drawPile.isEmpty()) {
            return null; // Deck habis, tidak bisa tarik kartu lagi
        }
        return drawPile.poll(); // Remove dari atas deque
    }

    /**
     * Tambahkan kartu ke atas draw pile (untuk kartu baru atau shuffle).
     */
    public void addCard(ExperimentCard card) {
        if (card != null) {
            drawPile.push(card); // Add ke atas deque
        }
    }

    /**
     * Tambahkan kartu ke bawah draw pile (untuk progress card yang sudah dimainkan dan di-shuffle).
     */
    public void addToBottom(ExperimentCard card) {
        if (card != null) {
            drawPile.offer(card); // Add ke bawah deque
        }
    }

    /**
     * Shuffle draw pile.
     * Konversi ke List, shuffle, lalu konversi kembali ke Deque.
     */
    public void shuffle() {
        if (drawPile.isEmpty()) {
            return;
        }

        // Konversi ke List
        List<ExperimentCard> tempList = new ArrayList<>(drawPile);

        // Shuffle
        Collections.shuffle(tempList);

        // Buat deque baru
        drawPile = new ArrayDeque<>(tempList);
    }

    /**
     * Cek apakah draw pile kosong.
     */
    public boolean isEmpty() {
        return drawPile.isEmpty();
    }

    /**
     * Dapatkan ukuran draw pile.
     */
    public int drawPileSize() {
        return drawPile.size();
    }

    /**
     * Dapatkan discard pile (untuk tracking kartu yang sudah dimainkan).
     * Penting untuk Largest Army calculation (count KnightCard yang sudah diungkap).
     */
    public List<ExperimentCard> getDiscardPile() {
        return new ArrayList<>(discardPile); // Return copy untuk encapsulation
    }

    /**
     * Dapatkan draw pile dalam urutan top-first (atas ke bawah).
     */
    public List<ExperimentCard> getDrawPile() {
        return new ArrayList<>(drawPile);
    }

    /**
     * Tambahkan kartu ke discard pile (ketika kartu sudah dimainkan).
     */
    public void addToDiscardPile(ExperimentCard card) {
        if (card != null) {
            discardPile.add(card);
        }
    }

    /**
     * Build default deck dengan komposisi standar Banana Republic.
     * 14x KnightCard + 3x RoadBuildingCard + 3x MonopolyCard + 5x VictoryPointCard
     * Kemudian shuffle.
     */
    public void buildDefaultDeck() {
        drawPile.clear();
        discardPile.clear();

        // Tambahkan 14 KnightCard
        for (int i = 0; i < 14; i++) {
            addCard(new KnightCard());
        }

        // Tambahkan 3 RoadBuildingCard
        for (int i = 0; i < 3; i++) {
            addCard(new RoadBuildingCard());
        }

        // Tambahkan 3 MonopolyCard
        for (int i = 0; i < 3; i++) {
            addCard(new MonopolyCard());
        }

        // Tambahkan 5 VictoryPointCard
        for (int i = 0; i < 5; i++) {
            addCard(new VictoryPointCard());
        }

        // Shuffle
        shuffle();

        // Assertion: total kartu harus 25
        assert (drawPileSize() == 25) : "Default deck harus berisi 25 kartu";
    }
    /**
     * Menyuntikkan kartu-kartu dari plugin ke draw pile yang sudah ada, lalu shuffle ulang.
     *
     * <p>Dipanggil oleh {@code LobbyController} setelah {@link #buildDefaultDeck()}
     * jika ada plugin kartu yang terdaftar di {@code PluginRegistry}.
     *
     * <p><strong>Urutan yang benar:</strong>
     * <pre>
     *     deck.buildDefaultDeck();
     *     deck.injectPluginCards(registry.getLoadedCards());
     * </pre>
     *
     * @param pluginCards daftar kartu dari {@code PluginRegistry.getLoadedCards()};
     *                    boleh kosong (no-op), tidak boleh null
     */
    public void injectPluginCards(List<ExperimentCard> pluginCards) {
        if (pluginCards == null) {
            throw new IllegalArgumentException("pluginCards tidak boleh null");
        }
        if (pluginCards.isEmpty()) return; // no-op

        for (ExperimentCard card : pluginCards) {
            if (card != null) {
                addCard(card);
            }
        }
        shuffle(); // shuffle ulang setelah injeksi agar posisi kartu plugin acak
    }
}
