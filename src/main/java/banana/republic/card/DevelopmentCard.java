package banana.republic.card;

/**
 * Abstract base class untuk semua Kartu Temuan Dr. Neroifa.
 *
 * <p>Mengelola state umum kartu: {@code revealed} (terungkap) dan
 * {@code newlyDrawn} (baru diambil pada giliran ini).
 *
 * <p>Subclass wajib mengimplementasikan {@link #getCardName()},
 * {@link #getDescription()}, {@link #applyEffect(GameState, Player)},
 * dan {@link #getCardType()}.
 */
public abstract class DevelopmentCard implements ExperimentCard {
    protected boolean revealed;
    protected boolean newlyDrawn;

    /**
     * Constructor default.
     *
     * <p>Semua kartu dimulai dalam kondisi belum terungkap
     * dan belum diambil ({@code newlyDrawn = true}).
     */
    public DevelopmentCard() {
        this.revealed = false;
        this.newlyDrawn = true;
    }

    /**
     * Mengungkap kartu ini.
     *
     * <p>Digunakan untuk kartu Knight dan kartu spesial lainnya
     * saat dimainkan.
     */
    public void reveal() {
        this.revealed = true;
    }

    /**
     * Cek apakah kartu sudah terungkap.
     *
     * @return {@code true} jika sudah di-{@link #reveal()}
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Cek apakah kartu baru saja diambil pada giliran ini.
     *
     * @return {@code true} jika kartu baru diambil dari deck
     */
    public boolean isNewlyDrawn() {
        return newlyDrawn;
    }

    /**
     * Mengatur flag {@code newlyDrawn}.
     *
     * <p>Digunakan untuk validasi "tidak boleh dimainkan saat giliran
     * yang sama".
     *
     * @param flag {@code true} jika baru diambil, {@code false} jika sudah boleh dimainkan
     */
    public void setNewlyDrawn(boolean flag) {
        this.newlyDrawn = flag;
    }

    /**
     * Semua development card bersifat secret (tersembunyi) kecuali
     * {@link VictoryPointCard} yang selalu rahasia.
     *
     * <p>Subclass dapat meng-override jika perlu behavior berbeda.
     *
     * @return {@code true} secara default
     */
    @Override
    public boolean isSecret() {
        return true;
    }
}
