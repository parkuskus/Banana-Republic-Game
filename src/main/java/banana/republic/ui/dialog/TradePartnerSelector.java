package banana.republic.ui.dialog;

import java.util.Map;
import java.util.Optional;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

public interface TradePartnerSelector {
    Optional<Player> chooseAcceptedTarget(Game game, Player active,
                                          Map<ResourceType, Integer> give,
                                          Map<ResourceType, Integer> receive);
}
