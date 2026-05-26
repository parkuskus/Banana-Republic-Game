package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.building.PlayerSupply;
import banana.republic.building.PosPantau;
import banana.republic.building.Road;
import banana.republic.card.CardDeck;
import banana.republic.card.ExperimentCard;
import banana.republic.dice.Dice;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.plugin.MapGeneratorPlugin;
import banana.republic.resource.Bank;
import banana.republic.resource.BankImpl;
import banana.republic.resource.ResourceProductionService;
import banana.republic.robber.Robber;
import banana.republic.timer.TurnTimer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orkestrator utama permainan Banana Republic.
 *
 * Kelas ini adalah Facade (GoF Structural) dari seluruh sistem permainan. Semua
 * aksi pemain (build, trade, play card, end turn) masuk melalui kelas ini.
 * Tidak ada modul lain yang perlu tahu detail implementasi internal permainan,
 * interaksi lewat GameState (untuk plugin) atau method public kelas ini (untuk
 * UI layer M5).
 *
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
     * 0 = belum ada, 1 = sudah taruh pertama (tunggu road), 2 = putaran
     * selesai.
     */
    private int setupSettlementCount;

    private ExperimentCard cardPlayedThisTurn;
    private ExperimentCard cardBoughtThisTurn;

    /**
     * Membuat instance Game baru dengan daftar pemain dan generator peta.
     *
     * @param players daftar pemain (3–4 orang); tidak boleh {@code null} atau
     *     kosong
     * @param mapPlugin generator peta yang menghasilkan Board, jika {@code
     *     null}, digunakan {@link banana.republic.plugin.StandardMapGenerator}
     * secara default
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
        this.dice = new Dice();
        this.robber = new Robber();
        this.gameLog = new GameLog();
        this.productionService = new ResourceProductionService();
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

    /**
     * Mengembalikan GameState adapter singleton untuk diserahkan ke plugin dan
     * UI.
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
     * Memulai fase setup (inisiasi papan). Menentukan urutan giliran pertama
     * berdasarkan lemparan dadu.
     *
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
     *
     */
    public void startMainGame() {
        assert currentPhase.isSetupPhase()
            : "startMainGame() hanya boleh dipanggil setelah setup selesai";
        currentPhase = GamePhase.RESOURCE_GATHERING;
        turnManager.setOrder(TurnOrder.CLOCKWISE);
        gameLog.addEntry(LogEntry.EventType.SYSTEM, "Permainan utama dimulai!");
    }

    /**
     * Menempatkan Pos Pantau awal pada fase setup. Hanya bisa dilakukan saat
     * currentPhase.isSetupPhase()}
     *
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
     *
     * <p>
     * <em>Diimplementasikan di Fase 2.</em>
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
     * Melempar dadu untuk giliran ini (Fase 1: Resource Gathering). Jika
     * hasilnya 7, mengaktifkan mekanisme Nimon Ungu.
     *
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
     * Mengakhiri giliran pemain aktif dan memindahkan giliran ke pemain
     * berikutnya.
     *
     * <p>
     * <em>Diimplementasikan di Fase 2.</em>
     */
    public void endTurn() {
        if (currentPhase == GamePhase.GAME_OVER)
            return;

        turnManager.stopTimer();
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
     * Memulai TurnTimer untuk fase Trade/Build. Dipanggil oleh UI
     * (GameController) setelah rollDice() selesai.
     */
    public void startTradeBuildTimer(TurnTimer.OnTickCallback onTick) {
        if (currentPhase == GamePhase.TRADE_BUILD) {
            turnManager.startTimer(onTick);
        }
    }

    /**
     * Membangun Pipa Transportasi milik pemain di jalur yang ditentukan.
     *
     */
    public void buildRoad(Player player, banana.republic.board.Path path) {
        throw new UnsupportedOperationException(
            "buildRoad() — diimplementasikan di Fase 3");
    }

    /**
     * Membangun Pos Pantau milik pemain di persimpangan yang ditentukan.
     */
    public void
    buildSettlement(Player player,
                    banana.republic.board.Intersection intersection) {
        throw new UnsupportedOperationException(
            "buildSettlement() not implemented");
    }

    /**
     * Mengupgrade Pos Pantau menjadi Laboratorium.
     */
    public void buildCity(Player player,
                          banana.republic.board.Intersection intersection) {
        throw new UnsupportedOperationException("buildCity() not implemented");
    }

    /**
     * Membeli Kartu Temuan dari deck.
     */
    public void buyDevelopmentCard(Player player) {
        throw new UnsupportedOperationException(
            "buyDevelopmentCard() — diimplementasikan di Fase 3");
    }

    /**
     * Mengaktifkan mekanisme Nimon Ungu: memindahkan robber dan opsional
     * mencuri resource.
     *
     */
    public void activateRobber(banana.republic.board.HexTile targetTile,
                               Player victim) {
        throw new UnsupportedOperationException(
            "activateRobber() not implemented");
    }

    /**
     * Memproses pembuangan kartu dari semua pemain yang memiliki lebih dari
     * #HAND_LIMIT} kartu (efek dadu 7, langkah 1).
     *
     */
    public void processDiscardPhase() {
        throw new UnsupportedOperationException(
            "processDiscardPhase() not implemented");
    }

    /**
     * Memainkan satu Kartu Temuan milik pemain aktif.
     *
     */
    public void playCard(Player player,
                         banana.republic.card.ExperimentCard card) {
        throw new UnsupportedOperationException("playCard() not implemented");
    }

    /**
     * Memeriksa apakah ada pemain yang mencapai #VICTORY_POINTS_TO_WIN} PP dan
     * belum diproses sebagai pemenang.
     *
     */
    public Player checkVictory() {
        throw new UnsupportedOperationException(
            "checkVictory() — diimplementasikan di Fase 6");
    }

    /**
     * Menyimpan state permainan ke file.
     *
     */
    public void saveGame(String filePath) {
        throw new UnsupportedOperationException(
            "saveGame() — diimplementasikan di Fase 7");
    }

    /**
     * Memuat state permainan dari file yang sudah disimpan.
     */
    public static Game loadGame(String filePath) {
        throw new UnsupportedOperationException(
            "loadGame() — diimplementasikan di Fase 7");
    }

    /**
     * Memajukan giliran saat fase setup setelah satu pasang (Settlement + Road)
     * ditempatkan. Menangani transisi:
     *
     * Putaran 1 (CW): setiap pemain taruh 1 Settlement + 1 Road Transisi:
     * pemain terakhir langsung mulai putaran 2
     *
     * Putaran 2 (CCW): setiap pemain taruh 1 Settlement + 1 Road (urutan
     * terbalik) Setelah semua selesai: startMainGame()
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

    @Override
    public String toString() {
        return String.format(
            "Game[phase=%s, turn=%d, activePlayer=%s, players=%d]",
            currentPhase, turnNumber, getActivePlayer().getName(),
            players.size());
    }
}
