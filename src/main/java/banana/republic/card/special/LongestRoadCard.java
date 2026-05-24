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
 * Longest Road special card (Jalan Terpanjang).
 * Jumlah: 1 kartu
 * Bonus: 2 Poin Prestasi
 *
 * Syarat: Pemain harus memiliki jalan Pipa berurutan minimal 5 ruas tanpa percabangan.
 *
 * Aturan Seri:
 * - Seri Perebutan: Holder lama tetap memegang kartu sampai ada yang melampaui
 * - Seri Terputus: Jika jalan terputus dan ada seri, kartu disisihkan (holder = null)
 */
public class LongestRoadCard extends SpecialCard {
    private static final int MINIMUM_LENGTH = 5;
    private int currentQualifyingLength;

    /**
     * Constructor untuk Longest Road Card.
     */
    public LongestRoadCard() {
        super();
        this.currentQualifyingLength = 0;
    }

    @Override
    public SpecialCardType getCardType() {
        return SpecialCardType.LONGEST_ROAD;
    }

    /**
     * Evaluasi dan tentukan siapa yang berhak memegang kartu ini.
     * Harus dipanggil setiap kali ada pemain yang build road atau ketika jalan diputus.
     */
    public void evaluate(List<Player> players, Board board) {
        assert players != null : "Players list tidak boleh null";
        assert board != null : "Board tidak boleh null";

        // Calculate longest road untuk setiap pemain
        Map<Player, Integer> roadLengths = new HashMap<>();
        for (Player player : players) {
            int length = calculateRoadLength(player, board);
            roadLengths.put(player, length);
        }

        // Cari pemain(s) dengan road terpanjang yang >= MINIMUM_LENGTH
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

        // Tentukan hasil: transfer kartu, revoke, atau handle tie.
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
                // Tie karena perebutan: holder masih berada di posisi yang sama.
                this.active = true;
                this.currentQualifyingLength = holderLength;
                return;
            }
        }

        // Tie karena jaringan holder berubah/terputus: kartu disisihkan.
        this.revoke();
        this.currentQualifyingLength = 0;
    }

    /**
     * Hitung panjang jalan terpanjang untuk pemain tertentu menggunakan DFS.
     * Jangan hitung percabangan—hanya longest linear path.
     */
    public int calculateRoadLength(Player player, Board board) {
        assert player != null : "Player tidak boleh null";
        assert board != null : "Board tidak boleh null";

        // Get semua paths dari pemain
        List<Path> playerPaths = board.getConnectedRoads(player);

        if (playerPaths.isEmpty()) {
            return 0;
        }

        // Cari longest path dengan DFS dari setiap path sebagai starting point
        int maxLength = 0;
        for (Path startPath : playerPaths) {
            Set<Path> visited = new HashSet<>();
            int length = findLongestPath(startPath, visited, player, board);
            maxLength = Math.max(maxLength, length);
        }

        return maxLength;
    }

    /**
     * DFS helper untuk menemukan longest path dari current path.
     * visited set untuk avoid cycles.
     */
    private int findLongestPath(Path currentPath, Set<Path> visited, Player player, Board board) {
        visited.add(currentPath);

        // Get endpoints (intersection) dari path
        var endpoints = board.getAdjacentIntersections(currentPath);

        int maxExtension = 0;

        // Dari setiap endpoint, cari paths lain yang terhubung dan belum visited
        for (var endpoint : endpoints) {
            // Get semua paths di intersection tersebut
            var connectedPaths = board.getAdjacentPaths(endpoint);

            for (Path connected : connectedPaths) {
                // Cek apakah path sudah visited dan apakah owned oleh player
                if (!visited.contains(connected) && connected.hasRoad() && player.equals(connected.getOwner())) {
                    int extension = findLongestPath(connected, visited, player, board);
                    maxExtension = Math.max(maxExtension, extension);
                }
            }
        }

        visited.remove(currentPath); // Backtrack untuk explore paths lain

        return 1 + maxExtension; // 1 untuk current path + max extension
    }

    /**
     * Handle kondisi seri (untuk future use jika perlu explicit handling).
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
