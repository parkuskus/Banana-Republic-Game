package banana.republic.card;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.robber.Robber;

/**
 * Kartu Penjaga (Knight Card).
 *
 * <p>Efek: Pindahkan Nimon Ungu ke petak terrain lain dan curi 1 kartu
 * sumber daya acak dari pemain yang punya bangunan di sekitar lokasi baru.
 *
 * <p>Komposisi deck: 14 kartu.
 */
public class KnightCard extends DevelopmentCard {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCardName() {
        return "Kartu Penjaga (Knight)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Pindahkan Nimon Ungu ke petak terrain lain. " +
                "Anda boleh mencuri 1 kartu sumber daya acak dari pemain yang punya bangunan " +
                "di sekitar lokasi baru Nimon Ungu.";
    }

    /**
     * Menerapkan efek Knight: pindahkan Nimon Ungu dan curi resource.
     *
     * @param state  state permainan saat ini (tidak boleh {@code null})
     * @param player pemain yang memainkan kartu (tidak boleh {@code null})
     */
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

    /**
     * Knight Card tidak bisa dimainkan jika baru saja diambil dari deck
     * pada giliran yang sama.
     *
     * @return {@code true} jika kartu bukan newly-drawn
     */
    @Override
    public boolean isPlayable() {
        return !isNewlyDrawn();
    }

    /**
     * Knight Card terungkap saat dimainkan; sebelumnya bersifat secret.
     *
     * @return {@code false} jika sudah {@link #reveal()}ed, {@code true} sebaliknya
     */
    @Override
    public boolean isSecret() {
        return !revealed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardType getCardType() {
        return CardType.KNIGHT;
    }
}
