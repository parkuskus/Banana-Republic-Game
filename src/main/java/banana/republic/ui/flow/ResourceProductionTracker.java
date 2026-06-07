package banana.republic.ui.flow;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

public class ResourceProductionTracker {

    public Map<Player, Map<ResourceType, Integer>> snapshot(Game game) {
        Map<Player, Map<ResourceType, Integer>> snapshot = new HashMap<>();
        if (game == null) return snapshot;
        for (Player player : game.getPlayers()) {
            Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
            for (ResourceType type : ResourceType.values()) {
                counts.put(type, player.getResourceCount(type));
            }
            snapshot.put(player, counts);
        }
        return snapshot;
    }

    public String summarize(Game game, Map<Player, Map<ResourceType, Integer>> beforeRoll) {
        if (game == null) return "Tidak ada resource yang diproduksi dari hasil dadu ini.";
        List<String> producedByPlayer = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            Map<ResourceType, Integer> before = beforeRoll.get(player);
            if (before == null) continue;

            List<String> gainedResources = new ArrayList<>();
            for (ResourceType type : ResourceType.values()) {
                int beforeCount = before.getOrDefault(type, 0);
                int gained = player.getResourceCount(type) - beforeCount;
                if (gained > 0) {
                    gainedResources.add(gained + " " + type.getDisplayName());
                }
            }

            if (!gainedResources.isEmpty()) {
                producedByPlayer.add(player.getName() + " mendapat " + String.join(", ", gainedResources));
            }
        }
        if (producedByPlayer.isEmpty()) {
            return "Tidak ada resource yang diproduksi dari hasil dadu ini.";
        }
        return "Produksi resource: " + String.join("; ", producedByPlayer) + ".";
    }
}
