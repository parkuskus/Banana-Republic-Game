package banana.republic.ui.command;

import banana.republic.core.Game;
import banana.republic.player.Player;

public class GameActionUiService {

    public UiActionResult buyDevelopmentCard(Game game) {
        if (game == null) return UiActionResult.failure("Game tidak tersedia.");
        game.buyDevelopmentCard(game.getActivePlayer());
        return UiActionResult.success("Berhasil membeli Kartu Temuan.");
    }

    public Player checkVictory(Game game) {
        if (game == null) return null;
        return game.declareVictory(game.getActivePlayer());
    }

    public Player endGame(Game game) {
        if (game == null) return null;
        return game.endGameByHighestVictoryPoints();
    }
}
