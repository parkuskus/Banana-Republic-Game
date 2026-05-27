package banana.republic.core;

import banana.republic.player.Player;

/**
 * Data Transfer Object rincian Poin Prestasi (PP) satu pemain.
 *
 * Berisi breakdown per kategori sumber PP:
 *   - Pos Pantau di board = 1 PP per bangunan
 *   - Laboratorium di board = 2 PP per bangunan
 *   - VictoryPointCard di tangan = 1 PP per kartu (tersembunyi)
 *   - Pasukan Terbesar / Jalan Terpanjang = 2 PP jika dimiliki
 *
 * Immutable, dibuat oleh VictoryPointCalculator dan dikonsumsi UI.
 */
public final class VictoryPointBreakdown {

    private final Player player;
    private final int settlementPoints;  // PosPantau yang di-placed di board
    private final int cityPoints;        // Laboratorium yang di-placed di board
    private final int victoryCardPoints; // VictoryPointCard di tangan
    private final int largestArmyPoints; // 2 jika punya kartu Pasukan Terbesar
    private final int longestRoadPoints; // 2 jika punya kartu Jalan Terpanjang
    private final int total;

    public VictoryPointBreakdown(Player player, int settlementPoints,
                                 int cityPoints, int victoryCardPoints,
                                 int largestArmyPoints, int longestRoadPoints) {
        if (player == null)
            throw new IllegalArgumentException("Player tidak boleh null");
        this.player = player;
        this.settlementPoints = settlementPoints;
        this.cityPoints = cityPoints;
        this.victoryCardPoints = victoryCardPoints;
        this.largestArmyPoints = largestArmyPoints;
        this.longestRoadPoints = longestRoadPoints;
        this.total = settlementPoints + cityPoints + victoryCardPoints +
                     largestArmyPoints + longestRoadPoints;
    }

    public Player getPlayer() { return player; }

    public int getSettlementPoints() { return settlementPoints; }

    public int getCityPoints() { return cityPoints; }

    public int getVictoryCardPoints() { return victoryCardPoints; }

    public int getLargestArmyPoints() { return largestArmyPoints; }

    public int getLongestRoadPoints() { return longestRoadPoints; }

    public int getTotal() { return total; }

    /**
     * Total PP yang terlihat publik (tidak termasuk VictoryPointCard).
     *
     * Dipakai UI untuk menampilkan skor ke pemain lain.
     */
    public int getPublicTotal() {
        return settlementPoints + cityPoints + largestArmyPoints +
            longestRoadPoints;
    }

    @Override
    public String toString() {
        return String.format("VPBreakdown[%s | settlement=%d, city=%d, "
                                 + "vpCard=%d, army=%d, road=%d | total=%d]",
                             player.getName(), settlementPoints, cityPoints,
                             victoryCardPoints, largestArmyPoints,
                             longestRoadPoints, total);
    }
}
