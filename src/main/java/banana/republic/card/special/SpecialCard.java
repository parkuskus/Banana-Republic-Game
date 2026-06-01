package banana.republic.card.special;

import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;

/**
 * Abstract special card (Kartu Spesial).
 *
 * <p>Kartu Spesial adalah achievement card yang dibawa oleh pemain
 * dengan pencapaian tertentu. Dua jenis kartu spesial:
 * <ul>
 *   <li>{@link LongestRoadCard} (Jalan Terpanjang): +2 Poin Prestasi</li>
 *   <li>{@link LargestArmyCard} (Pasukan Terbesar): +2 Poin Prestasi</li>
 * </ul>
 *
 * <p>Karakteristik:
 * <ul>
 *   <li>Dipegang oleh 1 pemain ({@code holder})</li>
 *   <li>Memberikan 2 Poin Prestasi bonus</li>
 *   <li>Bisa berpindah tangan jika pemain lain melakukan pencapaian lebih besar</li>
 *   <li>Bisa disisihkan ({@code holder = null}) jika ada kondisi seri khusus</li>
 * </ul>
 */
public abstract class SpecialCard {
    private static final int BONUS_POINTS = 2;

    protected Player holder;
    protected boolean active;

    /**
     * Constructor default.
     */
    public SpecialCard() {
        this.holder = null;
        this.active = false;
    }

    /**
     * Mengembalikan pemain yang sedang memegang kartu ini.
     *
     * @return {@link Player} holder, atau {@code null} jika tidak ada
     */
    public Player getHolder() {
        return holder;
    }

    /**
     * Cek apakah kartu sedang aktif (dipegang oleh pemain).
     *
     * @return {@code true} jika {@code active} dan {@code holder != null}
     */
    public boolean isActive() {
        return active && holder != null;
    }

    /**
     * Mentransfer kartu ke pemain baru.
     *
     * <p>Jika holder sebelumnya ada, {@link #revoke()} dipanggil terlebih dahulu.
     *
     * @param newHolder pemain baru yang memegang kartu, atau {@code null} untuk melepaskan
     */
    public void transfer(Player newHolder) {
        if (holder != null) {
            this.revoke();
        }

        this.holder = newHolder;
        this.active = (newHolder != null);
    }

    /**
     * Mencabut kartu dari holder (kartu disisihkan, {@code holder = null}).
     */
    public void revoke() {
        this.holder = null;
        this.active = false;
    }

    /**
     * Mengembalikan bonus poin yang diberikan kartu ini.
     *
     * @return selalu {@value #BONUS_POINTS}
     */
    public int getBonusPoints() {
        return BONUS_POINTS;
    }

    /**
     * Mengembalikan tipe kartu spesial.
     *
     * @return {@link SpecialCardType} milik subclass
     */
    public abstract SpecialCardType getCardType();
}
