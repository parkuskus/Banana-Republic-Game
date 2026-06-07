package banana.republic.ui.command;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

public class BuildUiService {

    public BuildResult execute(Game game,
                               Player player,
                               Intersection intersection,
                               Path path,
                               boolean upgrade,
                               Map<ResourceType, Integer> expectedInitialResources) {
        if (game == null || player == null) {
            throw new IllegalStateException("Game atau pemain aktif tidak tersedia.");
        }

        if (intersection != null) {
            if (game.getCurrentPhase().isSetupPhase()) {
                boolean grantsInitialResources = game.getCurrentPhase() == GamePhase.SETUP_SECOND_ROUND;
                Map<ResourceType, Integer> before = snapshotPlayerResourceCounts(player);
                game.placeInitialSettlement(player, intersection);
                if (grantsInitialResources) {
                    syncInitialResources(game, player, before, expectedInitialResources);
                }
                return new BuildResult(true, false);
            }

            if (upgrade) game.buildCity(player, intersection);
            else game.buildSettlement(player, intersection);
            return new BuildResult(false, false);
        }

        if (path != null) {
            if (game.getCurrentPhase().isSetupPhase()) {
                game.placeInitialRoad(player, path);
                return new BuildResult(false, true);
            }
            game.buildRoad(player, path);
        }

        return new BuildResult(false, false);
    }

    private Map<ResourceType, Integer> snapshotPlayerResourceCounts(Player player) {
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            counts.put(type, player.getResourceCount(type));
        }
        return counts;
    }

    private void syncInitialResources(Game game,
                                      Player player,
                                      Map<ResourceType, Integer> beforeResources,
                                      Map<ResourceType, Integer> expectedResources) {
        if (expectedResources == null) return;

        List<String> gainedText = new java.util.ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            int before = beforeResources.getOrDefault(type, 0);
            int actualGain = player.getResourceCount(type) - before;
            int expectedGain = expectedResources.getOrDefault(type, 0);

            if (actualGain > expectedGain) {
                int extra = actualGain - expectedGain;
                player.removeResource(type, extra);
                game.getBank().returnResource(type, extra);
            } else if (expectedGain > actualGain) {
                int missing = expectedGain - actualGain;
                int available = Math.min(missing, game.getBank().getCount(type));
                if (available > 0) {
                    game.getBank().takeResource(type, available);
                    player.addResource(type, available);
                }
            }

            int finalGain = player.getResourceCount(type) - before;
            if (finalGain > 0) {
                gainedText.add(finalGain + " " + type.getDisplayName());
            }
        }

        if (!gainedText.isEmpty()) {
            game.getGameLog().addEntry(
                    LogEntry.EventType.RESOURCE_PRODUCTION,
                    player.getName(),
                    "Resource awal dari Pos Pantau kedua: " + String.join(", ", gainedText) + "."
            );
        }
    }

    public record BuildResult(boolean setupSettlementPlaced, boolean setupRoadPlaced) {
    }
}
