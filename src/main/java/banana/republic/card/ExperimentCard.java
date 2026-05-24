package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

public interface ExperimentCard {
    String getCardName();

    String getDescription();

    void applyEffect(GameState state, Player player);

    /**
     * Cek apakah kartu bisa dimainkan sekarang.
     * Aturan: tidak bisa dimainkan jika baru diambil (newlyDrawn) kecuali untuk Victory Point.
     */
    boolean isPlayable();

    /**
     * Cek apakah kartu bersifat secret (tersembunyi dari pemain lain).
     * Victory Point cards selalu secret; Knight terungkap jika dimainkan.
     */
    boolean isSecret();

    /**
     * Dapatkan tipe kartu untuk identification dan categorization.
     */
    CardType getCardType();
}
