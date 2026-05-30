package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.Intersection;
import banana.republic.building.BuildingType;
import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Menghitung Poin Prestasi (PP) setiap pemain dan menentukan pemenang.
 *
 * Sumber PP:
 *   - Pos Pantau di board = 1 PP per bangunan
 *   - Laboratorium di board = 2 PP per bangunan
 *   - VictoryPointCard di tangan = 1 PP per kartu (tersembunyi)
 *   - Pasukan Terbesar (Largest Army) = +2 PP (min. 3 knight dimainkan)
 *   - Jalan Terpanjang (Longest Road) = +2 PP (min. 5 road beruntun)
 *
 * Semua method bersifat stateless.
 */
public class VictoryPointCalculator {

    private static final int SPECIAL_CARD_POINTS = 2;

    /**
     * Menghitung VP lengkap satu pemain beserta rinciannya.
     *
     * Parameter:
     *   player - pemain yang dihitung
     *   board - papan permainan (untuk iterasi bangunan yang sudah ditempatkan)
     *
     * Return: VictoryPointBreakdown yang berisi total dan rincian
     */
    public VictoryPointBreakdown calculate(Player player, Board board) {
        if (player == null)
            throw new IllegalArgumentException("Player tidak boleh null");
        if (board == null)
            throw new IllegalArgumentException("Board tidak boleh null");

        int settlementPts = countSettlementPoints(player, board);
        int cityPts = countCityPoints(player, board);
        int vpCardPts = countVictoryCardPoints(player);
        int largestArmyPts = player.hasSpecialCard(SpecialCardType.LARGEST_ARMY)
                                 ? SPECIAL_CARD_POINTS
                                 : 0;
        int longestRoadPts = player.hasSpecialCard(SpecialCardType.LONGEST_ROAD)
                                 ? SPECIAL_CARD_POINTS
                                 : 0;

        return new VictoryPointBreakdown(player, settlementPts, cityPts,
                                         vpCardPts, largestArmyPts,
                                         longestRoadPts);
    }

    /**
     * Menghitung VP total (hanya angka) untuk pemain tertentu.
     *
     * Shortcut dari calculate(Player, Board).
     */
    public int getTotalVP(Player player, Board board) {
        return calculate(player, board).getTotal();
    }

    // -------------------------------------------------------------------------
    // Hitung VP semua pemain
    // -------------------------------------------------------------------------

    /**
     * Menghitung VP semua pemain dan mengembalikan daftar breakdown.
     *
     * Hasil diurutkan dari VP tertinggi ke terendah.
     *
     * Parameter:
     *   players - daftar pemain
     *   board - papan permainan
     *
     * Return: list VictoryPointBreakdown terurut descending
     */
    public List<VictoryPointBreakdown> calculateAll(List<Player> players,
                                                    Board board) {
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        }
        List<VictoryPointBreakdown> results = new ArrayList<>();
        for (Player p : players) {
            results.add(calculate(p, board));
        }
        results.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));
        return Collections.unmodifiableList(results);
    }

    // -------------------------------------------------------------------------
    // Cek pemenang
    // -------------------------------------------------------------------------

    /**
     * Mencari pemain yang sudah mencapai atau melampaui victoryTarget PP.
     *
     * Jika lebih dari satu pemain mencapai target (edge case seharusnya tidak
     * terjadi), mengembalikan yang VP-nya tertinggi.
     *
     * Parameter:
     *   players - daftar pemain
     *   board - papan permainan
     *   victoryTarget - target PP untuk menang (biasanya 10)
     *
     * Return: pemain pemenang, atau null jika belum ada yang menang
     */
    public Player findWinner(List<Player> players, Board board,
                             int victoryTarget) {
        Player winner = null;
        int highestVP = -1;

        for (Player p : players) {
            int vp = getTotalVP(p, board);
            if (vp >= victoryTarget && vp > highestVP) {
                highestVP = vp;
                winner = p;
            }
        }

        return winner;
    }

    // -------------------------------------------------------------------------
    // Longest Road management
    // -------------------------------------------------------------------------

    /**
     * Memeriksa dan memperbarui kepemilikan Jalan Terpanjang (Longest Road).
     *
     * Dipanggil setelah setiap buildRoad().
     *
     * Syarat:
     *   - Minimal 5 road beruntun
     *   - Lebih panjang dari pemegang saat ini
     *
     * Parameter:
     *   players - daftar pemain
     *
     * Return: pemain yang sekarang memegang Longest Road,
     *         atau null jika belum ada yang memenuhi syarat
     */
    public Player updateLongestRoad(List<Player> players) {
        final int MIN_ROAD_LENGTH = 5;

        Player currentHolder = null;
        int holderLength = 0;

        // Temukan pemegang saat ini
        for (Player p : players) {
            if (p.hasSpecialCard(SpecialCardType.LONGEST_ROAD)) {
                currentHolder = p;
                holderLength = p.getLongestRoadLength();
                break;
            }
        }

        // Temukan pemain dengan road terpanjang
        Player newHolder = null;
        int newLength = 0;

        for (Player p : players) {
            int length = p.getLongestRoadLength();
            if (length >= MIN_ROAD_LENGTH && length > newLength) {
                newLength = length;
                newHolder = p;
            }
        }

        // Tidak ada perubahan jika pemegang sama atau tidak ada yang qualify
        if (newHolder == null)
            return currentHolder;
        if (newHolder.equals(currentHolder))
            return currentHolder;

        // Perlu panjang lebih besar dari pemegang, bukan hanya sama
        if (currentHolder != null && newLength <= holderLength) {
            return currentHolder;
        }

        // Transfer kepemilikan
        if (currentHolder != null) {
            currentHolder.setSpecialCard(SpecialCardType.LONGEST_ROAD, false);
        }
        newHolder.setSpecialCard(SpecialCardType.LONGEST_ROAD, true);
        return newHolder;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Hitung PP dari Pos Pantau yang sudah ditempatkan pemain di board.
     *
     * 1 PP per bangunan. Jumlah = MAX_POS_PANTAU(5) - sisa di supply.
     */
    private int countSettlementPoints(Player player, Board board) {
        int count = 0;
        for (Intersection intersection : board.getAllIntersections()) {
            if (intersection.hasBuilding() &&
                player.equals(intersection.getOwner()) &&
                intersection.getBuilding().getBuildingType() ==
                    BuildingType.POS_PANTAU) {
                count++;
            }
        }
        return count * 1;
    }

    /**
     * Hitung PP dari Laboratorium yang sudah ditempatkan pemain di board.
     *
     * 2 PP per bangunan.
     */
    private int countCityPoints(Player player, Board board) {
        int count = 0;
        for (Intersection intersection : board.getAllIntersections()) {
            if (intersection.hasBuilding() &&
                player.equals(intersection.getOwner()) &&
                intersection.getBuilding().getBuildingType() ==
                    BuildingType.LABORATORIUM) {
                count++;
            }
        }
        return count * 2;
    }

    /**
     * Hitung PP dari VictoryPointCard di tangan.
     *
     * 1 PP per kartu (tersembunyi).
     */
    private int countVictoryCardPoints(Player player) {
        int count = 0;
        for (ExperimentCard card : player.getHandCards()) {
            if (card.getCardType() == CardType.VICTORY_POINT) {
                count++;
            }
        }
        return count;
    }
}
