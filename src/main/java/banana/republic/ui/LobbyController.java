package banana.republic.ui;

import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.player.BotPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerStrategy;
import banana.republic.plugin.MapGeneratorPlugin;
import banana.republic.plugin.PluginLoadResult;
import banana.republic.plugin.PluginLoader;
import banana.republic.plugin.PluginRegistry;

import java.util.List;

/**
 * Kontroler layar lobby — tempat pemain dikonfigurasi dan plugin diload
 * sebelum permainan dimulai.
 *
 * <h3>Tanggung jawab:</h3>
 * <ol>
 *   <li>Mengelola konfigurasi pemain (nama, warna, human/bot)</li>
 *   <li>Memuat plugin via {@link PluginLoader} dan menyimpannya di {@link PluginRegistry}</li>
 *   <li>Membuat instance {@link Game} dengan plugin yang tepat (Game tetap decoupled)</li>
 *   <li>Menyuntikkan plugin kartu ke {@code CardDeck} sebelum game dimulai</li>
 * </ol>
 *
 * <h3>Alur plugin saat start game:</h3>
 * <pre>
 * // 1. Map plugin → dipass ke Game constructor
 * MapGeneratorPlugin mapPlugin = registry.getMapGenerator().orElse(null);
 * Game game = new Game(players, mapPlugin);
 *
 * // 2. Card plugins → injeksi ke deck sebelum game dimulai
 * if (registry.hasCards()) {
 *     game.getCardDeck().buildDefaultDeck();
 *     game.getCardDeck().injectPluginCards(registry.getLoadedCards());
 * } else {
 *     game.getCardDeck().buildDefaultDeck();
 * }
 *
 * // 3. Bot strategy → dipakai saat membuat BotPlayer di konfigurasi pemain
 * PlayerStrategy strategy = registry.getBotStrategy().orElseThrow(
 *     () -> new IllegalStateException("Bot strategy belum diload"));
 * BotPlayer bot = new BotPlayer(name, color, strategy);
 * </pre>
 *
 * <p>Perhatikan bahwa {@link Game} <strong>tidak menerima</strong> {@link PluginRegistry}
 * secara langsung — Game hanya menerima {@link MapGeneratorPlugin} (atau null).
 * Semua injeksi plugin lainnya dilakukan oleh kelas ini setelah {@code Game} dibuat.
 */
public class LobbyController {

    private final PluginLoader   pluginLoader   = new PluginLoader();
    private final PluginRegistry pluginRegistry = new PluginRegistry();

    // -------------------------------------------------------------------------
    // Plugin loading — ExperimentCard
    // -------------------------------------------------------------------------

    /**
     * Memuat semua implementasi {@link ExperimentCard} dari JAR secara otomatis
     * (auto-discovery: tidak perlu tahu nama class-nya).
     *
     * <p>Semua kartu yang berhasil diload langsung didaftarkan ke registry.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list hasil loading; tiap entry bisa success atau failure
     */
    public List<PluginLoadResult<ExperimentCard>> loadCardPlugin(String jarPath) {
        List<PluginLoadResult<ExperimentCard>> results = pluginLoader.loadAllCards(jarPath);
        for (PluginLoadResult<ExperimentCard> result : results) {
            if (result.isSuccess()) {
                pluginRegistry.registerCard(result.getInstance());
            }
        }
        return results;
    }

    /**
     * Memuat satu {@link ExperimentCard} dari JAR berdasarkan nama class secara manual.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return hasil loading
     */
    public PluginLoadResult<ExperimentCard> loadCardPluginManual(String jarPath, String className) {
        PluginLoadResult<ExperimentCard> result = pluginLoader.loadCard(jarPath, className);
        if (result.isSuccess()) {
            pluginRegistry.registerCard(result.getInstance());
        }
        return result;
    }

    /**
     * Menemukan semua implementasi {@link ExperimentCard} dalam JAR tanpa memuat mereka.
     *
     * <p>Digunakan UI untuk menampilkan dropdown pilihan class sebelum user konfirmasi.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name yang bisa dipilih user
     */
    public List<String> discoverCardClasses(String jarPath) {
        return pluginLoader.discoverCards(jarPath);
    }

    // -------------------------------------------------------------------------
    // Plugin loading — MapGeneratorPlugin
    // -------------------------------------------------------------------------

    /**
     * Memuat {@link MapGeneratorPlugin} dari JAR secara otomatis
     * (menggunakan class pertama yang ditemukan via auto-discovery).
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return hasil loading
     */
    public PluginLoadResult<MapGeneratorPlugin> loadMapPlugin(String jarPath) {
        List<String> discovered = pluginLoader.discoverMapGenerators(jarPath);
        if (discovered.isEmpty()) {
            return PluginLoadResult.failure(
                "Tidak ada implementasi MapGeneratorPlugin ditemukan dalam JAR", jarPath);
        }
        return loadMapPluginManual(jarPath, discovered.get(0));
    }

