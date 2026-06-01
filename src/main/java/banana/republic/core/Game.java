package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.board.TerrainType;
import banana.republic.building.PlayerSupply;
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
import banana.republic.robber.RobberService;
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
 * Kelas ini adalah <strong>Facade</strong> (GoF Structural) dari seluruh sistem permainan.
 * Setiap method public mendelegasikan pekerjaannya ke service yang tepat:
 * <ul>
 *   <li>{@link BuildValidator}  — validasi pre-condition sebelum build/kartu</li>
 *   <li>{@link BuildExecutor}   — mutasi state bangunan dan resource</li>
 *   <li>{@link CardPlayValidator} — validasi memainkan kartu</li>
 *   <li>{@link VictoryPointCalculator} — kalkulasi VP, Longest Road, Largest Army</li>
 *   <li>{@link RobberService}   — pembuangan otomatis resource bot</li>
 *   <li>{@link TradeManager}    — orkestrasi trade domestik dan maritim</li>
 * </ul>
 *
 * Tidak ada logika bisnis yang hidup di dalam kelas ini — hanya koordinasi.
 */
public class Game {

    /** Jumlah Poin Prestasi yang dibutuhkan untuk menang. */
    public static final int VICTORY_POINTS_TO_WIN = 10;

    /** Batas kartu di tangan sebelum kena penalti dadu 7. */
    public static final int HAND_LIMIT = 7;

    // -------------------------------------------------------------------------
    // State fields
    // -------------------------------------------------------------------------

    private final List<Player>          players;
    private final Board                 board;
    private final Bank                  bank;
    private final CardDeck              cardDeck;
    private final Dice                  dice;
    private final Robber                robber;
    private final GameLog               gameLog;
    private final Map<Player, PlayerSupply> supplies;

    private TurnManager     turnManager;
    private GameStateAdapter stateAdapter;
    private GamePhase       currentPhase;
    private int             turnNumber;
    private Player          winner;
    private DiceResult      lastDiceResult;
    private int             setupSettlementCount;
    private ExperimentCard  cardPlayedThisTurn;
    private ExperimentCard  cardBoughtThisTurn;

    // -------------------------------------------------------------------------
    // Service fields (injected / created here)
    // -------------------------------------------------------------------------

