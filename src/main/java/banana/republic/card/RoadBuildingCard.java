package banana.republic.card;

import banana.republic.board.Board;
import banana.republic.board.Path;
import banana.republic.building.PlayerSupply;
import banana.republic.building.Road;
import banana.republic.core.GameState;
import banana.republic.core.LogEntry;
import banana.republic.player.Player;

/**
 * Kartu Konstruksi Cepat (Road Building Progress Card).
 *
 * <p>Efek: Tempatkan 2 Pipa Transportasi baru secara GRATIS di mana pun
 * sesuai aturan pembangunan.
 *
 * <p>Komposisi deck: 3 kartu.
 */
public class RoadBuildingCard extends ProgressCard {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCardName() {
        return "Kartu Konstruksi Cepat (Road Building)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Tempatkan 2 Pipa Transportasi baru secara GRATIS di mana pun sesuai aturan pembangunan.";
    }

    /**
     * Menerapkan efek Road Building: membangun hingga 2 pipa gratis
     * di lokasi yang valid.
     *
     * @param state  state permainan saat ini
     * @param player pemain yang memainkan kartu
     */
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

        java.util.List<Path> candidates = board.getBuildableRoadPaths(player);
        java.util.List<Path> selectedPaths = state.chooseRoadBuildingPaths(
            player,
            java.util.Collections.unmodifiableList(candidates),
            2
        );

        if (selectedPaths == null || selectedPaths.isEmpty()) {
            this.consume();
            return;
        }

        int roadsPlaced = 0;
        for (Path path : selectedPaths) {
            if (path == null || roadsPlaced >= 2 || !supply.canBuildRoad()) {
                break;
            }

            if (!candidates.contains(path) || !path.isEmpty()) {
                continue;
            }

            Road road = supply.takeRoad();
            path.placeRoad(road);
            state.getGameLog().addEntry(
                LogEntry.EventType.BUILD,
                player.getName(),
                player.getName() + " membangun Pipa Transportasi (Kartu Konstruksi Cepat) di path #"
                    + path.getId()
            );
            roadsPlaced++;
        }

        this.consume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardType getCardType() {
        return CardType.ROAD_BUILDING;
    }
}
