package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

/**
 * Kartu Poin Prestasi Rahasia (Victory Point Card).
 *
 * <p>Bernilai 1 Poin Prestasi per kartu. Sifatnya selalu tersembunyi
 * sampai pemain memenangkan permainan.
 *
 * <p><strong>Pengecualian:</strong> Jika kartu ini membuat total VP ≥ 10,
 * pemain BOLEH langsung mengungkap dan menang meski baru saja dibeli
 * pada giliran yang sama.
 *
 * <p>Komposisi deck: 5 kartu.
 */
public class VictoryPointCard extends DevelopmentCard {
    private static final int POINT_VALUE = 1;

    /**
     * Constructor default.
     */
    public VictoryPointCard() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCardName() {
        return "Kartu Poin Prestasi Rahasia (Victory Point)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Kartu rahasia bernilai 1 Poin Prestasi. Tetap tersembunyi sampai akhir permainan " +
                "(atau sampai Anda kandekannya untuk menang).";
    }

    /**
     * Mengembalikan nilai poin prestasi dari kartu ini.
     *
     * @return selalu {@value #POINT_VALUE}
     */
    public int getPointValue() {
        return POINT_VALUE;
    }

    /**
     * Victory Point Card tidak memiliki efek khusus yang diaplikasikan.
     *
     * <p>Nilai poin dihitung saat kalkulasi total Poin Prestasi
     * (di {@code VictoryPointCalculator}). Kartu ini hanya disimpan
     * tersembunyi di tangan pemain.
     *
     * @param state  state permainan saat ini
     * @param player pemain yang memainkan kartu
     */
    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null : "Player harus tidak null saat mainkan VictoryPointCard";
    }

    /**
     * Victory Point Card tidak bisa dimainkan jika baru saja diambil.
     *
     * @return {@code true} jika bukan newly-drawn
     */
    @Override
    public boolean isPlayable() {
        return !isNewlyDrawn();
    }

    /**
     * Victory Point Card SELALU secret (tersembunyi).
     *
     * @return selalu {@code true}
     */
    @Override
    public boolean isSecret() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardType getCardType() {
        return CardType.VICTORY_POINT;
    }
}
