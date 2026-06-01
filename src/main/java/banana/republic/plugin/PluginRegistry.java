package banana.republic.plugin;

import banana.republic.card.ExperimentCard;
import banana.republic.player.PlayerStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Menyimpan semua plugin yang sudah berhasil diload untuk satu sesi game.
 *
 * <p>Dibuat dan dikelola oleh {@code LobbyController} — Game tidak mengetahui
 * keberadaan kelas ini secara langsung (decoupled).
 *
 * <p>Alur penggunaan:
 * <ol>
 *   <li>Buat satu instance di lobby</li>
 *   <li>Load plugin via {@link PluginLoader}, register hasilnya ke sini</li>
 *   <li>Sebelum game dimulai, ekstrak nilai yang dibutuhkan dan pass ke {@code Game}/{@code CardDeck}</li>
 *   <li>Panggil {@link #clear()} jika perlu reset untuk game baru</li>
 * </ol>
 *
 * <h3>Tanggung jawab LobbyController:</h3>
 * <pre>{@code
 * // Map plugin → pass ke Game constructor
 * MapGeneratorPlugin mapPlugin = registry.getMapGenerator().orElse(null);
 * Game game = new Game(players, mapPlugin);
 *
 * // Card plugins → inject ke deck sebelum game dimulai
 * game.getCardDeck().buildDefaultDeck();
 * game.getCardDeck().injectPluginCards(registry.getLoadedCards());
 *
 * // Bot strategy → pass ke BotPlayer constructor
 * PlayerStrategy strategy = registry.getBotStrategy().orElseThrow();
 * BotPlayer bot = new BotPlayer(name, color, strategy);
 * }</pre>
 */
public class PluginRegistry {

    private final List<ExperimentCard> loadedCards  = new ArrayList<>();
    private MapGeneratorPlugin         mapGenerator  = null;
    private PlayerStrategy             botStrategy   = null;

    // -------------------------------------------------------------------------
    // ExperimentCard
    // -------------------------------------------------------------------------

    /**
     * Mendaftarkan satu kartu kustom yang berhasil diload.
     *
     * @param card instance {@link ExperimentCard} yang valid (tidak boleh null)
     */
    public void registerCard(ExperimentCard card) {
        if (card == null) throw new IllegalArgumentException("Card tidak boleh null");
        loadedCards.add(card);
    }

    /**
     * Mendaftarkan beberapa kartu kustom sekaligus (multi-card JAR support).
     *
     * @param cards list kartu dari {@link PluginLoader#loadAllCards(String)}
     */
    public void registerCards(List<ExperimentCard> cards) {
        if (cards == null) throw new IllegalArgumentException("Cards tidak boleh null");
        for (ExperimentCard card : cards) {
            if (card != null) loadedCards.add(card);
        }
    }

    /**
     * Mengembalikan semua kartu kustom yang terdaftar (unmodifiable).
     *
     * <p>List ini digunakan oleh {@code LobbyController} untuk memanggil
     * {@code CardDeck.injectPluginCards()}.
     */
    public List<ExperimentCard> getLoadedCards() {
        return Collections.unmodifiableList(loadedCards);
    }

    /** {@code true} jika ada minimal satu kartu kustom terdaftar. */
    public boolean hasCards() {
        return !loadedCards.isEmpty();
    }

    // -------------------------------------------------------------------------
    // MapGeneratorPlugin
    // -------------------------------------------------------------------------

    /**
     * Mendaftarkan generator peta kustom.
     *
     * <p>Hanya satu generator aktif — memanggil method ini dua kali akan
     * menimpa registrasi sebelumnya.
     *
     * @param generator instance {@link MapGeneratorPlugin} yang valid (tidak boleh null)
     */
    public void setMapGenerator(MapGeneratorPlugin generator) {
        if (generator == null) throw new IllegalArgumentException("MapGeneratorPlugin tidak boleh null");
        this.mapGenerator = generator;
    }

    /**
     * Mengembalikan generator peta terdaftar, jika ada.
     *
     * <p>Digunakan oleh {@code LobbyController}:
     * {@code new Game(players, registry.getMapGenerator().orElse(null))}
     */
    public Optional<MapGeneratorPlugin> getMapGenerator() {
        return Optional.ofNullable(mapGenerator);
    }

    /** {@code true} jika ada map generator terdaftar. */
    public boolean hasMapGenerator() {
        return mapGenerator != null;
    }

    // -------------------------------------------------------------------------
    // PlayerStrategy
    // -------------------------------------------------------------------------

    /**
     * Mendaftarkan strategi bot kustom.
     *
     * <p>Hanya satu strategi aktif per registry — memanggil method ini dua kali
     * akan menimpa registrasi sebelumnya.
     *
     * @param strategy instance {@link PlayerStrategy} yang valid (tidak boleh null)
     */
    public void setBotStrategy(PlayerStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("PlayerStrategy tidak boleh null");
        this.botStrategy = strategy;
    }

    /**
     * Mengembalikan strategi bot terdaftar, jika ada.
     *
     * <p>Digunakan oleh {@code LobbyController} saat membuat {@code BotPlayer}:
     * {@code new BotPlayer(name, color, registry.getBotStrategy().orElseThrow())}
     */
    public Optional<PlayerStrategy> getBotStrategy() {
        return Optional.ofNullable(botStrategy);
    }

    /** {@code true} jika ada bot strategy terdaftar. */
    public boolean hasBotStrategy() {
        return botStrategy != null;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Menghapus semua registrasi.
     *
     * <p>Dipanggil sebelum memulai game baru dari lobby yang sama,
     * agar plugin dari session sebelumnya tidak ikut terbawa.
     */
    public void clear() {
        loadedCards.clear();
        mapGenerator = null;
        botStrategy  = null;
    }

    @Override
    public String toString() {
        return String.format(
            "PluginRegistry[cards=%d, mapGenerator=%s, botStrategy=%s]",
            loadedCards.size(),
            mapGenerator != null ? mapGenerator.getClass().getSimpleName() : "none",
            botStrategy  != null ? botStrategy.getClass().getSimpleName()  : "none");
    }
}
