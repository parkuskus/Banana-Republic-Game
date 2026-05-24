package banana.republic.card;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.robber.Robber;

/**
 * Knight Card (Kartu Penjaga).
 * Memungkinkan pemain memindahkan Nimon Ungu tanpa penalti discard fase.
 *
 * Jumlah: 14 kartu
 * Efek: Pindahkan Nimon Ungu ke petak lain, boleh curi 1 kartu acak dari pemain terdekat.
 */
public class KnightCard extends DevelopmentCard {

    @Override
    public String getCardName() {
        return "Kartu Penjaga (Knight)";
    }

    @Override
    public String getDescription() {
        return "Pindahkan Nimon Ungu ke petak terrain lain. " +
                "Anda boleh mencuri 1 kartu sumber daya acak dari pemain yang punya bangunan " +
                "di sekitar lokasi baru Nimon Ungu.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null : "Player harus tidak null saat mainkan KnightCard";
        assert state != null : "GameState harus tidak null saat mainkan KnightCard";

        player.incrementKnightsPlayed();
        this.reveal();

        Board board = state.getBoard();
        if (board == null || board.getAllHexTiles().isEmpty()) {
            return;
        }

        HexTile currentRobberTile = board.getRobberTile().orElse(null);
        HexTile chosenTile = chooseKnightTarget(board, currentRobberTile, player);
        if (chosenTile == null) {
            return;
        }

        board.moveRobber(chosenTile);

        Robber robber = new Robber(chosenTile);
        var victims = robber.getEligibleVictims(player, board);
        if (!victims.isEmpty()) {
            robber.stealRandomResource(player, victims.get(0));
        }
    }

    @Override
    public boolean isPlayable() {
        return !isNewlyDrawn();
    }

    @Override
    public boolean isSecret() {
        // Knight terungkap ketika dimainkan
        // Tapi sebelum dimainkan, tersembunyi
        return !revealed;
    }

    @Override
    public CardType getCardType() {
        return CardType.KNIGHT;
    }

    private HexTile chooseKnightTarget(Board board, HexTile currentRobberTile, Player player) {
        HexTile preferred = null;

        for (HexTile tile : board.getAllHexTiles()) {
            if (tile == null || tile.equals(currentRobberTile)) {
                continue;
            }

            if (preferred == null) {
                preferred = tile;
            }

            if (!getEligibleVictimsAt(tile, board, player).isEmpty()) {
                return tile;
            }
        }

        return preferred;
    }

    private java.util.List<Player> getEligibleVictimsAt(HexTile tile, Board board, Player thief) {
        java.util.List<Player> victims = new java.util.ArrayList<>();
        for (var intersection : board.getAdjacentIntersections(tile)) {
            if (!intersection.hasBuilding()) {
                continue;
            }

            Player owner = intersection.getOwner();
            if (owner == null || owner.equals(thief) || victims.contains(owner)) {
                continue;
            }

            if (owner.getTotalResourceCount() > 0) {
                victims.add(owner);
            }
        }

        return victims;
    }
}
