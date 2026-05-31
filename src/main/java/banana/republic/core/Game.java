package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.board.TerrainType;
import banana.republic.building.Building;
import banana.republic.building.BuildingType;
import banana.republic.building.Laboratorium;
import banana.republic.building.PlayerSupply;
import banana.republic.building.PosPantau;
import banana.republic.building.Road;
import banana.republic.card.CardDeck;
import banana.republic.card.ExperimentCard;
import banana.republic.dice.Dice;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.SpecialCardType;
import banana.republic.plugin.MapGeneratorPlugin;
import banana.republic.resource.Bank;
import banana.republic.resource.BankImpl;
import banana.republic.resource.ResourceProductionService;
import banana.republic.resource.ResourceType;
import banana.republic.robber.Robber;
import banana.republic.timer.TurnTimer;
import banana.republic.save.GameSaveManager;
import banana.republic.trade.TradeManager;
import banana.republic.trade.TradeOffer;
import banana.republic.trade.ValidationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orkestrator utama permainan Banana Republic.
 *
 * Kelas ini adalah Facade (GoF Structural) dari seluruh sistem permainan.
 *
 * Semua aksi pemain masuk melalui kelas ini:
 * - build
 * - trade
 * - play card
 * - end turn
 *
 * Tidak ada modul lain yang perlu tahu detail implementasi internal.
 * Interaksi dilakukan lewat:
 * - GameState (untuk plugin)
 * - Method public kelas ini (untuk UI layer M5)
 */
public class Game {

    /** Jumlah Poin Prestasi yang dibutuhkan untuk menang. */
    public static final int VICTORY_POINTS_TO_WIN = 10;

    /** Batas kartu di tangan sebelum kena penalti dadu 7. */
    public static final int HAND_LIMIT = 7;

    private final List<Player> players;
    private final Board board;
    private final Bank bank;
    private final CardDeck cardDeck;
    private final Dice dice;
    private final Robber robber;
    private final GameLog gameLog;
    private final ResourceProductionService productionService;
    private TurnManager turnManager;

    /** Supply bangunan per pemain: Pos Pantau, Lab, Road. */
    private final Map<Player, PlayerSupply> supplies;
    private GameStateAdapter stateAdapter;
    private GamePhase currentPhase;
    private int turnNumber;
    private Player winner;
    private DiceResult lastDiceResult;

    /**
     * Jumlah Pos Pantau yang sudah ditempatkan pemain aktif di fase setup.
     *
     * Nilai:
     * - 0 = belum ada
     * - 1 = sudah taruh pertama (tunggu road)
     * - 2 = putaran selesai
     */
    private int setupSettlementCount;

    private ExperimentCard cardPlayedThisTurn;
    private ExperimentCard cardBoughtThisTurn;

    /** Manajemen trade domestik dan maritim per giliran. */
    private final TradeManager tradeManager;

    /** Kalkulasi Poin Prestasi dan penentuan pemenang. */
    private final VictoryPointCalculator vpCalculator;

