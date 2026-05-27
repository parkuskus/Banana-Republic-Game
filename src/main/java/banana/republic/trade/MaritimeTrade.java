package banana.republic.trade;

import banana.republic.board.Board;
import banana.republic.board.Harbor;
import banana.republic.board.Intersection;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import java.util.Optional;

/**
 * Menghitung rasio trade maritim (dengan bank) berdasarkan harbor yang dimiliki
 * pemain.
 *
 * Aturan trade dengan bank: - Default (tanpa harbor): 4:1 untuk resource
 * apapun</li> - Generic harbor (3:1): 3:1 untuk resource apapun</li> - Specific
 * harbor (2:1): 2:1 khusus untuk resource tertentu</li>
 *
 * Pemain memiliki akses ke harbor jika memiliki PosPantau atau Laboratorium di
 * salah satu intersection yang bersebelahan dengan harbor tersebut.
 */
public class MaritimeTrade {

    public static final int DEFAULT_RATIO = 4;

    /**
     * Mengembalikan rasio trade terbaik yang dimiliki pemain untuk resource
     * tertentu.
     *
     * Contoh: jika pemain punya harbor GENERIC_3TO1 dan BANANA_2TO1, rasio
     * untuk BANANA = 2, untuk WOOD = 3.
     *
     * player pemain yang ingin trade resource resource yang akan dijual ke bank
     * board papan permainan return rasio terbaik (2, 3, atau 4)
     */
    public int getBestRatio(Player player, ResourceType resource, Board board) {
        int bestRatio = DEFAULT_RATIO;

        for (Intersection intersection : board.getAllIntersections()) {
            // Cek apakah intersection ini milik player
            if (!player.equals(intersection.getOwner()))
                continue;

            // Cek apakah ada harbor di intersection ini
            Optional<Harbor> harbor = board.getHarborAt(intersection);
            if (harbor.isEmpty())
                continue;

            int ratio = harbor.get().getTradeRatio(resource);
            if (ratio < bestRatio) {
                bestRatio = ratio;
            }
        }

        return bestRatio;
    }

    /**
     * Mengembalikan rasio trade semua resource untuk pemain tertentu. Berguna
     * untuk UI yang ingin menampilkan semua rasio sekaligus.
     *
     * @return array panjang {@link ResourceType#values()}.length berisi rasio
     *     per resource
     */
    public int[] getAllRatios(Player player, Board board) {
        ResourceType[] types = ResourceType.values();
        int[] ratios = new int[types.length];
        for (int i = 0; i < types.length; i++) {
            ratios[i] = getBestRatio(player, types[i], board);
        }
        return ratios;
    }

    /**
     * Mengembalikan apakah pemain bisa trade resource tertentu ke bank dengan
     * resource yang dimilikinya.
     *
     * player pemain sellType resource yang dijual buyType resource yang dibeli
     * board papan permainan return ValidationResult
     */
    public ValidationResult canTradeWithBank(Player player,
                                             ResourceType sellType,
                                             ResourceType buyType,
                                             Board board) {
        if (player == null)
            return ValidationResult.fail("Player tidak boleh null");
        if (sellType == null)
            return ValidationResult.fail(
                "Resource yang dijual tidak boleh null");
        if (buyType == null)
            return ValidationResult.fail(
                "Resource yang dibeli tidak boleh null");
        if (sellType == buyType)
            return ValidationResult.fail("Tidak bisa trade resource yang sama");
        if (board == null)
            return ValidationResult.fail("Board tidak boleh null");

        int ratio = getBestRatio(player, sellType, board);
        if (!player.hasResource(sellType, ratio)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya cukup " +
                sellType.getDisplayName() + " (butuh " + ratio + ", punya " +
                player.getResourceCount(sellType) + ")");
        }
        return ValidationResult.ok();
    }
}
