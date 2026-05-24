package banana.republic.card.special;

import java.util.*;

import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;

/**
 * Largest Army special card (Pasukan Terbesar).
 * Jumlah: 1 kartu
 * Bonus: 2 Poin Prestasi
 *
 * Syarat: Pemain harus sudah memainkan minimal 3 Kartu Penjaga (Knight).
 *
 * Mekanisme:
 * - Pemain pertama yang play 3 Knight mendapat kartu ini
 * - Jika pemain lain play lebih banyak Knight, kartu berpindah (+ ambil 2 VP)
 * - Aturan seri: Holder lama tetap sampai ada yang melampaui
 */
public class LargestArmyCard extends SpecialCard {
    private static final int MINIMUM_KNIGHTS = 3;
    private int currentQualifyingCount;

    /**
     * Constructor untuk Largest Army Card.
     */
    public LargestArmyCard() {
        super();
        this.currentQualifyingCount = 0;
    }

    @Override
    public SpecialCardType getCardType() {
        return SpecialCardType.LARGEST_ARMY;
    }

    /**
     * Evaluasi dan tentukan siapa yang berhak memegang kartu ini.
     * Harus dipanggil setiap kali ada pemain yang play Knight card.
     */
    public void evaluate(List<Player> players) {
        assert players != null : "Players list tidak boleh null";

        // Cari pemain dengan knights dimainkan terbanyak (>= MINIMUM_KNIGHTS)
        int maxKnights = 0;
        Player topPlayer = null;

        for (Player player : players) {
            int knightsPlayed = player.getKnightsPlayed();
            if (knightsPlayed >= MINIMUM_KNIGHTS) {
                if (knightsPlayed > maxKnights) {
                    maxKnights = knightsPlayed;
                    topPlayer = player;
                }
            }
        }

        // Tentukan hasil
        if (topPlayer == null) {
            // Tidak ada yang qualify
            this.revoke();
            this.currentQualifyingCount = 0;
        } else {
            // Ada pemain yang qualify
            if (holder == null || !holder.equals(topPlayer)) {
                // Kartu belum ada holder ATAU berpindah ke holder baru
                transfer(topPlayer);
            }
            this.currentQualifyingCount = maxKnights;
        }
    }

    /**
     * Get jumlah knights yang saat ini memqualify untuk kartu ini.
     */
    public int getCurrentQualifyingCount() {
        return currentQualifyingCount;
    }
}
