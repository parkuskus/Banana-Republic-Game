package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

/**
 * Interface untuk semua Kartu Temuan Dr. Neroifa.
 *
 * <p>Setiap kartu harus dapat memberikan nama, deskripsi, menerapkan efek,
 * dan melaporkan status playable/secret-nya.
 *
 * <p>Digunakan oleh {@link CardDeck} untuk mengelola deck dan oleh
 * {@code PluginLoader} untuk memuat kartu plugin eksternal.
 */
public interface ExperimentCard {

    /**
     * Mengembalikan nama tampilan kartu.
     *
     * @return nama kartu dalam Bahasa Indonesia
     */
    String getCardName();

    /**
     * Mengembalikan deskripsi efek kartu.
     *
     * @return deskripsi teks bebas
     */
    String getDescription();

    /**
     * Menerapkan efek kartu ke state permainan.
     *
     * @param state  state permainan read-only yang diekspor ke kartu
     * @param player pemain yang memainkan kartu
     */
    void applyEffect(GameState state, Player player);

    /**
     * Cek apakah kartu bisa dimainkan saat ini.
     *
     * <p>Aturan: tidak bisa dimainkan jika baru diambil ({@code newlyDrawn})
     * kecuali untuk Victory Point.
     *
     * @return {@code true} jika boleh dimainkan
     */
    boolean isPlayable();

    /**
     * Cek apakah kartu bersifat secret (tersembunyi dari pemain lain).
     *
     * <p>Victory Point cards selalu secret; Knight terungkap jika dimainkan.
     *
     * @return {@code true} jika pemain lain tidak boleh melihat kartu ini
     */
    boolean isSecret();

    /**
     * Mengembalikan tipe kartu untuk identification dan categorization.
     *
     * @return {@link CardType} milik kartu ini
     */
    CardType getCardType();

    /**
     * Mengecek apakah kartu ini berasal dari plugin eksternal.
     * Default adalah false. Kartu plugin harus meng-override method ini dan
     * mengembalikan true agar tidak menyebabkan error saat penyimpanan permainan.
     *
     * @return {@code true} jika kartu berasal dari plugin
     */
    default boolean isPluginCard() {
        return false;
    }
}
