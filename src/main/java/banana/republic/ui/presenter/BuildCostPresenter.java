package banana.republic.ui.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import javafx.scene.control.Label;

public class BuildCostPresenter {

    private final Label leftLabel;
    private final Label rightLabel;

    public BuildCostPresenter(Label leftLabel, Label rightLabel) {
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
    }

    public void render(Game game) {
        if (leftLabel == null || rightLabel == null) return;
        if (game == null || game.getActivePlayer() == null) {
            leftLabel.setText("Tidak ada game aktif.");
            rightLabel.setText("");
            return;
        }

        Player active = game.getActivePlayer();
        leftLabel.setText(
                costLine("Pos Pantau", active, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1,
                        ResourceType.WHEAT, 1,
                        ResourceType.BANANA, 1
                )) + "\n" +
                costLine("Pipa", active, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1
                ))
        );
        rightLabel.setText(
                costLine("Laboratorium", active, Map.of(
                        ResourceType.WHEAT, 2,
                        ResourceType.ORE, 3
                )) + "\n" +
                costLine("Kartu Temuan", active, Map.of(
                        ResourceType.ORE, 1,
                        ResourceType.WHEAT, 1,
                        ResourceType.BANANA, 1
                ))
        );
    }

    private String costLine(String label, Player player, Map<ResourceType, Integer> cost) {
        return label + ": " + formatResourceCost(cost) + " (" + (canPay(player, cost) ? "Bisa" : "Kurang") + ")";
    }

    private String formatResourceCost(Map<ResourceType, Integer> cost) {
        List<String> parts = new ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            int amount = cost.getOrDefault(type, 0);
            if (amount > 0) {
                parts.add(amount + " " + type.getDisplayName());
            }
        }
        return String.join(", ", parts);
    }

    private boolean canPay(Player player, Map<ResourceType, Integer> cost) {
        if (player == null) return false;
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (!player.hasResource(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
