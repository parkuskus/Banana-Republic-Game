package banana.republic.ui.command;

import banana.republic.core.Game;
import banana.republic.core.LogEntry;
import banana.republic.player.Player;

public class StealUiService {

    public UiActionResult steal(Game game, Player thief, Player victim) {
        if (game == null || thief == null || victim == null) {
            return UiActionResult.failure("Game atau pemain tidak tersedia.");
        }
        if (victim.equals(thief)) {
            return UiActionResult.failure("Tidak bisa mencuri dari diri sendiri.");
        }
        try {
            if (victim.getTotalResourceCount() > 0) {
                game.getRobber().stealRandomResource(thief, victim);
                game.getGameLog().addEntry(
                        LogEntry.EventType.STEAL,
                        thief.getName(),
                        thief.getName() + " mencuri resource dari " + victim.getName());
            }
            return UiActionResult.success("Steal selesai.");
        } catch (Exception e) {
            return UiActionResult.failure(e.getMessage());
        }
    }
}
