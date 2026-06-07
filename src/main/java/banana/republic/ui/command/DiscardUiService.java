package banana.republic.ui.command;

import java.util.Map;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

public class DiscardUiService {

    public UiActionResult discard(Game game, Player player, Map<ResourceType, Integer> resources, int requiredDiscard) {
        if (game == null || player == null) return UiActionResult.failure("Game atau pemain tidak tersedia.");
        int totalSelected = resources.values().stream().mapToInt(Integer::intValue).sum();
        if (totalSelected != requiredDiscard) {
            return UiActionResult.failure("Kamu (" + player.getName() + ") harus membuang tepat " +
                    requiredDiscard + " kartu (sekarang: " + totalSelected + ").");
        }
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() > 0 && !player.hasResource(entry.getKey(), entry.getValue())) {
                return UiActionResult.failure("Kamu tidak punya cukup " + entry.getKey().getDisplayName());
            }
        }
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() > 0) {
                game.discardResource(player, entry.getKey(), entry.getValue());
            }
        }
        return UiActionResult.success("Resource berhasil dibuang.");
    }
}
