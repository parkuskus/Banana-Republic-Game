package banana.republic.card.special;

import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;

/**
 * Abstract special card (Kartu Spesial).
 * Kartu Spesial adalah achievement cards yang dibawa oleh pemain dengan pencapaian tertentu.
 * Dua jenis kartu spesial:
 * - Longest Road (Jalan Terpanjang): 2 VP
 * - Largest Army (Pasukan Terbesar): 2 VP
 *
 * Karakteristik:
 * - Dipegang oleh 1 pemain (holder)
 * - Memberikan 2 Poin Prestasi bonus
 * - Bisa berpindah tangan jika pemain lain melakukan pencapaian lebih besar
 * - Bisa disisihkan (holder = null) jika ada kondisi seri khusus
 */
public abstract class SpecialCard {
    private static final int BONUS_POINTS = 2;

    protected Player holder;
    protected boolean active;

    /**
     * Constructor untuk special card.
     */
    public SpecialCard() {
        this.holder = null;
        this.active = false;
    }

    /**
     * Dapatkan pemain yang sedang memegang kartu ini.
     */
    public Player getHolder() {
        return holder;
    }

    /**
     * Cek apakah kartu sedang aktif (dipegang oleh pemain).
     */
    public boolean isActive() {
        return active && holder != null;
    }

    /**
     * Transfer kartu ke pemain baru.
     * Jika holder sebelumnya ada, kurangi 2 VP dari mereka.
     */
    public void transfer(Player newHolder) {
        if (holder != null) {
            // Revoke dari holder lama
            this.revoke();
        }

        this.holder = newHolder;
        this.active = (newHolder != null);
    }

    /**
     * Revoke kartu dari holder (kartu disisihkan, holder = null).
     */
    public void revoke() {
        this.holder = null;
        this.active = false;
    }

    /**
     * Dapatkan bonus poin yang diberikan kartu ini.
     */
    public int getBonusPoints() {
        return BONUS_POINTS;
    }

    /**
     * Get tipe kartu spesial (abstract, diimplementasi di subclass).
     */
    public abstract SpecialCardType getCardType();
}
