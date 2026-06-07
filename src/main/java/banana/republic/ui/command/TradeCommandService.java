package banana.republic.ui.command;

import java.util.Map;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

public interface TradeCommandService {
    UiActionResult executeMaritime(Game game, Player active, Map<ResourceType, Integer> give,
                                   Map<ResourceType, Integer> receive);

    UiActionResult executeDomestic(Game game, Player active, Player target,
                                   Map<ResourceType, Integer> give,
                                   Map<ResourceType, Integer> receive);
}