    private final ResourceProductionService productionService;
    private final TradeManager              tradeManager;
    private final VictoryPointCalculator    vpCalculator;
    private final BuildValidator            buildValidator;
    private final BuildExecutor             buildExecutor;
    private final CardPlayValidator         cardPlayValidator;
    private final RobberService             robberService;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Membuat instance Game baru dengan daftar pemain dan generator peta.
     *
     * @param players   daftar pemain (3–4 orang), tidak boleh null atau kosong
     * @param mapPlugin generator peta; jika null, digunakan {@code StandardMapGenerator}
     */
    public Game(List<Player> players, MapGeneratorPlugin mapPlugin) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }
        if (players.size() < 3 || players.size() > 4) {
            throw new IllegalArgumentException(
                "Game requires 3 or 4 players, got: " + players.size());
        }

        this.players = new ArrayList<>(players);

        // Services
        this.bank              = new BankImpl();
        this.cardDeck          = new CardDeck();
        this.dice              = new Dice();
        this.gameLog           = new GameLog();
        this.productionService = new ResourceProductionService();
        this.tradeManager      = new TradeManager();
        this.vpCalculator      = new VictoryPointCalculator();
        this.buildValidator    = new BuildValidator();
        this.buildExecutor     = new BuildExecutor();
        this.cardPlayValidator = new CardPlayValidator();
        this.robberService     = new RobberService();

        // State
        this.currentPhase         = GamePhase.SETUP_FIRST_ROUND;
        this.turnNumber           = 1;
        this.winner               = null;
        this.lastDiceResult       = null;
        this.setupSettlementCount = 0;
        this.cardPlayedThisTurn   = null;
        this.cardBoughtThisTurn   = null;

        // Supply bangunan per pemain
        this.supplies = new HashMap<>();
        for (Player p : this.players) {
            supplies.put(p, new PlayerSupply(p));
        }

        // Generate board — fallback ke StandardMapGenerator jika tidak ada plugin
        MapGeneratorPlugin generator = (mapPlugin != null)
            ? mapPlugin
            : new banana.republic.plugin.StandardMapGenerator();
        this.board = generator.generateBoard();

        // Inisialisasi Robber setelah board tersedia (cari tile gurun)
        this.robber = new Robber(findDesertTile());

        // TurnManager dibuat setelah board siap agar this::endTurn bisa dipakai
        this.turnManager = new TurnManager(this.players, 0, this::endTurn);

        gameLog.addEntry(LogEntry.EventType.SYSTEM,
            "Permainan Banana Republic dimulai dengan " + this.players.size() + " pemain.");
    }

    /**
     * Constructor khusus untuk restore state dari save data.
     */
    public Game(List<Player> players, Board board, Bank bank, CardDeck cardDeck,
                GamePhase phase, int turnNumber, int activeIndex,
                int setupSettlementCount, DiceResult lastDiceResult) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }
        if (players.size() < 3 || players.size() > 4) {
            throw new IllegalArgumentException(
                "Game requires 3 or 4 players, got: " + players.size());
        }
        if (board == null)    throw new IllegalArgumentException("Board cannot be null");
        if (bank == null)     throw new IllegalArgumentException("Bank cannot be null");
        if (cardDeck == null) throw new IllegalArgumentException("CardDeck cannot be null");

        this.players  = new ArrayList<>(players);
        this.board    = board;
        this.bank     = bank;
        this.cardDeck = cardDeck;

        this.dice              = new Dice();
        this.gameLog           = new GameLog();
        this.productionService = new ResourceProductionService();
        this.tradeManager      = new TradeManager();
        this.vpCalculator      = new VictoryPointCalculator();
        this.buildValidator    = new BuildValidator();
        this.buildExecutor     = new BuildExecutor();
        this.cardPlayValidator = new CardPlayValidator();
        this.robberService     = new RobberService();

        this.currentPhase         = (phase != null) ? phase : GamePhase.RESOURCE_GATHERING;
        this.turnNumber           = turnNumber;
        this.winner               = null;
        this.lastDiceResult       = lastDiceResult;
        this.setupSettlementCount = setupSettlementCount;
        this.cardPlayedThisTurn   = null;
        this.cardBoughtThisTurn   = null;

        // Supply bangunan per pemain (restore dari player)
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

    // -------------------------------------------------------------------------
    // Accessors (read-only queries)
    // -------------------------------------------------------------------------

    public List<Player>    getPlayers()            { return Collections.unmodifiableList(players); }
    public Player          getActivePlayer()        { return turnManager.getActivePlayer(); }
    public PlayerSupply    getSupply(Player player) { return supplies.get(player); }
    public Board           getBoard()               { return board; }
    public Bank            getBank()                { return bank; }
    public CardDeck        getCardDeck()            { return cardDeck; }
    public Dice            getDice()                { return dice; }
    public Robber          getRobber()              { return robber; }
    public GameLog         getGameLog()             { return gameLog; }
    public TurnManager     getTurnManager()         { return turnManager; }
    public GamePhase       getCurrentPhase()        { return currentPhase; }
    public int             getTurnNumber()          { return turnNumber; }
    public DiceResult      getLastDiceResult()      { return lastDiceResult; }
    public Player          getWinner()              { return winner; }
    public int             getSetupSettlementCount(){ return setupSettlementCount; }
    public TradeManager    getTradeManager()        { return tradeManager; }

    /**
     * Mengembalikan {@link GameState} adapter singleton (lazy-init).
     * Digunakan oleh plugin dan kartu agar tidak perlu akses langsung ke {@link Game}.
     */
    public GameState getState() {
        if (stateAdapter == null) {
            stateAdapter = new GameStateAdapter(this);
        }
        return stateAdapter;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Memulai fase setup dan menentukan urutan giliran pertama via lemparan dadu.
     */
    public void startSetupPhase() {
        currentPhase = GamePhase.SETUP_FIRST_ROUND;
        setupSettlementCount = 0;
        turnManager.setOrder(TurnOrder.CLOCKWISE);
        gameLog.addEntry(LogEntry.EventType.SYSTEM,
            "Fase Setup dimulai. Pemain menentukan urutan dengan dadu.");

        int highestRoll = -1;
        int firstPlayerIndex = 0;
        for (int i = 0; i < players.size(); i++) {
            DiceResult roll = dice.roll();
            int total = roll.getTotal();
            gameLog.addEntry(LogEntry.EventType.SYSTEM, players.get(i).getName(),
                players.get(i).getName() + " melempar dadu: "
                + roll.getDie1() + " + " + roll.getDie2() + " = " + total);
            if (total > highestRoll) {
                highestRoll = total;
                firstPlayerIndex = i;
            }
        }
        turnManager.setActiveIndex(firstPlayerIndex);
        gameLog.addEntry(LogEntry.EventType.TURN_CHANGE,
            players.get(firstPlayerIndex).getName(),
            players.get(firstPlayerIndex).getName()
            + " memulai pertama (dadu tertinggi: " + highestRoll + ")");
    }

    /** Memulai permainan utama setelah fase setup selesai. */
    public void startMainGame() {
        assert currentPhase.isSetupPhase()
            : "startMainGame() hanya boleh dipanggil setelah setup selesai";
        currentPhase = GamePhase.RESOURCE_GATHERING;
        turnManager.setOrder(TurnOrder.CLOCKWISE);
        gameLog.addEntry(LogEntry.EventType.SYSTEM, "Permainan utama dimulai!");
    }

    // -------------------------------------------------------------------------
    // Setup actions
    // -------------------------------------------------------------------------

    /**
     * Menempatkan Pos Pantau awal pada fase setup.
     */
    public void placeInitialSettlement(Player player, Intersection intersection) {
        ValidationResult v = buildValidator.canPlaceInitialSettlement(
            player, intersection, board, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        buildExecutor.executeInitialSettlement(
            player, intersection, supplies, currentPhase, productionService, bank, board);
        setupSettlementCount++;

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
            player.getName() + " menempatkan Pos Pantau di intersection #" + intersection.getId());
    }

    /**
     * Menempatkan Pipa Transportasi awal pada fase setup.
     */
    public void placeInitialRoad(Player player, Path path) {
        ValidationResult v = buildValidator.canPlaceInitialRoad(player, path, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        buildExecutor.executeInitialRoad(player, path, supplies);

        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
            player.getName() + " menempatkan Pipa di path #" + path.getId());

        advanceSetupTurn();
    }

    // -------------------------------------------------------------------------
    // Turn actions
    // -------------------------------------------------------------------------

    /**
     * Melempar dadu untuk giliran ini.
     * Jika 7, aktifkan mekanisme Nimon Ungu.
     */
    public DiceResult rollDice() {
        if (currentPhase != GamePhase.RESOURCE_GATHERING) {
            throw new IllegalStateException(
                "Dadu hanya boleh dilempar pada fase RESOURCE_GATHERING");
        }

        lastDiceResult = dice.roll();
        int total = lastDiceResult.getTotal();

        gameLog.addEntry(LogEntry.EventType.SYSTEM, getActivePlayer().getName(),
            getActivePlayer().getName() + " melempar dadu: "
            + lastDiceResult.getDie1() + " + " + lastDiceResult.getDie2() + " = " + total);

        if (lastDiceResult.isSeven()) {
            gameLog.addEntry(LogEntry.EventType.ROBBER, getActivePlayer().getName(),
                "Dadu 7! Nimon Ungu aktif.");
        } else {
            productionService.distributeForRoll(total, board, players, bank);
        }

        currentPhase        = GamePhase.TRADE_BUILD;
        cardPlayedThisTurn  = null;
        cardBoughtThisTurn  = null;
        return lastDiceResult;
    }

    /**
     * Mengakhiri giliran pemain aktif dan memindahkan giliran ke berikutnya.
     */
    public void endTurn() {
        if (currentPhase == GamePhase.GAME_OVER) return;

        turnManager.stopTimer();
        tradeManager.reset();
        Player prev = getActivePlayer();
        turnManager.advanceTurn();
        currentPhase = GamePhase.RESOURCE_GATHERING;
        turnNumber++;

        gameLog.addEntry(LogEntry.EventType.TURN_CHANGE, prev.getName(),
            prev.getName() + " mengakhiri giliran. Sekarang giliran "
            + getActivePlayer().getName() + ".");
    }

    /**
     * Memulai TurnTimer untuk fase Trade/Build.
     * Dipanggil oleh UI (GameController) setelah rollDice() selesai.
     */
    public void startTradeBuildTimer(TurnTimer.OnTickCallback onTick) {
        if (currentPhase == GamePhase.TRADE_BUILD) {
            turnManager.startTimer(onTick);
        }
    }

    // -------------------------------------------------------------------------
    // Build actions
    // -------------------------------------------------------------------------

    /** Membangun Pipa Transportasi milik pemain di path yang ditentukan. */
    public void buildRoad(Player player, Path path) {
        ValidationResult v = buildValidator.canBuildRoad(
            player, path, board, supplies, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        buildExecutor.executeRoad(player, path, bank, supplies);
        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
            player.getName() + " membangun Pipa Transportasi di path #" + path.getId());

        updateLongestRoad();
    }

    /** Membangun Pos Pantau milik pemain di persimpangan yang ditentukan. */
    public void buildSettlement(Player player, Intersection intersection) {
        ValidationResult v = buildValidator.canBuildSettlement(
            player, intersection, board, supplies, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        buildExecutor.executeSettlement(player, intersection, bank, supplies);
        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
            player.getName() + " membangun Pos Pantau di intersection #" + intersection.getId());
    }

    /** Mengupgrade Pos Pantau menjadi Laboratorium. */
    public void buildCity(Player player, Intersection intersection) {
        ValidationResult v = buildValidator.canBuildCity(
            player, intersection, supplies, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        buildExecutor.executeCity(player, intersection, bank, supplies);
        gameLog.addEntry(LogEntry.EventType.BUILD, player.getName(),
            player.getName() + " meng-upgrade Pos Pantau ke Laboratorium di intersection #"
            + intersection.getId());
    }

    /** Membeli Kartu Temuan dari deck. */
    public void buyDevelopmentCard(Player player) {
        ValidationResult v = buildValidator.canBuyCard(player, cardDeck, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        cardBoughtThisTurn = buildExecutor.executeBuyCard(player, cardDeck, bank);
        gameLog.addEntry(LogEntry.EventType.CARD_BOUGHT, player.getName(),
            player.getName() + " membeli Kartu Temuan (" + cardBoughtThisTurn.getCardName() + ")");
    }

    // -------------------------------------------------------------------------
    // Card actions
    // -------------------------------------------------------------------------

    /**
     * Memainkan satu Kartu Temuan milik pemain aktif.
     */
    public void playCard(Player player, ExperimentCard card) {
        // Phase check first so callers always get IllegalStateException for wrong phase,
        // even if player/card are null.
        if (currentPhase != GamePhase.TRADE_BUILD) {
            throw new IllegalStateException(
                "Kartu hanya bisa dimainkan saat fase TRADE_BUILD (saat ini: " + currentPhase + ")");
        }
        ValidationResult v = cardPlayValidator.canPlay(
            player, card, cardBoughtThisTurn, cardPlayedThisTurn, currentPhase);
        if (!v.isValid()) throw new IllegalStateException(v.getReason());

        card.applyEffect(getState(), player);
        cardPlayedThisTurn = card;

        player.removeCard(card);
        cardDeck.addToDiscardPile(card);

        gameLog.addEntry(LogEntry.EventType.CARD_PLAYED, player.getName(),
            player.getName() + " memainkan " + card.getCardName());

        updateLargestArmy();
    }

    // -------------------------------------------------------------------------
    // Robber actions
    // -------------------------------------------------------------------------

    /**
     * Mengaktifkan mekanisme Nimon Ungu: pindah robber + curi resource (opsional).
     */
    public void activateRobber(HexTile targetTile, Player victim) {
        if (targetTile == null) {
            throw new IllegalArgumentException("Target tile tidak boleh null");
        }
        if (targetTile.equals(robber.getCurrentTile())) {
            throw new IllegalStateException("Nimon Ungu harus pindah ke petak berbeda");
        }

        robber.move(targetTile);
        gameLog.addEntry(LogEntry.EventType.ROBBER, getActivePlayer().getName(),
            getActivePlayer().getName() + " memindahkan Nimon Ungu ke tile #" + targetTile.getId());

        if (victim != null) {
            List<Player> eligible = robber.getEligibleVictims(getActivePlayer(), board);
            if (!eligible.contains(victim)) {
                throw new IllegalStateException(
                    victim.getName() + " tidak eligible untuk dicuri di posisi Nimon Ungu saat ini");
            }
            if (victim.getTotalResourceCount() == 0) {
                gameLog.addEntry(LogEntry.EventType.STEAL, getActivePlayer().getName(),
                    victim.getName() + " tidak punya resource untuk dicuri.");
            } else {
                ResourceType stolen = robber.stealRandomResource(getActivePlayer(), victim);
                gameLog.addEntry(LogEntry.EventType.STEAL, getActivePlayer().getName(),
                    getActivePlayer().getName() + " mencuri 1 " + stolen.getDisplayName()
                    + " dari " + victim.getName());
            }
        }

        currentPhase = GamePhase.TRADE_BUILD;
    }

    /**
     * Memproses pembuangan resource saat dadu 7 aktif.
     *
     * <p>Bot membuang otomatis via {@link RobberService}.
     * Human dicatat saja — UI memanggil {@link #discardResource} satu per satu.
     */
    public void processDiscardPhase() {
        Map<Player, Integer> discardMap = robber.activateDiscardPhase(players);
        if (discardMap.isEmpty()) return;

        Map<Player, Integer> botDiscarded = robberService.discardForBots(discardMap, bank);

        for (Map.Entry<Player, Integer> entry : discardMap.entrySet()) {
            Player player   = entry.getKey();
            int toDiscard   = entry.getValue();
            if (player.isBot()) {
                int actual = botDiscarded.getOrDefault(player, 0);
                gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
                    player.getName() + " (bot) membuang " + actual + " kartu resource.");
            } else {
                gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
                    player.getName() + " harus membuang " + toDiscard + " kartu resource.");
            }
        }
    }

    /**
     * Dipakai UI untuk memproses pembuangan resource satu per satu (human player).
     */
    public void discardResource(Player player, ResourceType type, int amount) {
        if (player == null || type == null) {
            throw new IllegalArgumentException("Player dan ResourceType tidak boleh null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Jumlah yang dibuang harus > 0");
        }
        if (!player.hasResource(type, amount)) {
            throw new IllegalStateException(
                player.getName() + " tidak punya " + amount + " " + type);
        }
        player.removeResource(type, amount);
        bank.returnResource(type, amount);
        gameLog.addEntry(LogEntry.EventType.DISCARD, player.getName(),
            player.getName() + " membuang " + amount + " " + type.getDisplayName());
    }

    // -------------------------------------------------------------------------
    // Trade actions
    // -------------------------------------------------------------------------

    /**
     * Pemain aktif membuat penawaran dagang ke pemain lain (atau broadcast ke semua).
     * Gunakan {@code offer.target = null} untuk broadcast.
     */
    public ValidationResult makeTradeOffer(TradeOffer offer) {
        ValidationResult result = tradeManager.makeOffer(offer, currentPhase);
        if (result.isValid()) {
            String targetName = offer.getTarget() != null
                ? offer.getTarget().getName() : "semua pemain";
            gameLog.addEntry(LogEntry.EventType.TRADE, offer.getOfferer().getName(),
                offer.getOfferer().getName() + " menawarkan dagang ke " + targetName);
        }
        return result;
    }

    /** Pemain {@code acceptingPlayer} menerima offer aktif. Resource ditransfer otomatis. */
    public ValidationResult acceptTradeOffer(Player acceptingPlayer) {
        ValidationResult result = tradeManager.acceptOffer(acceptingPlayer);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE, acceptingPlayer.getName(),
                acceptingPlayer.getName() + " menerima penawaran dagang.");
        }
        return result;
    }

    /** Pemain {@code rejectingPlayer} menolak offer aktif. */
    public ValidationResult rejectTradeOffer(Player rejectingPlayer) {
        ValidationResult result = tradeManager.rejectOffer(rejectingPlayer);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE, rejectingPlayer.getName(),
                rejectingPlayer.getName() + " menolak penawaran dagang.");
        }
        return result;
    }

    /** Target mengajukan counter-offer ke offerer. */
    public ValidationResult counterTradeOffer(TradeOffer counter) {
        ValidationResult result = tradeManager.counterOffer(counter, currentPhase);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE, counter.getOfferer().getName(),
                counter.getOfferer().getName() + " mengajukan counter-offer.");
        }
        return result;
    }

    /** Offerer membatalkan offer aktif. */
    public ValidationResult cancelTradeOffer(Player offerer) {
        ValidationResult result = tradeManager.cancelOffer(offerer);
        if (result.isValid()) {
            gameLog.addEntry(LogEntry.EventType.TRADE, offerer.getName(),
                offerer.getName() + " membatalkan penawaran dagang.");
        }
        return result;
    }

    /**
     * Pemain melakukan trade maritim dengan bank.
     * Rasio otomatis dihitung berdasarkan harbor terbaik pemain.
     */
    public ValidationResult tradeWithBank(Player player, ResourceType sellType,
                                          ResourceType buyType) {
        ValidationResult result = tradeManager.tradeWithBank(
            player, sellType, buyType, bank, board, currentPhase);
        if (result.isValid()) {
            int ratio = tradeManager.getBestTradeRatio(player, sellType, board);
            gameLog.addEntry(LogEntry.EventType.TRADE, player.getName(),
                player.getName() + " trade " + ratio + " " + sellType.getDisplayName()
                + " → 1 " + buyType.getDisplayName() + " (bank)");
        }
        return result;
    }

    /** Mengembalikan rasio trade terbaik pemain untuk resource tertentu. */
    public int getTradeRatio(Player player, ResourceType sellType) {
        return tradeManager.getBestTradeRatio(player, sellType, board);
    }

    /** Mengembalikan semua rasio trade pemain (satu per ResourceType). */
    public int[] getAllTradeRatios(Player player) {
        return tradeManager.getAllTradeRatios(player, board);
    }

    // -------------------------------------------------------------------------
    // Victory
    // -------------------------------------------------------------------------

    /**
     * Memeriksa apakah ada pemain yang mencapai {@value #VICTORY_POINTS_TO_WIN} PP.
     */
    public Player checkVictory() {
        if (currentPhase == GamePhase.GAME_OVER && winner != null) return winner;

        Player candidate = vpCalculator.findWinner(players, board, VICTORY_POINTS_TO_WIN);
        if (candidate != null) {
            winner       = candidate;
            currentPhase = GamePhase.GAME_OVER;
            turnManager.stopTimer();
            int totalVP = vpCalculator.getTotalVP(winner, board);
            gameLog.addEntry(LogEntry.EventType.VICTORY, winner.getName(),
                winner.getName() + " MENANG dengan " + totalVP + " Poin Prestasi!");
        }
        return winner;
    }

    /** Menghitung dan mengembalikan breakdown VP pemain tertentu. */
    public VictoryPointBreakdown getVPBreakdown(Player player) {
        return vpCalculator.calculate(player, board);
    }

    /** Menghitung VP semua pemain, diurutkan dari tertinggi ke terendah. */
    public java.util.List<VictoryPointBreakdown> getAllVPBreakdowns() {
        return vpCalculator.calculateAll(players, board);
    }

    /** Shortcut total VP pemain tanpa breakdown. */
    public int getVPTotal(Player player) {
        return vpCalculator.getTotalVP(player, board);
    }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    /** Menyimpan state permainan ke file. */
    public void saveGame(String filePath) {
        GameSaveManager.saveGame(this, filePath);
    }

    /** Memuat state permainan dari file yang sudah disimpan. */
    public static Game loadGame(String filePath) {
        return GameSaveManager.loadGame(filePath);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Memajukan giliran saat fase setup setelah satu pasang (Settlement + Road) ditempatkan.
     */
    private void advanceSetupTurn() {
        int playerCount = players.size();
        if (currentPhase == GamePhase.SETUP_FIRST_ROUND) {
            if (setupSettlementCount < playerCount) {
                turnManager.advanceTurnInDirection(TurnOrder.CLOCKWISE);
            } else {
                currentPhase = GamePhase.SETUP_SECOND_ROUND;
                turnManager.setOrder(TurnOrder.COUNTER_CLOCKWISE);
                gameLog.addEntry(LogEntry.EventType.SYSTEM,
                    "Putaran setup pertama selesai. Mulai putaran kedua (berlawanan arah).");
            }
        } else if (currentPhase == GamePhase.SETUP_SECOND_ROUND) {
            if (setupSettlementCount < playerCount * 2) {
                turnManager.advanceTurnInDirection(TurnOrder.COUNTER_CLOCKWISE);
            } else {
                gameLog.addEntry(LogEntry.EventType.SYSTEM, "Fase setup selesai!");
                startMainGame();
            }
        }
    }

    /** Mencari tile gurun di board untuk inisialisasi Robber. */
    private HexTile findDesertTile() {
        for (HexTile tile : board.getAllHexTiles()) {
            if (tile.getTerrainType() == TerrainType.DESERT) {
                return tile;
            }
        }
        return board.getAllHexTiles().get(0); // Fallback
    }

    /**
     * Memperbarui kepemilikan Jalan Terpanjang setelah road baru dipasang.
     * Dipanggil secara internal oleh {@link #buildRoad} dan {@link #placeInitialRoad}.
     */
    private void updateLongestRoad() {
        Player prev = null;
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

    /**
     * Memperbarui kepemilikan Pasukan Terbesar setelah Knight Card dimainkan.
     * Dipanggil secara internal oleh {@link #playCard}.
     */
    private void updateLargestArmy() {
        Player prev = null;
        for (Player p : players) {
            if (p.hasSpecialCard(SpecialCardType.LARGEST_ARMY)) { prev = p; break; }
        }

        Player newHolder = vpCalculator.updateLargestArmy(players);
        if (newHolder != null && !newHolder.equals(prev)) {
            if (prev != null) {
                gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD, prev.getName(),
                    prev.getName() + " kehilangan Pasukan Terbesar.");
            }
            gameLog.addEntry(LogEntry.EventType.SPECIAL_CARD, newHolder.getName(),
                newHolder.getName() + " mendapat Pasukan Terbesar ("
                + newHolder.getKnightsPlayed() + " knight)! (+2 PP)");
        }
    }

    @Override
    public String toString() {
        return String.format("Game[phase=%s, turn=%d, activePlayer=%s, players=%d]",
            currentPhase, turnNumber, getActivePlayer().getName(), players.size());
    }
}
