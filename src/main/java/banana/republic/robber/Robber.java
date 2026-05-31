package banana.republic.robber;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import java.util.*;

/**
 * Robber / Nimon Ungu mechanics.
 *
 * Nimon Ungu merepresentasikan "pembohong ungu jahat" yang bisa dipindahkan
 * saat dadu 7.
 *
 * Efek Nimon Ungu (saat dadu 7):
 * - Discard Phase: Pemain dengan >7 kartu harus buang setengah kartu
 * - Move: Nimon Ungu WAJIB pindah ke petak terrain lain
 * - Steal (opsional): Pemain aktif boleh curi 1 kartu acak dari pemain
 *   yang punya bangunan di sekitar lokasi baru
 *
 * Note:
 * Kartu Temuan Dr. Neroifa TIDAK dihitung dan TIDAK BISA dibuang saat
 * discard phase.
 */
public class Robber {
    private HexTile currentTile;

    /**
     * Constructor untuk Robber.
     *
     * Nimon Ungu dimulai di petak Gurun (terrain dengan type DESERT/GURUN).
     */
    public Robber(HexTile desertTile) {
        assert desertTile != null
            : "Desert tile tidak boleh null saat init Robber";
        this.currentTile = desertTile;
        this.currentTile.setRobber(true);
    }

    /**
     * Pindahkan Nimon Ungu ke petak terrain lain.
     *
     * WAJIB pindah (tidak boleh tetap di tempat yang sama).
     * Update status robber di HexTiles.
     */
    public void move(HexTile newTile) {
        assert newTile != null
            : "New tile tidak boleh null saat pindahkan Robber";
        assert !newTile.equals(currentTile)
            : "Robber harus pindah ke tile yang berbeda";

        // Remove dari tile lama
        this.currentTile.setRobber(false);

        // Move ke tile baru
        this.currentTile = newTile;
        this.currentTile.setRobber(true);
    }

    /**
     * Dapatkan tile saat ini tempat Nimon Ungu berada.
     */
    public HexTile getCurrentTile() { return currentTile; }

    /**
     * Dapatkan daftar pemain yang eligible untuk dicuri.
     *
     * Criteria:
     * - Memiliki Pos Pantau atau Laboratorium di persimpangan yang
     *   bersebelahan dengan tile sekarang
     * - Memiliki minimal 1 kartu sumber daya di tangan
     *   (bukan kartu development)
     * - Bukan pemain aktif (thief) sendiri
     */
    public List<Player> getEligibleVictims(Player thief, Board board) {
        assert thief != null : "Thief tidak boleh null";
        assert board != null : "Board tidak boleh null";

        List<Player> victims = new ArrayList<>();

        // Get intersections di sekitar currentTile
        List<Intersection> adjacentIntersections =
            board.getAdjacentIntersections(currentTile);

        for (Intersection intersection : adjacentIntersections) {
            // Cek apakah ada building (Settlement atau City) di intersection
            if (intersection.hasBuilding()) {
                Player owner = intersection.getBuilding().getOwner();

                // Pastikan bukan thief dan belum di list
                if (owner != null && !owner.equals(thief) &&
                    !victims.contains(owner)) {
                    // Cek apakah owner punya kartu sumber daya
                    if (owner.getTotalResourceCount() > 0) {
                        victims.add(owner);
                    }
                }
            }
        }

        return victims;
    }

    /**
     * Curi 1 kartu sumber daya RANDOM dari victim.
     *
     * Memilih random index dari total resource count.
     * Return tipe resource yang dicuri (untuk logging/UI).
     */
    public ResourceType stealRandomResource(Player thief, Player victim) {
        assert thief != null : "Thief tidak boleh null";
        assert victim != null : "Victim tidak boleh null";
        assert victim.getTotalResourceCount() > 0
            : "Victim harus punya kartu untuk dicuri";

        // Get total resource count
        int totalResources = victim.getTotalResourceCount();
        Random random = new Random();
        int randomIndex = random.nextInt(totalResources);

        // Iterasi resource types dan random 1 dari mereka
        int currentIndex = 0;
        for (ResourceType type : ResourceType.values()) {
            int count = victim.getResourceCount(type);
            if (randomIndex < currentIndex + count) {
                // Ini adalah resource yang dicuri
                victim.removeResource(type, 1);
                thief.addResource(type, 1);
                return type;
            }
            currentIndex += count;
        }

        // Seharusnya unreachable karena victim punya resource
        throw new IllegalStateException(
            "Failed to steal resource: random index out of bounds despite valid total count"
        );
    }

    /**
     * Aktivasi Discard Phase.
     *
     * Setiap pemain dengan >7 kartu sumber daya harus buang setengah.
     * Return map: Player -> jumlah kartu yang harus dibuang.
     *
     * Note:
     * Kartu Temuan (development cards) TIDAK DIHITUNG dalam jumlah kartu.
     */
    public Map<Player, Integer> activateDiscardPhase(List<Player> players) {
        assert players != null : "Players list tidak boleh null";

        Map<Player, Integer> discardThreshold = new HashMap<>();

        for (Player player : players) {
            // Hitung hanya resource cards (excludes development cards)
            int resourceCount = player.getTotalResourceCount();

            if (resourceCount > 7) {
                // Harus buang sebanyak setengah (floor)
                int toDiscard = resourceCount / 2;
                discardThreshold.put(player, toDiscard);
            }
        }

        return discardThreshold;
    }
}
