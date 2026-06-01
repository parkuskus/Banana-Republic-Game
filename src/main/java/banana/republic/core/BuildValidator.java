package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.building.BuildingType;
import banana.republic.building.PlayerSupply;
import banana.republic.card.CardDeck;
import banana.republic.card.ExperimentCard;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import banana.republic.trade.ValidationResult;

import java.util.Map;

/**
 * Memvalidasi semua pre-condition sebelum aksi build/kartu dieksekusi.
 *
 * <p>Semua method bersifat stateless dan mengembalikan {@link ValidationResult}
 * — tidak pernah melempar exception untuk kondisi bisnis tidak valid.
 * Exception hanya dilempar jika argumen yang wajib non-null ternyata null.
 *
 * <p>Tanggung jawab kelas ini murni validasi — tidak ada mutasi state sama sekali.
 * Eksekusi diserahkan ke {@link BuildExecutor}.
 */
public class BuildValidator {

    // -------------------------------------------------------------------------
    // Fase pembangunan normal (TRADE_BUILD)
    // -------------------------------------------------------------------------

    /**
     * Memvalidasi semua pre-condition sebelum {@code buildRoad()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus {@code TRADE_BUILD}</li>
     *   <li>{@code player} dan {@code path} tidak boleh null</li>
     *   <li>Path belum ada road</li>
     *   <li>Path terhubung ke jaringan road/settlement pemain</li>
     *   <li>Pemain punya 1 WOOD + 1 BRICK</li>
     *   <li>Stok road tersedia di supply pemain</li>
     * </ul>
     */
    public ValidationResult canBuildRoad(Player player, Path path,
                                          Board board,
                                          Map<Player, PlayerSupply> supplies,
                                          GamePhase phase) {
        if (player == null || path == null || board == null || supplies == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (phase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                "buildRoad() hanya boleh dipanggil saat fase TRADE_BUILD (saat ini: " + phase + ")");
        }
        if (path.hasRoad()) {
            return ValidationResult.fail("Path #" + path.getId() + " sudah ada road");
        }
        if (!board.isPathConnectedToPlayer(path, player)) {
            return ValidationResult.fail(
                "Road harus terhubung ke jaringan " + player.getName());
        }
        if (!player.hasResource(ResourceType.WOOD, 1) ||
                !player.hasResource(ResourceType.BRICK, 1)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya resource cukup untuk Pipa"
                + " (butuh 1 WOOD + 1 BRICK)");
        }
        PlayerSupply supply = supplies.get(player);
        if (supply == null || !supply.canBuildRoad()) {
            return ValidationResult.fail(
                player.getName() + " sudah kehabisan stok Pipa Transportasi");
        }
        return ValidationResult.ok();
    }

    /**
     * Memvalidasi semua pre-condition sebelum {@code buildSettlement()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus {@code TRADE_BUILD}</li>
     *   <li>Intersection kosong</li>
     *   <li>Distance rule (tidak boleh bersebelahan dengan bangunan lain)</li>
     *   <li>Terhubung ke minimal 1 road milik pemain</li>
     *   <li>Pemain punya 1 WOOD + 1 BRICK + 1 WHEAT + 1 BANANA</li>
     *   <li>Stok Pos Pantau tersedia</li>
     * </ul>
     */
    public ValidationResult canBuildSettlement(Player player,
                                               Intersection intersection,
                                               Board board,
                                               Map<Player, PlayerSupply> supplies,
                                               GamePhase phase) {
        if (player == null || intersection == null || board == null || supplies == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (phase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                "buildSettlement() hanya boleh dipanggil saat fase TRADE_BUILD (saat ini: " + phase + ")");
        }
        if (intersection.hasBuilding()) {
            return ValidationResult.fail(
                "Intersection #" + intersection.getId() + " sudah ada bangunan");
        }
        if (!board.isDistanceRuleValid(intersection)) {
            return ValidationResult.fail(
                "Distance rule dilanggar — harus berjarak minimal 2 edge dari bangunan lain");
        }
        boolean connected = intersection.getAdjacentPaths().stream()
            .anyMatch(p -> p.hasRoad() && player.equals(p.getOwner()));
        if (!connected) {
            return ValidationResult.fail(
                "Pos Pantau harus terhubung ke Pipa milik " + player.getName());
        }
        if (!player.hasResource(ResourceType.WOOD,   1) ||
                !player.hasResource(ResourceType.BRICK,  1) ||
                !player.hasResource(ResourceType.WHEAT,  1) ||
                !player.hasResource(ResourceType.BANANA, 1)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya resource cukup untuk Pos Pantau"
                + " (butuh 1 WOOD + 1 BRICK + 1 WHEAT + 1 BANANA)");
        }
        PlayerSupply supply = supplies.get(player);
        if (supply == null || !supply.canBuildPosPantau()) {
            return ValidationResult.fail(
                player.getName() + " sudah kehabisan stok Pos Pantau");
        }
        return ValidationResult.ok();
    }

    /**
     * Memvalidasi semua pre-condition sebelum {@code buildCity()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus {@code TRADE_BUILD}</li>
     *   <li>Intersection punya bangunan milik pemain bertipe {@code POS_PANTAU}</li>
     *   <li>Pemain punya 2 WHEAT + 3 ORE</li>
     *   <li>Stok Laboratorium tersedia</li>
     * </ul>
     */
    public ValidationResult canBuildCity(Player player,
                                         Intersection intersection,
                                         Map<Player, PlayerSupply> supplies,
                                         GamePhase phase) {
        if (player == null || intersection == null || supplies == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (phase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                "buildCity() hanya boleh dipanggil saat fase TRADE_BUILD (saat ini: " + phase + ")");
        }
        if (!intersection.hasBuilding()) {
            return ValidationResult.fail(
                "Intersection #" + intersection.getId() + " tidak ada bangunan untuk di-upgrade");
        }
        if (!player.equals(intersection.getOwner())) {
            return ValidationResult.fail(
                "Hanya pemilik Pos Pantau yang bisa upgrade ke Laboratorium");
        }
        if (intersection.getBuilding().getBuildingType() != BuildingType.POS_PANTAU) {
            return ValidationResult.fail(
                "Hanya Pos Pantau yang bisa di-upgrade (bangunan saat ini: "
                + intersection.getBuilding().getBuildingType() + ")");
        }
        if (!player.hasResource(ResourceType.WHEAT, 2) ||
                !player.hasResource(ResourceType.ORE, 3)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya resource cukup untuk Laboratorium"
                + " (butuh 2 WHEAT + 3 ORE)");
        }
        PlayerSupply supply = supplies.get(player);
        if (supply == null || !supply.canBuildLaboratorium()) {
            return ValidationResult.fail(
                player.getName() + " sudah kehabisan stok Laboratorium");
        }
        return ValidationResult.ok();
    }

    /**
     * Memvalidasi pre-condition sebelum {@code buyDevelopmentCard()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus {@code TRADE_BUILD}</li>
     *   <li>Deck tidak kosong</li>
     *   <li>Pemain punya 1 ORE + 1 WHEAT + 1 BANANA</li>
     * </ul>
     */
    public ValidationResult canBuyCard(Player player, CardDeck deck, GamePhase phase) {
        if (player == null || deck == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (phase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                "Kartu hanya bisa dibeli saat fase TRADE_BUILD (saat ini: " + phase + ")");
        }
        if (deck.isEmpty()) {
            return ValidationResult.fail("Deck kartu sudah habis");
        }
        if (!player.hasResource(ResourceType.ORE,    1) ||
                !player.hasResource(ResourceType.WHEAT,  1) ||
                !player.hasResource(ResourceType.BANANA, 1)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya resource cukup untuk Kartu Temuan"
                + " (butuh 1 ORE + 1 WHEAT + 1 BANANA)");
        }
        return ValidationResult.ok();
    }

    // -------------------------------------------------------------------------
    // Fase setup
    // -------------------------------------------------------------------------

    /**
     * Memvalidasi pre-condition sebelum {@code placeInitialSettlement()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus fase setup</li>
     *   <li>Intersection kosong</li>
     *   <li>Distance rule</li>
     * </ul>
     */
    public ValidationResult canPlaceInitialSettlement(Player player,
                                                       Intersection intersection,
                                                       Board board,
                                                       GamePhase phase) {
        if (player == null || intersection == null || board == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (!phase.isSetupPhase()) {
            return ValidationResult.fail(
                "placeInitialSettlement() hanya boleh dipanggil saat fase setup (saat ini: " + phase + ")");
        }
        if (intersection.hasBuilding()) {
            return ValidationResult.fail(
                "Intersection #" + intersection.getId() + " sudah ada bangunan");
        }
        if (!board.isDistanceRuleValid(intersection)) {
            return ValidationResult.fail("Distance rule dilanggar");
        }
        return ValidationResult.ok();
    }

    /**
     * Memvalidasi pre-condition sebelum {@code placeInitialRoad()}.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus fase setup</li>
     *   <li>Path kosong (belum ada road)</li>
     *   <li>Path terhubung ke settlement pemain</li>
     * </ul>
     */
    public ValidationResult canPlaceInitialRoad(Player player, Path path, GamePhase phase) {
        if (player == null || path == null) {
            throw new IllegalArgumentException("Argumen tidak boleh null");
        }
        if (!phase.isSetupPhase()) {
            return ValidationResult.fail(
                "placeInitialRoad() hanya boleh dipanggil saat fase setup (saat ini: " + phase + ")");
        }
        if (path.hasRoad()) {
            return ValidationResult.fail("Path #" + path.getId() + " sudah ada road");
        }
        boolean connected = (path.getIntersectionA().getOwner() == player) ||
                            (path.getIntersectionB().getOwner() == player);
        if (!connected) {
            return ValidationResult.fail(
                "Road harus terhubung ke Pos Pantau milik " + player.getName());
        }
        return ValidationResult.ok();
    }
}
