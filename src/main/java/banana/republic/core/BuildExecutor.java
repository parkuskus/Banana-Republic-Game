package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.building.Building;
import banana.republic.building.Laboratorium;
import banana.republic.building.PlayerSupply;
import banana.republic.building.PosPantau;
import banana.republic.building.Road;
import banana.republic.card.CardDeck;
import banana.republic.card.ExperimentCard;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceProductionService;
import banana.republic.resource.ResourceType;

import java.util.Map;

/**
 * Mengeksekusi mutasi state untuk semua aksi build.
 *
 * <p>Kelas ini hanya bertanggung jawab untuk mengubah state:
 * mengurangi resource pemain, mengembalikan resource ke bank,
 * mengambil bangunan dari supply, dan menempatkan bangunan di board.
 *
 * <p><strong>Pre-condition:</strong> Semua validasi sudah dilakukan
 * oleh {@link BuildValidator} sebelum method ini dipanggil.
 * Tidak ada validasi bisnis di sini — jika pre-condition dilanggar,
 * perilaku tidak didefinisikan.
 */
public class BuildExecutor {

    // -------------------------------------------------------------------------
    // Aksi normal (fase TRADE_BUILD)
    // -------------------------------------------------------------------------

    /**
     * Eksekusi build road: deduct resource, take road dari supply, tempatkan di path.
     *
     * <p>Resource yang dikurangi: 1 WOOD + 1 BRICK (dikembalikan ke bank).
     */
    public void executeRoad(Player player, Path path, Bank bank,
                            Map<Player, PlayerSupply> supplies) {
        player.removeResource(ResourceType.WOOD,  1);
        player.removeResource(ResourceType.BRICK, 1);
        bank.returnResource(ResourceType.WOOD,  1);
        bank.returnResource(ResourceType.BRICK, 1);

        Road road = supplies.get(player).takeRoad();
        path.placeRoad(road);
    }

    /**
     * Eksekusi build settlement: deduct resource, take Pos Pantau, tempatkan di intersection.
     *
     * <p>Resource yang dikurangi: 1 WOOD + 1 BRICK + 1 WHEAT + 1 BANANA (dikembalikan ke bank).
     */
    public void executeSettlement(Player player, Intersection intersection, Bank bank,
                                   Map<Player, PlayerSupply> supplies) {
        player.removeResource(ResourceType.WOOD,   1);
        player.removeResource(ResourceType.BRICK,  1);
        player.removeResource(ResourceType.WHEAT,  1);
        player.removeResource(ResourceType.BANANA, 1);
        bank.returnResource(ResourceType.WOOD,   1);
        bank.returnResource(ResourceType.BRICK,  1);
        bank.returnResource(ResourceType.WHEAT,  1);
        bank.returnResource(ResourceType.BANANA, 1);

        PosPantau pp = supplies.get(player).takePosPantau();
        intersection.placeBuilding(pp);
    }

    /**
     * Eksekusi upgrade Pos Pantau → Laboratorium.
     *
     * <p>Resource yang dikurangi: 2 WHEAT + 3 ORE (dikembalikan ke bank).
     * Pos Pantau lama dikembalikan ke supply pemain.
     */
    public void executeCity(Player player, Intersection intersection, Bank bank,
                             Map<Player, PlayerSupply> supplies) {
        player.removeResource(ResourceType.WHEAT, 2);
        player.removeResource(ResourceType.ORE,   3);
        bank.returnResource(ResourceType.WHEAT, 2);
        bank.returnResource(ResourceType.ORE,   3);

        Building removed = intersection.removeBuilding();
        PlayerSupply supply = supplies.get(player);
        if (removed instanceof PosPantau) {
            supply.returnPosPantau((PosPantau) removed);
        }
        Laboratorium lab = supply.takeLaboratorium();
        intersection.placeBuilding(lab);
    }

    /**
     * Eksekusi pembelian Kartu Temuan: deduct resource, draw kartu, tambah ke tangan pemain.
     *
     * <p>Resource yang dikurangi: 1 ORE + 1 WHEAT + 1 BANANA (dikembalikan ke bank).
     *
     * @return kartu yang baru dibeli (untuk dicatat sebagai {@code cardBoughtThisTurn})
     */
    public ExperimentCard executeBuyCard(Player player, CardDeck deck, Bank bank) {
        player.removeResource(ResourceType.ORE,    1);
        player.removeResource(ResourceType.WHEAT,  1);
        player.removeResource(ResourceType.BANANA, 1);
        bank.returnResource(ResourceType.ORE,    1);
        bank.returnResource(ResourceType.WHEAT,  1);
        bank.returnResource(ResourceType.BANANA, 1);

        ExperimentCard card = deck.draw();
        player.addCard(card);
        return card;
    }

    // -------------------------------------------------------------------------
    // Aksi setup (placeInitialSettlement / placeInitialRoad)
    // -------------------------------------------------------------------------

    /**
     * Eksekusi penempatan Pos Pantau awal di fase setup.
     *
     * <p>Tidak ada biaya resource. Jika ini fase setup kedua dan ada service produksi,
     * distribusikan resource awal ke pemain.
     *
     * @param productionService boleh null jika distribusi awal tidak diperlukan
     *                          (fase setup pertama)
     */
    public void executeInitialSettlement(Player player, Intersection intersection,
                                          Map<Player, PlayerSupply> supplies,
                                          GamePhase phase,
                                          ResourceProductionService productionService,
                                          Bank bank, Board board) {
        PosPantau pp = supplies.get(player).takePosPantau();
        intersection.placeBuilding(pp);

        if (phase == GamePhase.SETUP_SECOND_ROUND && productionService != null) {
            productionService.distributeInitialResources(player, intersection, bank, board);
        }
    }

    /**
     * Eksekusi penempatan Pipa awal di fase setup.
     *
     * <p>Tidak ada biaya resource.
     */
    public void executeInitialRoad(Player player, Path path,
                                    Map<Player, PlayerSupply> supplies) {
        Road road = supplies.get(player).takeRoad();
        path.placeRoad(road);
    }
}
