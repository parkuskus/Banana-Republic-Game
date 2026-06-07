package banana.republic.ui.command;

import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.player.Player;

public class CardUiService {

    public UiActionResult playSelectedCard(Game game, Player player, ExperimentCard card) {
        if (game == null) return UiActionResult.failure("Game tidak tersedia.");
        if (player == null) return UiActionResult.failure("Tidak ada pemain aktif.");
        if (card == null) return UiActionResult.failure("Pilih kartu yang valid untuk dimainkan.");
        try {
            game.playCard(player, card);
            return UiActionResult.success("Kartu berhasil dimainkan.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return UiActionResult.failure(e.getMessage());
        }
    }
}
