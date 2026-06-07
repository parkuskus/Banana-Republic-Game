package banana.republic.ui.command;

import java.util.Map;
import java.util.EnumMap;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import banana.republic.trade.TradeOffer;

public class TradeUiService implements TradeCommandService {

    public UiActionResult executeMaritime(Game game, Player active, Map<ResourceType, Integer> give,
                                          Map<ResourceType, Integer> receive) {
        if (game == null || active == null) return UiActionResult.failure("Trade tidak valid.");
        ResourceType sellType = singlePositiveType(give);
        ResourceType buyType = singlePositiveType(receive);
        if (sellType == null || buyType == null) {
            return UiActionResult.failure("Trade Maritim hanya dapat menukar 1 jenis sumber daya dengan 1 jenis lainnya.");
        }
        int sellAmount = give.getOrDefault(sellType, 0);
        int buyAmount = receive.getOrDefault(buyType, 0);
        int ratio = game.getTradeRatio(active, sellType);
        int requiredSellAmount = ratio * buyAmount;
        if (buyAmount <= 0 || sellAmount != requiredSellAmount) {
            return UiActionResult.failure("Trade Maritim membutuhkan " + requiredSellAmount + " "
                    + sellType.getDisplayName() + " untuk menerima " + buyAmount + " "
                    + buyType.getDisplayName() + " (rasio " + ratio + ":1).");
        }
        try {
            for (int i = 0; i < buyAmount; i++) {
                var result = game.tradeWithBank(active, sellType, buyType);
                if (!result.isValid()) {
                    return UiActionResult.failure("Trade Maritim gagal: " + result.getReason());
                }
            }
            return UiActionResult.success("Trade Maritim berhasil.");
        } catch (Exception e) {
            return UiActionResult.failure("Error: " + e.getMessage());
        }
    }

    public UiActionResult executeDomestic(Game game, Player active, Player target,
                                          Map<ResourceType, Integer> give,
                                          Map<ResourceType, Integer> receive) {
        if (game == null || active == null || target == null) return UiActionResult.failure("Trade tidak valid.");
        try {
            TradeOffer offer = new TradeOffer(active, target, positiveOnly(give), positiveOnly(receive));
            var makeResult = game.makeTradeOffer(offer);
            if (!makeResult.isValid()) return UiActionResult.failure(makeResult.getReason());

            var acceptResult = game.acceptTradeOffer(target);
            return acceptResult.isValid()
                    ? UiActionResult.success("Trade berhasil.")
                    : UiActionResult.failure(acceptResult.getReason());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return UiActionResult.failure(e.getMessage());
        }
    }

    private ResourceType singlePositiveType(Map<ResourceType, Integer> resources) {
        ResourceType type = null;
        int count = 0;
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() > 0) {
                type = entry.getKey();
                count++;
            }
        }
        return count == 1 ? type : null;
    }

    private Map<ResourceType, Integer> positiveOnly(Map<ResourceType, Integer> resources) {
        Map<ResourceType, Integer> cleaned = new EnumMap<>(ResourceType.class);
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                cleaned.put(entry.getKey(), entry.getValue());
            }
        }
        return cleaned;
    }
}
