package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

public interface ExperimentCard {
    String getCardName();

    String getDescription();

    void applyEffect(GameState state, Player player);
}
