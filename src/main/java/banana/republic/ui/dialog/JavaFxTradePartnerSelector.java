package banana.republic.ui.dialog;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import banana.republic.core.Game;
import banana.republic.player.BotPlayer;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import javafx.scene.control.ChoiceDialog;

public class JavaFxTradePartnerSelector implements TradePartnerSelector {

    private final UiDialogs dialogs;

    public JavaFxTradePartnerSelector(UiDialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public Optional<Player> chooseAcceptedTarget(Game game, Player active,
                                                 Map<ResourceType, Integer> give,
                                                 Map<ResourceType, Integer> receive) {
        List<Player> otherPlayers = game.getPlayers().stream()
                .filter(player -> !player.equals(active))
                .toList();

        if (otherPlayers.isEmpty()) {
            dialogs.showError("Tidak ada pemain lain.");
            return Optional.empty();
        }

        ChoiceDialog<Player> dialog = new ChoiceDialog<>(otherPlayers.get(0), otherPlayers);
        dialog.setTitle("Pilih Target Trade");
        dialog.setHeaderText("Pilih pemain yang akan menerima tawaran dagangmu.");
        dialog.setContentText("Target:");
        Optional<Player> chosenTarget = dialog.showAndWait();
        if (chosenTarget.isEmpty()) return Optional.empty();

        Player target = chosenTarget.get();
        if (target instanceof BotPlayer botPlayer) {
            boolean accepted = botPlayer.getStrategy().shouldAcceptTrade(game.getState(), active, give, receive);
            if (!accepted) {
                dialogs.showError(target.getName() + " (bot) menolak tawaran dagang.");
                return Optional.empty();
            }
            return Optional.of(target);
        }

        boolean accepted = dialogs.confirm(
                "Konfirmasi Trade",
                "Tawaran dari " + active.getName() + " ke " + target.getName(),
                target.getName() + ", apakah kamu menerima trade ini?");
        if (!accepted) {
            dialogs.showError(target.getName() + " menolak tawaran dagang.");
            return Optional.empty();
        }
        return Optional.of(target);
    }
}
