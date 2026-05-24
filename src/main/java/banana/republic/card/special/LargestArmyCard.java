package banana.republic.card.special;

import java.util.ArrayList;
import java.util.List;

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
        List<Player> qualifyingPlayers = new ArrayList<>();

        for (Player player : players) {
            int knightsPlayed = player.getKnightsPlayed();
            if (knightsPlayed >= MINIMUM_KNIGHTS) {
                if (knightsPlayed > maxKnights) {
                    maxKnights = knightsPlayed;
                    qualifyingPlayers.clear();
                    qualifyingPlayers.add(player);
                } else if (knightsPlayed == maxKnights) {
                    qualifyingPlayers.add(player);
                }
            }
        }

        // Tentukan hasil
        if (qualifyingPlayers.isEmpty()) {
            // Tidak ada yang qualify
            this.revoke();
            this.currentQualifyingCount = 0;
        } else if (qualifyingPlayers.size() == 1) {
            Player winner = qualifyingPlayers.get(0);
            if (holder == null || !holder.equals(winner)) {
                transfer(winner);
            }
            this.currentQualifyingCount = maxKnights;
        } else {
            // Tie: holder lama tetap jika masih termasuk pemain teratas
            if (holder != null && qualifyingPlayers.contains(holder)) {
                this.active = true;
                this.currentQualifyingCount = maxKnights;
            } else {
                this.revoke();
                this.currentQualifyingCount = 0;
            }
        }
    }

    /**
     * Get jumlah knights yang saat ini memqualify untuk kartu ini.
     */
    public int getCurrentQualifyingCount() {
        return currentQualifyingCount;
    }
}
