package banana.republic.card;

/**
 * Abstract progress card (Kartu Inovasi).
 *
 * <p>Progress Cards dapat dimainkan, mengambil efek spesifik, lalu
 * dikonsumsi (habis). Berbeda dengan {@link KnightCard} yang dapat
 * dimainkan berkali-kali jika ada (tidak mungkin karena dari deck).
 *
 * <p>Jenis Progress Card:
 * <ul>
 *   <li>{@link RoadBuildingCard} — Bangun 2 pipa gratis</li>
 *   <li>{@link MonopolyCard} — Ambil semua kartu satu jenis dari pemain lain</li>
 * </ul>
 */
public abstract class ProgressCard extends DevelopmentCard {
    protected boolean consumed;

    /**
     * Constructor default.
     */
    public ProgressCard() {
        super();
        this.consumed = false;
    }

    /**
     * Menandai kartu sebagai consumed (sudah digunakan dan keluar dari permainan).
     */
    public void consume() {
        this.consumed = true;
    }

    /**
     * Cek apakah kartu sudah dikonsumsi.
     *
     * @return {@code true} jika sudah {@link #consume()}d
     */
    public boolean isConsumed() {
        return consumed;
    }

    /**
     * Progress Card hanya bisa dimainkan jika:
     * <ol>
     *   <li>Belum dikonsumsi</li>
     *   <li>Bukan newly-drawn (atau pengecualian khusus untuk VP Card)</li>
     * </ol>
     *
     * <p>Subclass dapat meng-override untuk tambahan validasi
     * (misal: {@link MonopolyCard} butuh select resource).
     *
     * @return {@code true} jika playable
     */
    @Override
    public boolean isPlayable() {
        return !isNewlyDrawn() && !isConsumed();
    }

    /**
     * Progress Cards bersifat secret sampai dimainkan atau dikonsumsi.
     *
     * @return {@code false} jika sudah revealed atau consumed
     */
    @Override
    public boolean isSecret() {
        return !revealed && !consumed;
    }
}
