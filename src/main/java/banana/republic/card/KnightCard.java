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
        java.util.List<HexTile> candidates = new java.util.ArrayList<>();
        for (HexTile tile : board.getAllHexTiles()) {
            if (tile != null && !tile.equals(currentRobberTile)) {
                candidates.add(tile);
            }
        }

        HexTile chosenTile = state.chooseKnightTarget(player, java.util.Collections.unmodifiableList(candidates));
        if (chosenTile == null) {
            return;
        }

        board.moveRobber(chosenTile);

        Robber robber = new Robber(chosenTile);
        var victims = robber.getEligibleVictims(player, board);
        if (!victims.isEmpty()) {
            Player chosenVictim = state.chooseKnightVictim(player, chosenTile, java.util.Collections.unmodifiableList(victims));
            if (chosenVictim != null && victims.contains(chosenVictim)) {
                robber.stealRandomResource(player, chosenVictim);
            }
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
}
