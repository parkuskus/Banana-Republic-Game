package banana.republic.ui.command;

import banana.republic.card.ExperimentCard;
import banana.republic.card.KnightCard;
import banana.republic.card.RoadBuildingCard;
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

    public UiActionResult playKnightForManualRobber(Game game, Player player, KnightCard card) {
        if (game == null) return UiActionResult.failure("Game tidak tersedia.");
        if (player == null) return UiActionResult.failure("Tidak ada pemain aktif.");
        if (card == null) return UiActionResult.failure("Pilih Kartu Penjaga yang valid.");
        try {
            game.playKnightCardForManualRobber(player, card);
            return UiActionResult.success("Kartu Penjaga dimainkan. Pindahkan Nimon Ungu.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return UiActionResult.failure(e.getMessage());
        }
    }

    public UiActionResult playRoadBuildingForManualPlacement(Game game, Player player, RoadBuildingCard card) {
        if (game == null) return UiActionResult.failure("Game tidak tersedia.");
        if (player == null) return UiActionResult.failure("Tidak ada pemain aktif.");
        if (card == null) return UiActionResult.failure("Pilih Kartu Konstruksi Cepat yang valid.");
        try {
            game.playRoadBuildingCardForManualPlacement(player, card);
            return UiActionResult.success("Konstruksi Cepat dimainkan. Pilih 2 Pipa gratis.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return UiActionResult.failure(e.getMessage());
        }
    }
}
