package banana.republic.card;

/**
 * Abstract progress card (Kartu Inovasi).
 * Progress Cards dapat dimainkan, mengambil efek spesifik, lalu dikonsumsi (habis).
 * Berbeda dengan Knight Yang dapat dimainkan berkali-kali jika ada (tidak mungkin karena dari deck).
 *
 * Jenis Progress Card:
 * - Road Building: Bangun 2 pipa gratis
 * - Monopoly: Ambil semua kartu satu jenis dari pemain lain
 */
public abstract class ProgressCard extends DevelopmentCard {
    protected boolean consumed;

    /**
     * Constructor untuk progress card.
     */
    public ProgressCard() {
        super();
        this.consumed = false;
    }

    /**
     * Mark kartu sebagai consumed (sudah digunakan dan keluar permainan).
     */
    public void consume() {
        this.consumed = true;
    }

    /**
     * Cek apakah kartu sudah dikonsumsi.
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Progress Card hanya bisa dimainkan jika:
     * 1. Belum dikonsumsi
     * 2. Bukan newly drawn (atau pengecualian khusus untuk VP Card)
     * <p>
     * Note: Subclass akan override untuk tambahan validasi (mis: Monopoly butuh select resource)
     */
    @Override
    public boolean isPlayable() {
        // General rule: tidak bisa dimainkan jika baru diambil
        return !isNewlyDrawn() && !isConsumed();
    }

    /**
     * Progress Cards juga tersembunyi sampai dimainkan.
     */
    @Override
    public boolean isSecret() {
        return !revealed && !consumed;
    }
}