    /**
     * Memuat {@link MapGeneratorPlugin} dari JAR berdasarkan nama class secara manual.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return hasil loading
     */
    public PluginLoadResult<MapGeneratorPlugin> loadMapPluginManual(String jarPath, String className) {
        PluginLoadResult<MapGeneratorPlugin> result = pluginLoader.loadMapGenerator(jarPath, className);
        if (result.isSuccess()) {
            pluginRegistry.setMapGenerator(result.getInstance());
        }
        return result;
    }

    /**
     * Menemukan semua implementasi {@link MapGeneratorPlugin} dalam JAR.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name
     */
    public List<String> discoverMapGeneratorClasses(String jarPath) {
        return pluginLoader.discoverMapGenerators(jarPath);
    }

    // -------------------------------------------------------------------------
    // Plugin loading — PlayerStrategy
    // -------------------------------------------------------------------------

    /**
     * Memuat {@link PlayerStrategy} dari JAR secara otomatis.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return hasil loading
     */
    public PluginLoadResult<PlayerStrategy> loadStrategyPlugin(String jarPath) {
        List<String> discovered = pluginLoader.discoverStrategies(jarPath);
        if (discovered.isEmpty()) {
            return PluginLoadResult.failure(
                "Tidak ada implementasi PlayerStrategy ditemukan dalam JAR", jarPath);
        }
        return loadStrategyPluginManual(jarPath, discovered.get(0));
    }

    /**
     * Memuat {@link PlayerStrategy} dari JAR berdasarkan nama class secara manual.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return hasil loading
     */
    public PluginLoadResult<PlayerStrategy> loadStrategyPluginManual(String jarPath, String className) {
        PluginLoadResult<PlayerStrategy> result = pluginLoader.loadStrategy(jarPath, className);
        if (result.isSuccess()) {
            pluginRegistry.setBotStrategy(result.getInstance());
        }
        return result;
    }

    /**
     * Menemukan semua implementasi {@link PlayerStrategy} dalam JAR.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name
     */
    public List<String> discoverStrategyClasses(String jarPath) {
        return pluginLoader.discoverStrategies(jarPath);
    }

    // -------------------------------------------------------------------------
    // Start game — integrasi semua plugin
    // -------------------------------------------------------------------------

    /**
     * Membuat instance {@link Game} dengan semua plugin yang sudah terdaftar,
     * lalu menginisialisasi deck (dengan atau tanpa plugin kartu).
     *
     * <p>Urutan eksekusi:
     * <ol>
     *   <li>Ekstrak {@code MapGeneratorPlugin} dari registry (null jika tidak ada)</li>
     *   <li>Buat {@code Game} — map plugin di-pass ke constructor</li>
     *   <li>Build default deck</li>
     *   <li>Jika ada plugin kartu, inject ke deck lalu shuffle ulang</li>
     * </ol>
     *
     * @param players daftar pemain (sudah termasuk {@link BotPlayer} jika ada bot)
     * @return {@link Game} yang siap dimulai via {@code game.startSetupPhase()}
     */
    public Game createGame(List<Player> players) {
        // 1. Map plugin → null-safe; Game fallback ke StandardMapGenerator
        MapGeneratorPlugin mapPlugin = pluginRegistry.getMapGenerator().orElse(null);
        Game game = new Game(players, mapPlugin);

        // 2. Initialize deck
        game.getCardDeck().buildDefaultDeck();

        // 3. Inject plugin cards jika ada
        if (pluginRegistry.hasCards()) {
            game.getCardDeck().injectPluginCards(pluginRegistry.getLoadedCards());
        }

        return game;
    }

    // -------------------------------------------------------------------------
    // Query state registry (untuk UI menampilkan status plugin yang sudah diload)
    // -------------------------------------------------------------------------

    /** Mengembalikan jumlah kartu plugin yang sudah terdaftar. */
    public int getLoadedCardCount()    { return pluginRegistry.getLoadedCards().size(); }

    /** {@code true} jika ada map generator plugin terdaftar. */
    public boolean hasMapPlugin()      { return pluginRegistry.hasMapGenerator(); }

    /** {@code true} jika ada bot strategy plugin terdaftar. */
    public boolean hasStrategyPlugin() { return pluginRegistry.hasBotStrategy(); }

    /** Mengembalikan registry (read access untuk UI yang perlu display detail). */
    public PluginRegistry getRegistry() { return pluginRegistry; }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Reset registry dan tutup semua ClassLoader.
     * Dipanggil saat kembali ke main menu atau memulai sesi baru.
     */
    public void reset() {
        pluginRegistry.clear();
        pluginLoader.closeAll();
    }
}
