package banana.republic.card.special;

import java.util.ArrayList;
import java.util.List;

import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;

/**
 * Kartu Pasukan Terbesar (Largest Army Card).
 *
 * <p>Bonus: +2 Poin Prestasi.
 *
 * <p>Syarat: Pemain harus sudah memainkan minimal 3 Kartu Penjaga
 * ({@link banana.republic.card.KnightCard}).
 *
 * <p>Mekanisme:
 * <ul>
 *   <li>Pemain pertama yang play 3 Knight mendapat kartu ini</li>
 *   <li>Jika pemain lain play lebih banyak Knight, kartu berpindah tangan</li>
 *   <li>Aturan seri: Holder lama tetap sampai ada yang melampaui</li>
 * </ul>
 */
public class LargestArmyCard extends SpecialCard {
    private static final int MINIMUM_KNIGHTS = 3;
    private int currentQualifyingCount;

    /**
     * Constructor default.
     */
    public LargestArmyCard() {
        super();
        this.currentQualifyingCount = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpecialCardType getCardType() {
        return SpecialCardType.LARGEST_ARMY;
    }

    /**
     * Mengevaluasi dan menentukan siapa yang berhak memegang kartu ini.
     *
     * <p>Harus dipanggil setiap kali ada pemain yang memainkan Knight card.
     *
     * @param players daftar pemain
     */
    public void evaluate(List<Player> players) {
        assert players != null : "Players list tidak boleh null";

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

        if (qualifyingPlayers.isEmpty()) {
            this.revoke();
            this.currentQualifyingCount = 0;
            return;
        }

        if (qualifyingPlayers.size() == 1) {
            Player winner = qualifyingPlayers.get(0);
            if (holder == null || !holder.equals(winner)) {
                transfer(winner);
            }
            this.currentQualifyingCount = maxKnights;
            return;
        }

        if (holder != null && qualifyingPlayers.contains(holder)) {
            int holderKnights = holder.getKnightsPlayed();
            if (holderKnights == maxKnights && holderKnights >= MINIMUM_KNIGHTS) {
                this.active = true;
                this.currentQualifyingCount = maxKnights;
                return;
            }
        }

        this.revoke();
        this.currentQualifyingCount = 0;
    }

    /**
     * Mengembalikan jumlah Knight yang saat ini memenuhi syarat untuk kartu ini.
     *
     * @return jumlah Knight yang memqualify
     */
    public int getCurrentQualifyingCount() {
        return currentQualifyingCount;
    }
}
