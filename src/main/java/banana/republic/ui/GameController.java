package banana.republic.ui;

import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.plugin.PluginLoadResult;
import banana.republic.plugin.PluginLoader;

import java.util.List;

/**
 * Main game controller.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 *
 * <p>Mengelola alur permainan saat game sedang berjalan, termasuk kemampuan
 * untuk memuat plugin JAR secara dinamis ke dalam game yang sedang aktif.
 */
public class GameController {

    private Game game;
    private final PluginLoader pluginLoader = new PluginLoader();

    /**
     * Memasukkan instance Game yang sedang berjalan ke controller ini.
     *
     * @param game instance permainan aktif
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Memuat semua implementasi {@link ExperimentCard} dari JAR secara otomatis
     * dan menyuntikkannya ke deck permainan yang sedang berjalan.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list hasil loading
     */
    public List<PluginLoadResult<ExperimentCard>> loadCardPlugin(String jarPath) {
        List<PluginLoadResult<ExperimentCard>> results = pluginLoader.loadAllCards(jarPath);

        List<ExperimentCard> loaded = results.stream()
            .filter(PluginLoadResult::isSuccess)
            .map(PluginLoadResult::getInstance)
            .toList();

        if (!loaded.isEmpty() && game != null) {
            game.getCardDeck().injectPluginCards(loaded);
        }
        return results;
    }

    /**
     * Memuat satu {@link ExperimentCard} dari JAR berdasarkan nama class
     * secara manual dan menyuntikkannya ke deck permainan.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return hasil loading
     */
    public PluginLoadResult<ExperimentCard> loadCardPluginManual(String jarPath, String className) {
        PluginLoadResult<ExperimentCard> result = pluginLoader.loadCard(jarPath, className);
        if (result.isSuccess() && game != null) {
            game.getCardDeck().injectPluginCards(List.of(result.getInstance()));
        }
        return result;
    }

    /**
     * Menemukan semua implementasi {@link ExperimentCard} dalam JAR tanpa memuat mereka.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name
     */
    public List<String> discoverCardClasses(String jarPath) {
        return pluginLoader.discoverCards(jarPath);
    }
}
