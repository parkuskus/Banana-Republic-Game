package banana.republic.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import banana.republic.board.Board;
import banana.republic.card.CardDeck;
import banana.republic.dice.Dice;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.plugin.MapGeneratorPlugin;
import banana.republic.resource.Bank;
import banana.republic.resource.BankImpl;
import banana.republic.robber.Robber;

/**
 * Orkestrator utama permainan Banana Republic.
 *
 * <p>Kelas ini adalah <strong>Facade</strong> (GoF Structural) dari seluruh sistem permainan.
 * Semua aksi pemain (build, trade, play card, end turn) masuk melalui kelas ini.
 * Tidak ada modul lain yang perlu tahu detail implementasi internal permainan —
 * mereka cukup berinteraksi melalui {@link GameState} (untuk plugin) atau
 * method public kelas ini (untuk UI layer M5).
 *
 * <h3>Alur penggunaan tipikal dari M5 (GameController):</h3>
 * <pre>{@code
 * Game game = new Game(players, mapPlugin);
 * game.startSetupPhase();
 * // ... setup selesai ...
 * game.startMainGame();
 * game.rollDice();
 * game.buildSettlement(activePlayer, intersection);
 * game.endTurn();
 * }</pre>
 *
 * <h3>Design Patterns:</h3>
 * <ul>
 *   <li><strong>Facade</strong> — entry point tunggal untuk semua aksi permainan</li>
 *   <li><strong>Adapter</strong> — {@link GameStateAdapter} menjembatani Game ke GameState</li>
 * </ul>
 */
public class Game {

    // =========================================================================
    // Konstanta permainan
    // =========================================================================

    /** Jumlah Poin Prestasi yang dibutuhkan untuk menang. */
    public static final int VICTORY_POINTS_TO_WIN = 10;

    /** Batas kartu di tangan sebelum kena penalti dadu 7. */
    public static final int HAND_LIMIT = 7;

    // =========================================================================
    // State utama permainan
    // =========================================================================

    private final List<Player>    players;
    private final Board           board;
    private final Bank            bank;
    private final CardDeck        cardDeck;
    private final Dice            dice;
    private final Robber          robber;
    private final GameLog         gameLog;
    private final TurnManager     turnManager;

    /** Adapter singleton — dibuat satu kali di {@link #getState()}. */
    private GameStateAdapter stateAdapter;

    /** Fase permainan saat ini. */
    private GamePhase currentPhase;

    /** Indeks pemain aktif dalam list {@link #players}. */
    private int activePlayerIndex;

    /** Nomor giliran permainan (bertambah setiap kali semua pemain selesai satu putaran). */
    private int turnNumber;

    /** Pemain yang memenangkan permainan, atau {@code null} jika belum ada pemenang. */
    private Player winner;

    /** Hasil lemparan dadu terakhir. */
    private DiceResult lastDiceResult;

    // =========================================================================
    // Konstruktor
    // =========================================================================

