package banana.republic.card;

import java.util.*;

/**
 * Deck management untuk Kartu Temuan Dr. Neroifa.
 *
 * <p>Komposisi deck default:
 * <ul>
 *   <li>14x {@link KnightCard}</li>
 *   <li>3x {@link RoadBuildingCard}</li>
 *   <li>3x {@link MonopolyCard}</li>
 *   <li>5x {@link VictoryPointCard}</li>
 * </ul>
 *
 * <p>Total: 25 kartu.
 *
 * <p>Struktur internal:
 * <ul>
 *   <li>{@code drawPile}: {@link Deque} untuk draw efisien dari atas (LIFO)</li>
 *   <li>{@code discardPile}: {@link List} untuk track kartu yang sudah dimainkan</li>
 * </ul>
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
     * Tarik 1 kartu dari atas draw pile.
     *
     * @return kartu yang ditarik, atau {@code null} jika deck habis
     */
    public ExperimentCard draw() {
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.poll();
    }

    /**
     * Menambahkan kartu ke atas draw pile.
     *
     * @param card kartu yang ditambahkan; {@code null} diabaikan
     */
    public void addCard(ExperimentCard card) {
        if (card != null) {
            drawPile.push(card);
        }
    }

    /**
     * Menambahkan kartu ke bawah draw pile.
     *
     * <p>Berguna untuk progress card yang sudah dimainkan dan di-shuffle.
     *
     * @param card kartu yang ditambahkan; {@code null} diabaikan
     */
    public void addToBottom(ExperimentCard card) {
        if (card != null) {
            drawPile.offer(card);
        }
    }

    /**
     * Mengacak draw pile.
     *
     * <p>Konversi ke {@link List}, shuffle, lalu konversi kembali ke {@link Deque}.
     */
    public void shuffle() {
        if (drawPile.isEmpty()) {
            return;
        }

        List<ExperimentCard> tempList = new ArrayList<>(drawPile);
        Collections.shuffle(tempList);
        drawPile = new ArrayDeque<>(tempList);
    }

    /**
     * Cek apakah draw pile kosong.
     *
     * @return {@code true} jika tidak ada kartu tersisa
     */
    public boolean isEmpty() {
        return drawPile.isEmpty();
    }

    /**
     * Mengembalikan ukuran draw pile.
     *
     * @return jumlah kartu di draw pile
     */
    public int drawPileSize() {
        return drawPile.size();
    }

    /**
     * Mengembalikan salinan discard pile.
     *
     * <p>Penting untuk Largest Army calculation (menghitung KnightCard
     * yang sudah diungkap).
     *
     * @return salinan baru dari discard pile
     */
    public List<ExperimentCard> getDiscardPile() {
        return new ArrayList<>(discardPile);
    }

    /**
     * Mengembalikan draw pile dalam urutan top-first (atas ke bawah).
     *
     * @return salinan baru dari draw pile
     */
    public List<ExperimentCard> getDrawPile() {
        return new ArrayList<>(drawPile);
    }

    /**
     * Menambahkan kartu ke discard pile (ketika kartu sudah dimainkan).
     *
     * @param card kartu yang dibuang; {@code null} diabaikan
     */
    public void addToDiscardPile(ExperimentCard card) {
        if (card != null) {
            discardPile.add(card);
        }
    }

    /**
     * Membangun deck default dengan komposisi standar Banana Republic,
     * kemudian mengacak.
     *
     * <p>Total kartu: 25 (14 Knight + 3 Road Building + 3 Monopoly + 5 VP).
     */
    public void buildDefaultDeck() {
        drawPile.clear();
        discardPile.clear();

        for (int i = 0; i < 14; i++) {
            addCard(new KnightCard());
        }

        for (int i = 0; i < 3; i++) {
            addCard(new RoadBuildingCard());
        }

        for (int i = 0; i < 3; i++) {
            addCard(new MonopolyCard());
        }

        for (int i = 0; i < 5; i++) {
            addCard(new VictoryPointCard());
        }

        shuffle();

        assert (drawPileSize() == 25) : "Default deck harus berisi 25 kartu";
    }

    /**
     * Menyuntikkan kartu-kartu dari plugin ke deck, lalu mengacak ulang.
     *
     * <p>Dipanggil setelah {@link #buildDefaultDeck()} jika ada plugin kartu.
     *
     * @param pluginCards daftar kartu dari plugin
     */
    public void injectPluginCards(List<ExperimentCard> pluginCards) {
        for (ExperimentCard card : pluginCards) {
            addCard(card);
        }
        shuffle();
    }
}
