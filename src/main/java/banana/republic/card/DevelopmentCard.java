package banana.republic.card;

/**
 * Abstract development card.
 * Base class untuk semua Kartu Temuan Dr. Neroifa.
 * State management: revealed, newlyDrawn untuk tracking kartu.
 */
public abstract class DevelopmentCard implements ExperimentCard {
    protected boolean revealed;
    protected boolean newlyDrawn;

    /**
     * Constructor untuk development card.
     * Semua kartu dimulai belum terungkap dan belum diambil (fresh).
     */
    public DevelopmentCard() {
        this.revealed = false;
        this.newlyDrawn = true;
    }

    /**
     * Reveal kartu (untuk kartu knight dan special).
     */
    public void reveal() {
        this.revealed = true;
    }

    /**
     * Cek apakah kartu sudah terungkap.
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Cek apakah kartu baru saja diambil pada giliran ini.
     */
    public boolean isNewlyDrawn() {
        return newlyDrawn;
    }

    /**
     * Set flag newlyDrawn.
     * Digunakan untuk validasi "tidak boleh dimainkan saat giliran yang sama".
     */
    public void setNewlyDrawn(boolean flag) {
        this.newlyDrawn = flag;
    }

    /**
     * Semua development card, kecuali VictoryPointCard, bersifat secret (tersembunyi).
     * Override di subclass jika perlu behavior berbeda.
     */
    @Override
    public boolean isSecret() {
        return true;
    }
}
