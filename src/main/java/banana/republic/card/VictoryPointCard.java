package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

/**
 * Victory point development card (Kartu Poin Prestasi Rahasia).
 * Jumlah: 5 kartu
 * Nilai: 1 Poin Prestasi per kartu
 * Sifat: Tersembunyi sampai pemain memenangkan permainan.
 *
 * PENGECUALIAN: Jika kartu ini membuat total VP ≥ 10, pemain BOLEH langsung mengungkap & menang
 * meski baru saja dibeli pada giliran yang sama.
 */
public class VictoryPointCard extends DevelopmentCard {
    private static final int POINT_VALUE = 1;

    /**
     * Constructor untuk Victory Point Card.
     */
    public VictoryPointCard() {
        super();
    }

    @Override
    public String getCardName() {
        return "Kartu Poin Prestasi Rahasia (Victory Point)";
    }

    @Override
    public String getDescription() {
        return "Kartu rahasia bernilai 1 Poin Prestasi. Tetap tersembunyi sampai akhir permainan " +
                "(atau sampai Anda kandekannya untuk menang).";
    }

    /**
     * Get nilai poin prestasi dari kartu ini.
     */
    public int getPointValue() {
        return POINT_VALUE;
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        // Victory Point Card tidak memiliki efek khusus yang diaplikasikan.
        // Nilai poin dihitung saat kalkulasi total Poin Prestasi (di VictoryPointCalculator).
        // Kartu ini hanya disimpan tersembunyi di tangan pemain.

        assert player != null : "Player harus tidak null saat mainkan VictoryPointCard";
        // Note: Tidak ada action yang perlu dilakukan di sini.
    }

    @Override
    public boolean isPlayable() {
        // Victory Point Card tidak "dimainkan" dalam arti mengeluarkan efek.
        // Nilai otomatis dihitung saat victory check.
        // Tapi ada PENGECUALIAN: jika kartu ini membuat total VP ≥ 10,
        // pemain boleh mengungkap dan langsung menang (bahkan jika baru diambil).
        //
        // Untuk implementasi sederhana: return true jika total VP >= 10
        // (delegasi ke GameController untuk handle logika ini)
        return true;
    }

    @Override
    public boolean isSecret() {
        // Victory Point Card SELALU secret (tersembunyi) sampai:
        // 1. Permainan berakhir (pemain lain sudah menang)
        // 2. Atau pemain itu sendiri menang dengan mengungkap kartu ini
        return true;
    }

    @Override
    public CardType getCardType() {
        return CardType.VICTORY_POINT;
    }
}
