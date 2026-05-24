package banana.republic.card;

import banana.republic.board.Board;
import banana.republic.board.Path;
import banana.republic.building.PlayerSupply;
import banana.republic.building.Road;
import banana.republic.core.GameState;
import banana.republic.player.Player;

/**
 * Road building progress card.
 * Refer to class-diagram/Module3_Cards_Robber_Timer.puml for full specification.
 */
public class RoadBuildingCard extends ProgressCard {
    @Override
    public String getCardName() {
        return "Kartu Konstruksi Cepat (Road Building)";
    }

    @Override
    public String getDescription() {
        return "Tempatkan 2 Pipa Transportasi baru secara GRATIS di mana pun sesuai aturan pembangunan.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null : "Player harus tidak null saat mainkan RoadBuildingCard";
        assert state != null : "GameState harus tidak null saat mainkan RoadBuildingCard";

        this.reveal();

        Board board = state.getBoard();
        PlayerSupply supply = player.getSupply();
        if (board == null || supply == null || !supply.canBuildRoad()) {
            this.consume();
            return;
        }

        int roadsPlaced = 0;
        for (Path path : board.getBuildableRoadPaths(player)) {
            if (roadsPlaced >= 2 || !supply.canBuildRoad()) {
                break;
            }

            if (!path.isEmpty()) {
                continue;
            }

            Road road = supply.takeRoad();
            path.placeRoad(road);
            roadsPlaced++;
        }

        this.consume();
    }

    @Override
    public CardType getCardType() {
        return CardType.ROAD_BUILDING;
    }
}
