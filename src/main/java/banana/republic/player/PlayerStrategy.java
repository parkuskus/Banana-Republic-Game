package banana.republic.player;

import java.util.List;
import java.util.Map;

import banana.republic.board.HexTile;
import banana.republic.core.GameState;
import banana.republic.resource.ResourceType;


public interface PlayerStrategy {

    List<Action> takeTurn(GameState state);

    Player chooseRobberTarget(GameState state, List<Player> candidates);

    HexTile chooseRobberPlacement(GameState state);

    /**
     * Menentukan apakah bot menerima tawaran dagang dari pemain lain.
     *
     * @param state    state permainan saat ini
     * @param offerer  pemain yang menawarkan
     * @param give     resource yang diberikan offerer ke bot
     * @param receive  resource yang diminta offerer dari bot
     * @return true jika bot menerima tawaran
     */
    default boolean shouldAcceptTrade(GameState state, Player offerer,
                                      Map<ResourceType, Integer> give,
                                      Map<ResourceType, Integer> receive) {
        return false;
    }

    /**
     * Menentukan tawaran balik (counter-offer) dari bot.
     *
     * @param state    state permainan saat ini
     * @param offerer  pemain yang menawarkan
     * @param give     resource yang diberikan offerer ke bot
     * @param receive  resource yang diminta offerer dari bot
     * @return counter-offer baru, atau null jika bot tidak ingin menawar balik
     */
    default TradeResponse counterTrade(GameState state, Player offerer,
                                       Map<ResourceType, Integer> give,
                                       Map<ResourceType, Integer> receive) {
        return null;
    }

    /**
     * Data class sederhana untuk respon trade bot.
     */
    class TradeResponse {
        public final Map<ResourceType, Integer> give;
        public final Map<ResourceType, Integer> receive;

        public TradeResponse(Map<ResourceType, Integer> give,
                             Map<ResourceType, Integer> receive) {
            this.give = give;
            this.receive = receive;
        }
    }
}