    /**
     * Membuat instance Game baru dengan daftar pemain dan generator peta.
     *
     * @param players daftar pemain (3-4 orang)
     *        - tidak boleh null
     *        - tidak boleh kosong
     * @param mapPlugin generator peta yang menghasilkan Board
     *        - jika null, digunakan StandardMapGenerator secara default
     */
    public Game(List<Player> players, MapGeneratorPlugin mapPlugin) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException(
                "Players list cannot be null or empty");
        }
        if (players.size() < 3 || players.size() > 4) {
            throw new IllegalArgumentException(
                "Game requires 3 or 4 players, got: " + players.size());
        }

        this.players = new ArrayList<>(players);
        this.bank = new BankImpl();
        this.cardDeck = new CardDeck();
        this.cardDeck.buildDefaultDeck();
        this.dice = new Dice();
        this.gameLog = new GameLog();
        this.productionService = new ResourceProductionService();
        this.tradeManager = new TradeManager();
        this.vpCalculator = new VictoryPointCalculator();
        this.currentPhase = GamePhase.SETUP_FIRST_ROUND;
        this.turnNumber = 1;
        this.winner = null;
        this.lastDiceResult = null;
        this.setupSettlementCount = 0;
        this.cardPlayedThisTurn = null;
        this.cardBoughtThisTurn = null;

        // Supply bangunan per pemain
        this.supplies = new HashMap<>();
        for (Player p : this.players) {
            supplies.put(p, new PlayerSupply(p));
        }

        // Generate board — fallback ke StandardMapGenerator jika tidak ada
        // plugin
        MapGeneratorPlugin generator =
            (mapPlugin != null)
                ? mapPlugin
                : new banana.republic.plugin.StandardMapGenerator();
        this.board = generator.generateBoard();

        // Inisialisasi Robber setelah board tersedia (cari tile gurun)
        HexTile desertTile = findDesertTile();
        this.robber = new Robber(desertTile);

        // TurnManager dibuat setelah board siap agar this::endTurn bisa dipakai
        this.turnManager = new TurnManager(this.players, 0, this::endTurn);

        gameLog.addEntry(LogEntry.EventType.SYSTEM,
                         "Permainan Banana Republic dimulai dengan " +
                             this.players.size() + " pemain.");
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getActivePlayer() { return turnManager.getActivePlayer(); }

    public PlayerSupply getSupply(Player player) {
        return supplies.get(player);
    }

    public Board getBoard() { return board; }

    public Bank getBank() { return bank; }

    public CardDeck getCardDeck() { return cardDeck; }

    public Dice getDice() { return dice; }

    public Robber getRobber() { return robber; }

    public GameLog getGameLog() { return gameLog; }

    public TurnManager getTurnManager() { return turnManager; }

    public GamePhase getCurrentPhase() { return currentPhase; }

    public int getTurnNumber() { return turnNumber; }

    public DiceResult getLastDiceResult() { return lastDiceResult; }

    public Player getWinner() { return winner; }

    public int getSetupSettlementCount() { return setupSettlementCount; }

    /**
     * Mengembalikan GameState adapter singleton.
     *
     * Digunakan untuk:
     * - Plugin
     * - UI
     *
     * Lazy-init: adapter dibuat saat pertama kali dipanggil, lalu di-cache.
     */
    public GameState getState() {
        if (stateAdapter == null) {
            stateAdapter = new GameStateAdapter(this);
        }
        return stateAdapter;
    }

    /**
     * Memulai fase setup (inisiasi papan).
     *
     * Menentukan urutan giliran pertama berdasarkan lemparan dadu.
     */
    public void startSetupPhase() {
        currentPhase = GamePhase.SETUP_FIRST_ROUND;
        setupSettlementCount = 0;
        turnManager.setOrder(TurnOrder.CLOCKWISE);
        gameLog.addEntry(
            LogEntry.EventType.SYSTEM,
            "Fase Setup dimulai. Pemain menentukan urutan dengan dadu.");

        // Tentukan pemain pertama: setiap pemain lempar dadu, yang tertinggi
        // mulai
        int highestRoll = -1;
        int firstPlayerIndex = 0;
        for (int i = 0; i < players.size(); i++) {
            DiceResult roll = dice.roll();
            int total = roll.getTotal();
            gameLog.addEntry(
                LogEntry.EventType.SYSTEM, players.get(i).getName(),
                players.get(i).getName() + " melempar dadu: " + roll.getDie1() +
                    " + " + roll.getDie2() + " = " + total);
            if (total > highestRoll) {
                highestRoll = total;
                firstPlayerIndex = i;
            }
        }
        turnManager.setActiveIndex(firstPlayerIndex);
        gameLog.addEntry(
            LogEntry.EventType.TURN_CHANGE,
            players.get(firstPlayerIndex).getName(),
            players.get(firstPlayerIndex).getName() +
                " memulai pertama (dadu tertinggi: " + highestRoll + ")");
    }

    /**
     * Memulai permainan utama setelah fase setup selesai.
     */
    public void startMainGame() {
        assert currentPhase.isSetupPhase()
            : "startMainGame() hanya boleh dipanggil setelah setup selesai";
        currentPhase = GamePhase.RESOURCE_GATHERING;
        turnManager.setOrder(TurnOrder.CLOCKWISE);
        gameLog.addEntry(LogEntry.EventType.SYSTEM, "Permainan utama dimulai!");
    }

    /**
     * Menempatkan Pos Pantau awal pada fase setup.
     *
     * Hanya bisa dilakukan saat currentPhase.isSetupPhase()
     */
    public void placeInitialSettlement(Player player,
                                       Intersection intersection) {
        assert currentPhase.isSetupPhase()
            : ("placeInitialSettlement() hanya boleh dipanggil saat fase "
               + "setup");
        if (!currentPhase.isSetupPhase()) {
            throw new IllegalStateException("Bukan fase setup");
        }
        if (intersection == null) {
            throw new IllegalArgumentException("Intersection tidak boleh null");
        }
        if (intersection.hasBuilding()) {
            throw new IllegalStateException("Intersection sudah ada bangunan");
        }
        if (!board.isDistanceRuleValid(intersection)) {
            throw new IllegalStateException("Distance rule dilanggar");
        }

        PlayerSupply supply = supplies.get(player);
        PosPantau pp = supply.takePosPantau();
        intersection.placeBuilding(pp);
        setupSettlementCount++;

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
                         player.getName() +
                             " menempatkan Pos Pantau di intersection #" +
                             intersection.getId());

        if (currentPhase == GamePhase.SETUP_SECOND_ROUND) {
            productionService.distributeInitialResources(player, intersection,
                                                         bank, board);
        }
    }

    /**
     * Menempatkan Pipa Transportasi awal pada fase setup.
     */
    public void placeInitialRoad(Player player, Path path) {
        if (!currentPhase.isSetupPhase()) {
            throw new IllegalStateException(
                "placeInitialRoad() hanya boleh dipanggil saat fase setup");
        }
        if (path == null || path.hasRoad()) {
            throw new IllegalArgumentException(
                "Path tidak valid atau sudah ada road");
        }
        boolean connected = (path.getIntersectionA().getOwner() == player) ||
                            (path.getIntersectionB().getOwner() == player);
        if (!connected) {
            throw new IllegalStateException(
                "Road harus terhubung ke Pos Pantau milik " + player.getName());
        }

        PlayerSupply supply = supplies.get(player);
        Road road = supply.takeRoad();
        path.placeRoad(road);

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
                         player.getName() + " menempatkan Pipa di path #" +
                             path.getId());

        advanceSetupTurn();
    }

    /**
     * Melempar dadu untuk giliran ini (Fase 1: Resource Gathering).
     *
     * Jika hasilnya 7, mengaktifkan mekanisme Nimon Ungu.
     */
    public DiceResult rollDice() {
        if (currentPhase != GamePhase.RESOURCE_GATHERING) {
            throw new IllegalStateException(
                "Dadu hanya boleh dilempar pada fase RESOURCE_GATHERING");
        }

        lastDiceResult = dice.roll();
        int total = lastDiceResult.getTotal();

        gameLog.addEntry(LogEntry.EventType.SYSTEM, getActivePlayer().getName(),
                         getActivePlayer().getName() + " melempar dadu: " +
                             lastDiceResult.getDie1() + " + " +
                             lastDiceResult.getDie2() + " = " + total);

        if (lastDiceResult.isSeven()) {
            // Nimon Ungu aktif — diproses di Fase 4; sementara set phase ke
            // TRADE_BUILD
            gameLog.addEntry(LogEntry.EventType.ROBBER,
                             getActivePlayer().getName(),
                             "Dadu 7! Nimon Ungu aktif.");
        } else {
            // Distribusi resource normal
            productionService.distributeForRoll(total, board, players, bank);
        }

        // Pindah ke fase Trade/Build dan mulai timer
        currentPhase = GamePhase.TRADE_BUILD;
        cardPlayedThisTurn = null;
        cardBoughtThisTurn = null;
        return lastDiceResult;
    }

    /**
     * Mengakhiri giliran pemain aktif.
     *
     * Memindahkan giliran ke pemain berikutnya.
     */
    public void endTurn() {
        if (currentPhase == GamePhase.GAME_OVER)
            return;

        turnManager.stopTimer();
        tradeManager.reset(); // tutup semua negosiasi yang masih terbuka
        Player prev = getActivePlayer();
        turnManager.advanceTurn();
        currentPhase = GamePhase.RESOURCE_GATHERING;
        turnNumber++;

        gameLog.addEntry(LogEntry.EventType.TURN_CHANGE, prev.getName(),
                         prev.getName() +
                             " mengakhiri giliran. Sekarang giliran " +
                             getActivePlayer().getName() + ".");
    }

    /**
     * Memulai TurnTimer untuk fase Trade/Build.
     *
     * Dipanggil oleh UI (GameController) setelah rollDice() selesai.
     */
    public void startTradeBuildTimer(TurnTimer.OnTickCallback onTick) {
        if (currentPhase == GamePhase.TRADE_BUILD) {
            turnManager.startTimer(onTick);
        }
    }

    /**
     * Pemain aktif membuat penawaran dagang ke pemain lain (atau broadcast ke semua).
     *
     * Gunakan target = null untuk broadcast offer.
     *
     * @param offer penawaran yang dibuat
     * @return hasil validasi; jika gagal offer tidak dibuat
     */
    public ValidationResult makeTradeOffer(TradeOffer offer) {
        ValidationResult result = tradeManager.makeOffer(offer, currentPhase);
        if (result.isValid()) {
            String targetName = offer.getTarget() != null
                                    ? offer.getTarget().getName()
                                    : "semua pemain";
            gameLog.addEntry(LogEntry.EventType.TRADE,
                             offer.getOfferer().getName(),
                             offer.getOfferer().getName() +
                                 " menawarkan dagang ke " + targetName);
        }
        return result;
    }

    /**
     * Pemain acceptingPlayer menerima offer yang sedang aktif.
     *
     * Transfer resource dilakukan otomatis.
     *
     * @param acceptingPlayer pemain yang menerima
     * @return hasil validasi
     */
    public ValidationResult acceptTradeOffer(Player acceptingPlayer) {
        ValidationResult result = tradeManager.acceptOffer(acceptingPlayer);
        if (result.isValid()) {
            gameLog.addEntry(
                LogEntry.EventType.TRADE, acceptingPlayer.getName(),
                acceptingPlayer.getName() + " menerima penawaran dagang.");
        }
        return result;
    }

    /**
     * Pemain rejectingPlayer menolak offer yang sedang aktif.
     *
     * @param rejectingPlayer pemain yang menolak
     * @return hasil validasi
     */
    public ValidationResult rejectTradeOffer(Player rejectingPlayer) {
        ValidationResult result = tradeManager.rejectOffer(rejectingPlayer);
        if (result.isValid()) {
            gameLog.addEntry(
                LogEntry.EventType.TRADE, rejectingPlayer.getName(),
                rejectingPlayer.getName() + " menolak penawaran dagang.");
        }
        return result;
    }

    /**
     * Target mengajukan counter-offer ke offerer.
     *
     * @param counter penawaran balik dari target
     * @return hasil validasi
     */
    public ValidationResult counterTradeOffer(TradeOffer counter) {
        ValidationResult result =
            tradeManager.counterOffer(counter, currentPhase);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE,
                             counter.getOfferer().getName(),
                             counter.getOfferer().getName() +
                                 " mengajukan counter-offer.");
        }
        return result;
    }

    /**
     * Offerer membatalkan offer aktif.
     *
     * @param offerer pemain yang membatalkan (harus offerer asli)
     * @return hasil validasi
     */
    public ValidationResult cancelTradeOffer(Player offerer) {
        ValidationResult result = tradeManager.cancelOffer(offerer);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE, offerer.getName(),
                             offerer.getName() +
                                 " membatalkan penawaran dagang.");
        }
        return result;
    }

    /**
     * Pemain melakukan trade maritim dengan bank.
     *
     * Rasio otomatis dihitung berdasarkan harbor terbaik pemain.
     *
     * @param player pemain yang trade
     * @param sellType resource yang dijual
     * @param buyType resource yang dibeli (selalu 1 unit)
     * @return hasil validasi
     */
    public ValidationResult tradeWithBank(Player player, ResourceType sellType,
                                          ResourceType buyType) {
        ValidationResult result = tradeManager.tradeWithBank(
            player, sellType, buyType, bank, board, currentPhase);
        if (result.isValid()) {
            int ratio = tradeManager.getBestTradeRatio(player, sellType, board);
            gameLog.addEntry(LogEntry.EventType.TRADE, player.getName(),
                             player.getName() + " trade " + ratio + " " +
                                 sellType.getDisplayName() + " → 1 " +
                                 buyType.getDisplayName() + " (bank)");
        }
        return result;
    }

    /**
     * Mengembalikan rasio trade terbaik pemain untuk resource tertentu.
     *
     * Berguna untuk UI menampilkan harbor panel.
     *
     * @param player pemain
     * @param sellType resource yang akan dijual
     * @return rasio (2, 3, atau 4)
     */
    public int getTradeRatio(Player player, ResourceType sellType) {
        return tradeManager.getBestTradeRatio(player, sellType, board);
    }

    /**
     * Mengembalikan semua rasio trade pemain (satu per ResourceType).
     *
     * Index sesuai urutan ResourceType#values().
     */
    public int[] getAllTradeRatios(Player player) {
        return tradeManager.getAllTradeRatios(player, board);
    }

    /**
     * Mengembalikan TradeManager untuk UI yang butuh akses negosiasi aktif.
     */
    public TradeManager getTradeManager() { return tradeManager; }

    public void buildRoad(Player player, Path path) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException(
                "buildRoad() hanya boleh dipanggil saat fase TRADE_BUILD");
        }
        if (player == null || path == null) {
            throw new IllegalArgumentException(
                "Player dan Path tidak boleh null");
        }
        if (path.hasRoad()) {
            throw new IllegalStateException("Path sudah ada road");
        }
        if (!board.isPathConnectedToPlayer(path, player)) {
            throw new IllegalStateException(
                "Road harus terhubung ke jaringan " + player.getName());
        }

        // Biaya: 1 WOOD + 1 BRICK
        assert player.hasResource(ResourceType.WOOD, 1) &&
            player.hasResource(ResourceType.BRICK, 1)
            : "Pemain tidak punya resource cukup untuk membangun road";
        if (!player.hasResource(ResourceType.WOOD, 1) ||
            !player.hasResource(ResourceType.BRICK, 1)) {
            throw new IllegalStateException(
                player.getName() + " tidak punya resource cukup untuk Pipa");
        }

        PlayerSupply supply = supplies.get(player);
        if (!supply.canBuildRoad()) {
            throw new IllegalStateException(player.getName() +
                                            " sudah kehabisan stok Pipa");
        }

        // Deduct resource
        player.removeResource(ResourceType.WOOD, 1);
        player.removeResource(ResourceType.BRICK, 1);
        bank.returnResource(ResourceType.WOOD, 1);
        bank.returnResource(ResourceType.BRICK, 1);

        // Tempatkan road
        Road road = supply.takeRoad();
        path.placeRoad(road);

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
                player.getName() + " membangun Pipa Transportasi di path #" + path.getId());

        // Perbarui Jalan Terpanjang setelah road baru dipasang
        updateLongestRoad();
    }

    /**
     * Membangun Pos Pantau milik pemain di persimpangan yang ditentukan.
     */
    public void buildSettlement(Player player, Intersection intersection) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException("buildSettlement() hanya boleh "
                                            +
                                            "dipanggil saat fase TRADE_BUILD");
        }
        if (player == null || intersection == null) {
            throw new IllegalArgumentException(
                "Player dan Intersection tidak boleh null");
        }
        if (intersection.hasBuilding()) {
            throw new IllegalStateException("Intersection sudah ada bangunan");
        }
        if (!board.isDistanceRuleValid(intersection)) {
            throw new IllegalStateException(
                "Distance rule dilanggar — harus berjarak minimal 2 edge");
        }
        // Harus terhubung ke minimal 1 road milik pemain
        boolean connected = intersection.getAdjacentPaths().stream().anyMatch(
            p -> p.hasRoad() && player.equals(p.getOwner()));
        if (!connected) {
            throw new IllegalStateException(
                "Pos Pantau harus terhubung ke Pipa milik " + player.getName());
        }

        // Biaya: 1 WOOD + 1 BRICK + 1 WHEAT + 1 BANANA
        assert player.hasResource(ResourceType.WOOD, 1) &&
            player.hasResource(ResourceType.BRICK, 1) &&
            player.hasResource(ResourceType.WHEAT, 1) &&
            player.hasResource(ResourceType.BANANA, 1)
            : "Pemain tidak punya resource cukup untuk Pos Pantau";
        if (!player.hasResource(ResourceType.WOOD, 1) ||
            !player.hasResource(ResourceType.BRICK, 1) ||
            !player.hasResource(ResourceType.WHEAT, 1) ||
            !player.hasResource(ResourceType.BANANA, 1)) {
            throw new IllegalStateException(
                player.getName() +
                " tidak punya resource cukup untuk Pos Pantau");
        }

        PlayerSupply supply = supplies.get(player);
        if (!supply.canBuildPosPantau()) {
            throw new IllegalStateException(
                player.getName() + " sudah kehabisan stok Pos Pantau");
        }

        // Deduct resource
        player.removeResource(ResourceType.WOOD, 1);
        player.removeResource(ResourceType.BRICK, 1);
        player.removeResource(ResourceType.WHEAT, 1);
        player.removeResource(ResourceType.BANANA, 1);
        bank.returnResource(ResourceType.WOOD, 1);
        bank.returnResource(ResourceType.BRICK, 1);
        bank.returnResource(ResourceType.WHEAT, 1);
        bank.returnResource(ResourceType.BANANA, 1);

        // Tempatkan Pos Pantau
        PosPantau pp = supply.takePosPantau();
        intersection.placeBuilding(pp);

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
                         player.getName() +
                             " membangun Pos Pantau di intersection #" +
                             intersection.getId());
    }

    /**
     * Mengupgrade Pos Pantau menjadi Laboratorium.
     */
    public void buildCity(Player player, Intersection intersection) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException(
                "buildCity() hanya boleh dipanggil saat fase TRADE_BUILD");
        }
        if (player == null || intersection == null) {
            throw new IllegalArgumentException(
                "Player dan Intersection tidak boleh null");
        }
        if (!intersection.hasBuilding()) {
            throw new IllegalStateException(
                "Intersection tidak ada bangunan untuk di-upgrade");
        }
        if (!player.equals(intersection.getOwner())) {
            throw new IllegalStateException(
                "Hanya pemilik Pos Pantau yang bisa upgrade ke Laboratorium");
        }
        if (intersection.getBuilding().getBuildingType() !=
            BuildingType.POS_PANTAU) {
            throw new IllegalStateException(
                "Hanya Pos Pantau yang bisa di-upgrade");
        }

        // Biaya: 2 WHEAT + 3 ORE
        assert player.hasResource(ResourceType.WHEAT, 2) &&
            player.hasResource(ResourceType.ORE, 3)
            : "Pemain tidak punya resource cukup untuk Laboratorium";
        if (!player.hasResource(ResourceType.WHEAT, 2) ||
            !player.hasResource(ResourceType.ORE, 3)) {
            throw new IllegalStateException(
                player.getName() +
                " tidak punya resource cukup untuk Laboratorium");
        }

        PlayerSupply supply = supplies.get(player);
        if (!supply.canBuildLaboratorium()) {
            throw new IllegalStateException(
                player.getName() + " sudah kehabisan stok Laboratorium");
        }

        // Deduct resource
        player.removeResource(ResourceType.WHEAT, 2);
        player.removeResource(ResourceType.ORE, 3);
        bank.returnResource(ResourceType.WHEAT, 2);
        bank.returnResource(ResourceType.ORE, 3);

        // Tukar PosPantau → Laboratorium
        Building removed = intersection.removeBuilding();
        if (removed instanceof PosPantau) {
            supply.returnPosPantau(
                (PosPantau)removed); // kembalikan PosPantau ke supply
        }
        Laboratorium lab = supply.takeLaboratorium();
        intersection.placeBuilding(lab);

        gameLog.addEntry(
            LogEntry.EventType.BUILD, player.getName(),
            player.getName() +
                " meng-upgrade Pos Pantau ke Laboratorium di intersection #" +
                intersection.getId());
    }

    /**
     * Membeli Kartu Temuan dari deck.
     */
    public void buyDevelopmentCard(Player player) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException(
                "Kartu hanya bisa dibeli saat fase TRADE_BUILD");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player tidak boleh null");
        }
        if (cardDeck.isEmpty()) {
            throw new IllegalStateException("Deck kartu sudah habis");
        }

        // Biaya: 1 ORE + 1 WHEAT + 1 BANANA
        assert player.hasResource(ResourceType.ORE, 1) &&
            player.hasResource(ResourceType.WHEAT, 1) &&
            player.hasResource(ResourceType.BANANA, 1)
            : "Pemain tidak punya resource cukup untuk membeli Kartu Temuan";
        if (!player.hasResource(ResourceType.ORE, 1) ||
            !player.hasResource(ResourceType.WHEAT, 1) ||
            !player.hasResource(ResourceType.BANANA, 1)) {
            throw new IllegalStateException(
                player.getName() +
                " tidak punya resource cukup untuk Kartu Temuan");
        }

        // Deduct resource
        player.removeResource(ResourceType.ORE, 1);
        player.removeResource(ResourceType.WHEAT, 1);
        player.removeResource(ResourceType.BANANA, 1);
        bank.returnResource(ResourceType.ORE, 1);
        bank.returnResource(ResourceType.WHEAT, 1);
        bank.returnResource(ResourceType.BANANA, 1);

        // Tarik kartu — tidak bisa langsung dimainkan giliran ini
        ExperimentCard card = cardDeck.draw();
        player.addCard(card);
        cardBoughtThisTurn = card;

        gameLog.addEntry(LogEntry.EventType.CARD_BOUGHT, player.getName(),
                         player.getName() + " membeli Kartu Temuan (" +
                             card.getCardName() + ")");
    }

    /**
     * Mengaktifkan mekanisme Nimon Ungu.
     *
     * Meliputi:
     * - Memindahkan robber
     * - Mencuri resource (opsional)
     */
    public void activateRobber(HexTile targetTile, Player victim) {
        if (targetTile == null) {
            throw new IllegalArgumentException("Target tile tidak boleh null");
        }
        if (targetTile.equals(robber.getCurrentTile())) {
            throw new IllegalStateException(
                "Nimon Ungu harus pindah ke petak berbeda");
        }

        // Pindahkan Nimon Ungu
        robber.move(targetTile);
        gameLog.addEntry(LogEntry.EventType.ROBBER, getActivePlayer().getName(),
                         getActivePlayer().getName() +
                             " memindahkan Nimon Ungu ke tile #" +
                             targetTile.getId());

        // Curi resource dari victim (opsional, victim boleh null jika tidak
        // ada yang dicuri)
        if (victim != null) {
            List<Player> eligible =
                robber.getEligibleVictims(getActivePlayer(), board);
            if (!eligible.contains(victim)) {
                throw new IllegalStateException(
                    victim.getName() + (" tidak eligible untuk dicuri di "
                                        + "posisi Nimon Ungu saat ini"));
            }
            if (victim.getTotalResourceCount() == 0) {
                // Victim tidak punya resource — skip steal tanpa error
                gameLog.addEntry(
                    LogEntry.EventType.STEAL, getActivePlayer().getName(),
                    victim.getName() + " tidak punya resource untuk dicuri.");
            } else {
                ResourceType stolen =
                    robber.stealRandomResource(getActivePlayer(), victim);
                gameLog.addEntry(
                    LogEntry.EventType.STEAL, getActivePlayer().getName(),
                    getActivePlayer().getName() + " mencuri 1 " +
                        stolen.getDisplayName() + " dari " + victim.getName());
            }
        }

        // Setelah robber aktif (dari dadu 7), lanjutkan ke fase Trade/Build
        currentPhase = GamePhase.TRADE_BUILD;
    }

    /**
     * Memproses pembuangan kartu dari semua pemain.
     *
     * Berlaku untuk pemain yang memiliki lebih dari HAND_LIMIT kartu
     * (efek dadu 7, langkah 1).
     */
    public void processDiscardPhase() {
        // Cek siapa yang harus buang kartu (lebih dari HAND_LIMIT resource)
        Map<Player, Integer> discardMap = robber.activateDiscardPhase(players);
        if (discardMap.isEmpty()) {
            return;
        }

        for (Map.Entry<Player, Integer> entry : discardMap.entrySet()) {
            Player player = entry.getKey();
            int toDiscard = entry.getValue();

            // Bot: buang resource acak secara otomatis
            if (player.isBot()) {
                int discarded = 0;
                for (ResourceType type : ResourceType.values()) {
                    if (discarded >= toDiscard)
                        break;
                    int count = player.getResourceCount(type);
                    int remove = Math.min(count, toDiscard - discarded);
                    if (remove > 0) {
                        player.removeResource(type, remove);
                        bank.returnResource(type, remove);
                        discarded += remove;
                    }
                }
                gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
                                 player.getName() + " (bot) membuang " +
                                     discarded + " kartu resource.");
            } else {
                // Human: dicatat saja — UI akan meminta pemain memilih kartu
                // yang dibuang UI harus memanggil discardResource(player, type,
                // amount) per resource
                gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
                                 player.getName() + " harus membuang " +
                                     toDiscard + " kartu resource.");
            }
        }
    }

    /**
     * Dipakai UI untuk memproses pembuangan resource satu per satu.
     *
     * Digunakan untuk human player.
     */
    public void discardResource(Player player, ResourceType type, int amount) {
        if (player == null || type == null) {
            throw new IllegalArgumentException(
                "Player dan ResourceType tidak boleh null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah yang dibuang harus > 0");
        }
        if (!player.hasResource(type, amount)) {
            throw new IllegalStateException(player.getName() + " tidak punya " +
                                            amount + " " + type);
        }
        player.removeResource(type, amount);
        bank.returnResource(type, amount);
        gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
                         player.getName() + " membuang " + amount + " " +
                             type.getDisplayName());
    }

    /**
     * Memainkan satu Kartu Temuan milik pemain aktif.
     *
     * Kartu yang baru dibeli tidak bisa langsung dimainkan giliran ini.
     * Hanya boleh memainkan 1 kartu per giliran.
     */
    public void playCard(Player player, ExperimentCard card) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException(
                "Kartu hanya bisa dimainkan saat fase TRADE_BUILD");
        }
        if (player == null || card == null) {
            throw new IllegalArgumentException(
                "Player dan kartu tidak boleh null");
        }
        if (!player.getHandCards().contains(card)) {
            throw new IllegalStateException(
                player.getName() + " tidak punya kartu tersebut di tangan");
        }
        if (card == cardBoughtThisTurn) {
            throw new IllegalStateException(
                "Kartu yang baru dibeli tidak bisa langsung dimainkan "
                + "giliran ini");
        }
        if (cardPlayedThisTurn != null) {
            throw new IllegalStateException(
                "Hanya boleh memainkan 1 kartu per giliran");
        }
        if (!card.isPlayable()) {
            throw new IllegalStateException("Kartu " + card.getCardName() +
                                            " tidak bisa dimainkan saat ini");
        }

        // Jalankan efek kartu: card akan memanggil state.chooseKnightTarget()
        // dsb. bila perlu
        card.applyEffect(getState(), player);
        cardPlayedThisTurn = card;

        // Pindahkan ke discard pile
        player.removeCard(card);
        cardDeck.addToDiscardPile(card);

        gameLog.addEntry(LogEntry.EventType.CARD_PLAYED, player.getName(),
                         player.getName() + " memainkan " + card.getCardName());

        // Update Largest Army setelah Knight dimainkan
        updateLargestArmy();
    }

    /**
     * Memeriksa apakah ada pemain yang mencapai kemenangan.
     *
     * VICTORY_POINTS_TO_WIN = 10 PP
     * Belum diproses sebagai pemenang.
     */
    public Player checkVictory() {
        if (currentPhase == GamePhase.GAME_OVER && winner != null) {
            return winner; // sudah pernah ditetapkan
        }

        Player candidate = vpCalculator.findWinner(players, board, VICTORY_POINTS_TO_WIN);
        if (candidate != null) {
            winner = candidate;
            currentPhase = GamePhase.GAME_OVER;
            turnManager.stopTimer();

            int totalVP = vpCalculator.getTotalVP(winner, board);
            gameLog.addEntry(LogEntry.EventType.VICTORY, winner.getName(),
                    winner.getName() + " MENANG dengan " + totalVP + " Poin Prestasi!");
        }
        return winner;
    }

    /**
     * Menyimpan state permainan ke file.
     */
    public void saveGame(String filePath) {
        GameSaveManager.saveGame(this, filePath);
    }

    /**
     * Memuat state permainan dari file yang sudah disimpan.
     */
    public static Game loadGame(String filePath) {
        return GameSaveManager.loadGame(filePath);
    }

    /**
     * Constructor khusus untuk restore state dari save data.
     */
    public Game(List<Player> players, Board board, Bank bank, CardDeck cardDeck,
                GamePhase phase, int turnNumber, int activeIndex,
                int setupSettlementCount, DiceResult lastDiceResult) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException(
                "Players list cannot be null or empty");
        }
        if (players.size() < 3 || players.size() > 4) {
            throw new IllegalArgumentException(
                "Game requires 3 or 4 players, got: " + players.size());
        }
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null");
        }
        if (bank == null) {
            throw new IllegalArgumentException("Bank cannot be null");
        }
        if (cardDeck == null) {
            throw new IllegalArgumentException("CardDeck cannot be null");
        }

        this.players = new ArrayList<>(players);
        this.board = board;
        this.bank = bank;
        this.cardDeck = cardDeck;
        this.dice = new Dice();
        this.gameLog = new GameLog();
        this.productionService = new ResourceProductionService();
        this.tradeManager = new TradeManager();
        this.vpCalculator = new VictoryPointCalculator();
        this.currentPhase = (phase != null) ? phase : GamePhase.RESOURCE_GATHERING;
        this.turnNumber = turnNumber;
        this.winner = null;
        this.lastDiceResult = lastDiceResult;
        this.setupSettlementCount = setupSettlementCount;
        this.cardPlayedThisTurn = null;
        this.cardBoughtThisTurn = null;

        // Supply bangunan per pemain
        this.supplies = new HashMap<>();
        for (Player p : this.players) {
            supplies.put(p, p.getSupply());
        }

        // Inisialisasi Robber
        HexTile robberTile = board.getRobberTile().orElse(findDesertTile());
        this.robber = new Robber(robberTile);

        // TurnManager
        this.turnManager = new TurnManager(this.players, activeIndex, this::endTurn);
        if (this.currentPhase == GamePhase.SETUP_SECOND_ROUND) {
            this.turnManager.setOrder(TurnOrder.COUNTER_CLOCKWISE);
        } else {
            this.turnManager.setOrder(TurnOrder.CLOCKWISE);
        }

        if (this.currentPhase == GamePhase.GAME_OVER) {
            this.winner = vpCalculator.findWinner(this.players, board, VICTORY_POINTS_TO_WIN);
        }
    }

    /**
     * Memajukan giliran saat fase setup.
     *
     * Dipanggil setelah satu pasang (Settlement + Road) ditempatkan.
     *
     * Menangani transisi:
     *
     * Putaran 1 (Clockwise):
     * - Setiap pemain taruh 1 Settlement + 1 Road
     * - Transisi: pemain terakhir langsung mulai putaran 2
     *
     * Putaran 2 (Counter-clockwise):
     * - Setiap pemain taruh 1 Settlement + 1 Road
     * - Urutan terbalik
     * - Setelah semua selesai: startMainGame()
     */
    private void advanceSetupTurn() {
        int playerCount = players.size();
        // setupSettlementCount: total settlement yang sudah ditempatkan seluruh
        // pemain
        if (currentPhase == GamePhase.SETUP_FIRST_ROUND) {
            if (setupSettlementCount < playerCount) {
                // Masih dalam putaran pertama maju ke pemain berikutnya
                turnManager.advanceTurnInDirection(TurnOrder.CLOCKWISE);
            } else {
                // Putaran pertama selesai pemain terakhir langsung mulai
                // putaran 2 (tidak bergerak)
                currentPhase = GamePhase.SETUP_SECOND_ROUND;
                turnManager.setOrder(TurnOrder.COUNTER_CLOCKWISE);
                gameLog.addEntry(LogEntry.EventType.SYSTEM,
                                 "Putaran setup pertama selesai. Mulai "
                                     + "putaran kedua (berlawanan arah).");
            }
        } else if (currentPhase == GamePhase.SETUP_SECOND_ROUND) {
            if (setupSettlementCount < playerCount * 2) {
                turnManager.advanceTurnInDirection(TurnOrder.COUNTER_CLOCKWISE);
            } else {
                // Semua pemain selesai setup
                gameLog.addEntry(LogEntry.EventType.SYSTEM,
                                 "Fase setup selesai!");
                startMainGame();
            }
        }
    }

    /**
     * Mencari tile gurun di board untuk inisialisasi Robber.
     *
     * Jika tidak ada tile gurun, gunakan tile pertama sebagai fallback.
     */
    private HexTile findDesertTile() {
        for (HexTile tile : board.getAllHexTiles()) {
            if (tile.getTerrainType() == TerrainType.DESERT) {
                return tile;
            }
        }
        // Fallback: gunakan tile pertama jika tidak ada gurun
        return board.getAllHexTiles().get(0);
    }

    /**
     * Memeriksa dan memperbarui Largest Army (Pasukan Terbesar).
     *
     * Dipanggil setelah setiap Knight Card dimainkan.
     *
     * Syarat:
     * - Minimal 3 knight
     * - Lebih banyak dari pemegang saat ini
     *
     * Memberi 2 Poin Prestasi tambahan kepada pemegangnya.
     */
    /**
     * Memperbarui kepemilikan Jalan Terpanjang setelah road baru dipasang.
     * Dipanggil secara internal oleh {@link #buildRoad} dan {@link #placeInitialRoad}.
     */
    private void updateLongestRoad() {
        Player prev    = null;
        for (Player p : players) {
            if (p.hasSpecialCard(SpecialCardType.LONGEST_ROAD)) { prev = p; break; }
        }

        Player newHolder = vpCalculator.updateLongestRoad(players);

        if (newHolder != null && !newHolder.equals(prev)) {
            if (prev != null) {
                gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD, prev.getName(),
                        prev.getName() + " kehilangan Jalan Terpanjang.");
            }
            gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD, newHolder.getName(),
                    newHolder.getName() + " mendapat Jalan Terpanjang ("
                    + newHolder.getLongestRoadLength() + " road)! (+2 PP)");
        }
    }

    // =========================================================================
    // VP query — Fase 6
    // =========================================================================

    /**
     * Menghitung dan mengembalikan breakdown VP pemain tertentu.
     * Berguna untuk UI yang ingin menampilkan detail skor pemain.
     *
     * @param player pemain yang ingin dihitung VP-nya
     * @return {@link VictoryPointBreakdown} berisi total dan rincian per kategori
     */
    public VictoryPointBreakdown getVPBreakdown(Player player) {
        return vpCalculator.calculate(player, board);
    }

    /**
     * Menghitung VP semua pemain sekaligus, diurutkan dari tertinggi ke terendah.
     * Berguna untuk leaderboard / scoreboard UI.
     *
     * @return list {@link VictoryPointBreakdown} terurut descending
     */
    public java.util.List<VictoryPointBreakdown> getAllVPBreakdowns() {
        return vpCalculator.calculateAll(players, board);
    }

    /**
     * Shortcut total VP pemain tanpa breakdown rincian.
     *
     * @param player pemain yang dihitung
     * @return total VP termasuk VictoryPointCard tersembunyi
     */
    public int getVPTotal(Player player) {
        return vpCalculator.getTotalVP(player, board);
    }

    private void updateLargestArmy() {
        final int MIN_KNIGHTS = 3;
        Player currentHolder = null;
        int holderKnights = 0;

        // Temukan pemegang Largest Army saat ini
        for (Player p : players) {
            if (p.hasSpecialCard(SpecialCardType.LARGEST_ARMY)) {
                currentHolder = p;
                holderKnights = p.getKnightsPlayed();
                break;
            }
        }

        // Cek apakah ada pemain yang layak mengambil alih
        for (Player p : players) {
            int knights = p.getKnightsPlayed();
            if (knights < MIN_KNIGHTS)
                continue;

            boolean qualifies = (currentHolder == null)
                                    ? knights >= MIN_KNIGHTS
                                    : knights > holderKnights;

            if (qualifies && p != currentHolder) {
                // Ambil dari pemegang lama
                if (currentHolder != null) {
                    currentHolder.setSpecialCard(SpecialCardType.LARGEST_ARMY,
                                                 false);
                    gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD,
                                     currentHolder.getName(),
                                     currentHolder.getName() +
                                         " kehilangan Pasukan Terbesar.");
                }
                // Berikan ke pemegang baru
                p.setSpecialCard(SpecialCardType.LARGEST_ARMY, true);
                gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD, p.getName(),
                                 p.getName() + " mendapat Pasukan Terbesar (" +
                                     knights + " knight)! (+2 PP)");
                break;
            }
        }
    }

    @Override
    public String toString() {
        return String.format(
            "Game[phase=%s, turn=%d, activePlayer=%s, players=%d]",
            currentPhase, turnNumber, getActivePlayer().getName(),
            players.size());
    }
}
