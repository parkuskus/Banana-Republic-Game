package banana.republic.card.special;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import banana.republic.board.Board;
import banana.republic.board.Path;
import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;

/**
 * Kartu Jalan Terpanjang (Longest Road Card).
 *
 * <p>Bonus: +2 Poin Prestasi.
 *
 * <p>Syarat: Pemain harus memiliki jalan Pipa berurutan minimal 5 ruas
 * tanpa percabangan.
 *
 * <p>Aturan seri:
 * <ul>
 *   <li>Seri perebutan: Holder lama tetap memegang sampai ada yang melampaui</li>
 *   <li>Seri terputus: Jika jalan terputus dan ada seri, kartu disisihkan</li>
 * </ul>
 */
public class LongestRoadCard extends SpecialCard {
    private static final int MINIMUM_LENGTH = 5;
    private int currentQualifyingLength;

    /**
     * Constructor default.
     */
    public LongestRoadCard() {
        super();
        this.currentQualifyingLength = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpecialCardType getCardType() {
        return SpecialCardType.LONGEST_ROAD;
    }

    /**
     * Mengevaluasi dan menentukan siapa yang berhak memegang kartu ini.
     *
     * <p>Harus dipanggil setiap kali ada pemain yang build road atau
     * ketika jalan diputus.
     *
     * @param players daftar pemain
     * @param board   papan permainan
     */
    public void evaluate(List<Player> players, Board board) {
        assert players != null : "Players list tidak boleh null";
        assert board != null : "Board tidak boleh null";

        Map<Player, Integer> roadLengths = new HashMap<>();
        for (Player player : players) {
            int length = calculateRoadLength(player, board);
            roadLengths.put(player, length);
        }

        int maxLength = 0;
        List<Player> qualifyingPlayers = new ArrayList<>();

        for (Map.Entry<Player, Integer> entry : roadLengths.entrySet()) {
            int length = entry.getValue();
            if (length >= MINIMUM_LENGTH) {
                if (length > maxLength) {
                    maxLength = length;
                    qualifyingPlayers.clear();
                    qualifyingPlayers.add(entry.getKey());
                } else if (length == maxLength) {
                    qualifyingPlayers.add(entry.getKey());
                }
            }
        }

        if (qualifyingPlayers.isEmpty()) {
            this.revoke();
            this.currentQualifyingLength = 0;
            return;
        }

        if (qualifyingPlayers.size() == 1) {
            Player winner = qualifyingPlayers.get(0);
            if (holder == null || !holder.equals(winner)) {
                transfer(winner);
            }
            this.currentQualifyingLength = maxLength;
            return;
        }

        if (holder != null && qualifyingPlayers.contains(holder)) {
            Integer holderLength = roadLengths.get(holder);
            if (holderLength != null && holderLength == currentQualifyingLength && holderLength >= MINIMUM_LENGTH) {
                this.active = true;
                this.currentQualifyingLength = holderLength;
                return;
            }
        }

        this.revoke();
        this.currentQualifyingLength = 0;
    }

    /**
     * Menghitung panjang jalan terpanjang pemain menggunakan DFS.
     *
     * <p>Tidak menghitung percabangan — hanya longest linear path.
     *
     * @param player pemain yang dihitung
     * @param board  papan permainan
     * @return panjang jalan terpanjang dalam ruas pipa
     */
    public int calculateRoadLength(Player player, Board board) {
        assert player != null : "Player tidak boleh null";
        assert board != null : "Board tidak boleh null";

        List<Path> playerPaths = board.getConnectedRoads(player);

        if (playerPaths.isEmpty()) {
            return 0;
        }

        int maxLength = 0;
        for (Path startPath : playerPaths) {
            Set<Path> visited = new HashSet<>();
            int length = findLongestPath(startPath, visited, player, board);
            maxLength = Math.max(maxLength, length);
        }

        return maxLength;
    }

    /**
     * Helper DFS untuk menemukan longest path dari {@code currentPath}.
     *
     * @param currentPath path saat ini
     * @param visited     set path yang sudah dikunjungi
     * @param player      pemain yang dicari jalannya
     * @param board       papan permainan
     * @return panjang path dari titik ini
     */
    private int findLongestPath(Path currentPath, Set<Path> visited, Player player, Board board) {
        visited.add(currentPath);

        var endpoints = board.getAdjacentIntersections(currentPath);

        int maxExtension = 0;

        for (var endpoint : endpoints) {
            var connectedPaths = board.getAdjacentPaths(endpoint);

            for (Path connected : connectedPaths) {
                if (!visited.contains(connected) && connected.hasRoad() && player.equals(connected.getOwner())) {
                    int extension = findLongestPath(connected, visited, player, board);
                    maxExtension = Math.max(maxExtension, extension);
                }
            }
        }

        visited.remove(currentPath);

        return 1 + maxExtension;
    }

    /**
     * Menangani kondisi seri secara eksplisit.
     *
     * @param tied daftar pemain yang seri; {@code null} atau kosong
     *             menyebabkan kartu dicabut
     */
    public void handleTie(List<Player> tied) {
        if (tied == null || tied.isEmpty()) {
            this.revoke();
            return;
        }

        if (holder != null && tied.contains(holder)) {
            this.active = true;
            return;
        }

        this.revoke();
    }
}