    /**
     * Membuat instance Game baru dengan daftar pemain dan generator peta.
     *
     * @param players   daftar pemain (3–4 orang); tidak boleh {@code null} atau kosong
     * @param mapPlugin generator peta yang menghasilkan {@link Board}; jika {@code null},
     *                  digunakan {@link banana.republic.plugin.StandardMapGenerator} secara default
     */
    public Game(List<Player> players, MapGeneratorPlugin mapPlugin) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }
        if (players.size() < 3 || players.size() > 4) {
            throw new IllegalArgumentException("Game requires 3 or 4 players, got: " + players.size());
        }

        this.players           = new ArrayList<>(players);
        this.bank              = new BankImpl();
        this.cardDeck          = new CardDeck();
        this.dice              = new Dice();
        this.robber            = new Robber();
        this.gameLog           = new GameLog();
        this.turnManager       = new TurnManager();
        this.currentPhase      = GamePhase.SETUP_FIRST_ROUND;
        this.activePlayerIndex = 0;
        this.turnNumber        = 1;
        this.winner            = null;
        this.lastDiceResult    = null;

        // Generate board — fallback ke StandardMapGenerator jika tidak ada plugin
        MapGeneratorPlugin generator = (mapPlugin != null)
                ? mapPlugin
                : new banana.republic.plugin.StandardMapGenerator();
        this.board = generator.generateBoard();

        gameLog.addEntry(LogEntry.EventType.SYSTEM,
                "Permainan Banana Republic dimulai dengan " + players.size() + " pemain.");
    }

    // =========================================================================
    // Getters — digunakan oleh GameStateAdapter dan UI layer
    // =========================================================================

    /** Mengembalikan daftar pemain (unmodifiable). */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** Mengembalikan pemain yang sedang aktif (giliran sekarang). */
    public Player getActivePlayer() {
        return players.get(activePlayerIndex);
    }

    /** Mengembalikan papan permainan. */
    public Board getBoard() {
        return board;
    }

    /** Mengembalikan bank sumber daya. */
    public Bank getBank() {
        return bank;
    }

    /** Mengembalikan deck kartu temuan. */
    public CardDeck getCardDeck() {
        return cardDeck;
    }

    /** Mengembalikan objek dadu. */
    public Dice getDice() {
        return dice;
    }

    /** Mengembalikan objek Nimon Ungu (Robber). */
    public Robber getRobber() {
        return robber;
    }

    /** Mengembalikan log kejadian permainan. */
    public GameLog getGameLog() {
        return gameLog;
    }

    /** Mengembalikan turn manager. */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    /** Mengembalikan fase permainan saat ini. */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /** Mengembalikan nomor giliran saat ini (dimulai dari 1). */
    public int getTurnNumber() {
        return turnNumber;
    }

    /** Mengembalikan hasil lemparan dadu terakhir, atau {@code null} jika belum ada. */
    public DiceResult getLastDiceResult() {
        return lastDiceResult;
    }

    /**
     * Mengembalikan pemenang permainan, atau {@code null} jika belum ada pemenang.
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Mengembalikan {@link GameState} adapter singleton untuk diserahkan ke plugin dan UI.
     *
     * <p>Lazy-init: adapter dibuat saat pertama kali dipanggil, lalu di-cache.
     */
    public GameState getState() {
        if (stateAdapter == null) {
            stateAdapter = new GameStateAdapter(this);
        }
        return stateAdapter;
    }

    // =========================================================================
    // Lifecycle — diimplementasikan di Fase 2
    // =========================================================================

    /**
     * Memulai fase setup (inisiasi papan).
     * Menentukan urutan giliran pertama berdasarkan lemparan dadu.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public void startSetupPhase() {
        throw new UnsupportedOperationException("startSetupPhase() — diimplementasikan di Fase 2");
    }

    /**
     * Memulai permainan utama setelah fase setup selesai.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public void startMainGame() {
        throw new UnsupportedOperationException("startMainGame() — diimplementasikan di Fase 2");
    }

    // =========================================================================
    // Aksi fase setup — diimplementasikan di Fase 2
    // =========================================================================

    /**
     * Menempatkan Pos Pantau awal pada fase setup.
     * Hanya bisa dilakukan saat {@code currentPhase.isSetupPhase()}.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public void placeInitialSettlement(Player player, banana.republic.board.Intersection intersection) {
        throw new UnsupportedOperationException("placeInitialSettlement() — diimplementasikan di Fase 2");
    }

    /**
     * Menempatkan Pipa Transportasi awal pada fase setup.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public void placeInitialRoad(Player player, banana.republic.board.Path path) {
        throw new UnsupportedOperationException("placeInitialRoad() — diimplementasikan di Fase 2");
    }

    // =========================================================================
    // Aksi giliran normal — diimplementasikan di Fase 2 & 3
    // =========================================================================

    /**
     * Melempar dadu untuk giliran ini (Fase 1: Resource Gathering).
     * Jika hasilnya 7, mengaktifkan mekanisme Nimon Ungu.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public DiceResult rollDice() {
        throw new UnsupportedOperationException("rollDice() — diimplementasikan di Fase 2");
    }

    /**
     * Mengakhiri giliran pemain aktif dan memindahkan giliran ke pemain berikutnya.
     *
     * <p><em>Diimplementasikan di Fase 2.</em>
     */
    public void endTurn() {
        throw new UnsupportedOperationException("endTurn() — diimplementasikan di Fase 2");
    }

    // =========================================================================
    // Aksi build — diimplementasikan di Fase 3
    // =========================================================================

    /**
     * Membangun Pipa Transportasi milik pemain di jalur yang ditentukan.
     *
     * <p><em>Diimplementasikan di Fase 3.</em>
     */
    public void buildRoad(Player player, banana.republic.board.Path path) {
        throw new UnsupportedOperationException("buildRoad() — diimplementasikan di Fase 3");
    }

    /**
     * Membangun Pos Pantau milik pemain di persimpangan yang ditentukan.
     *
     * <p><em>Diimplementasikan di Fase 3.</em>
     */
    public void buildSettlement(Player player, banana.republic.board.Intersection intersection) {
        throw new UnsupportedOperationException("buildSettlement() — diimplementasikan di Fase 3");
    }

    /**
     * Mengupgrade Pos Pantau menjadi Laboratorium.
     *
     * <p><em>Diimplementasikan di Fase 3.</em>
     */
    public void buildCity(Player player, banana.republic.board.Intersection intersection) {
        throw new UnsupportedOperationException("buildCity() — diimplementasikan di Fase 3");
    }

    /**
     * Membeli Kartu Temuan dari deck.
     *
     * <p><em>Diimplementasikan di Fase 3.</em>
     */
    public void buyDevelopmentCard(Player player) {
        throw new UnsupportedOperationException("buyDevelopmentCard() — diimplementasikan di Fase 3");
    }

    // =========================================================================
    // Aksi Nimon Ungu & kartu — diimplementasikan di Fase 4
    // =========================================================================

    /**
     * Mengaktifkan mekanisme Nimon Ungu: memindahkan robber dan opsional mencuri resource.
     *
     * <p><em>Diimplementasikan di Fase 4.</em>
     */
    public void activateRobber(banana.republic.board.HexTile targetTile, Player victim) {
        throw new UnsupportedOperationException("activateRobber() — diimplementasikan di Fase 4");
    }

    /**
     * Memproses pembuangan kartu dari semua pemain yang memiliki lebih dari {@link #HAND_LIMIT} kartu
     * (efek dadu 7, langkah 1).
     *
     * <p><em>Diimplementasikan di Fase 4.</em>
     */
    public void processDiscardPhase() {
        throw new UnsupportedOperationException("processDiscardPhase() — diimplementasikan di Fase 4");
    }

    /**
     * Memainkan satu Kartu Temuan milik pemain aktif.
     *
     * <p><em>Diimplementasikan di Fase 4.</em>
     */
    public void playCard(Player player, banana.republic.card.ExperimentCard card) {
        throw new UnsupportedOperationException("playCard() — diimplementasikan di Fase 4");
    }

    // =========================================================================
    // Victory check — diimplementasikan di Fase 6
    // =========================================================================

    /**
     * Memeriksa apakah ada pemain yang mencapai {@link #VICTORY_POINTS_TO_WIN} PP
     * dan belum diproses sebagai pemenang.
     *
     * @return pemenang jika ditemukan, atau {@code null} jika belum ada
     * <p><em>Diimplementasikan di Fase 6.</em>
     */
    public Player checkVictory() {
        throw new UnsupportedOperationException("checkVictory() — diimplementasikan di Fase 6");
    }

    // =========================================================================
    // Save / Load — diimplementasikan di Fase 7
    // =========================================================================

    /**
     * Menyimpan state permainan ke file.
     *
     * <p><em>Diimplementasikan di Fase 7.</em>
     */
    public void saveGame(String filePath) {
        throw new UnsupportedOperationException("saveGame() — diimplementasikan di Fase 7");
    }

    /**
     * Memuat state permainan dari file yang sudah disimpan.
     *
     * <p><em>Diimplementasikan di Fase 7.</em>
     */
    public static Game loadGame(String filePath) {
        throw new UnsupportedOperationException("loadGame() — diimplementasikan di Fase 7");
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Override
    public String toString() {
        return String.format(
            "Game[phase=%s, turn=%d, activePlayer=%s, players=%d]",
            currentPhase,
            turnNumber,
            getActivePlayer().getName(),
            players.size()
        );
    }
}

