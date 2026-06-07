package banana.republic.ui.command;

import java.util.List;

import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.plugin.PluginLoadResult;
import banana.republic.plugin.PluginLoader;

public class PluginUiService {

    private final PluginLoader pluginLoader = new PluginLoader();

    public List<PluginLoadResult<ExperimentCard>> loadCardPlugin(Game game, String jarPath) {
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

    public PluginLoadResult<ExperimentCard> loadCardPluginManual(Game game, String jarPath, String className) {
        PluginLoadResult<ExperimentCard> result = pluginLoader.loadCard(jarPath, className);
        if (result.isSuccess() && game != null) {
            game.getCardDeck().injectPluginCards(List.of(result.getInstance()));
        }
        return result;
    }

    public List<String> discoverCardClasses(String jarPath) {
        return pluginLoader.discoverCards(jarPath);
    }
}
