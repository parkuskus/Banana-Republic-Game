package banana.republic.core;

import java.util.List;

import banana.republic.board.Board;
import banana.republic.player.Player;
import banana.republic.resource.Bank;

/**
 * Adapter yang membungkus {@link Game} menjadi {@link GameState} yang dapat
 * diserahkan ke plugin ({@code ExperimentCard}, {@code PlayerStrategy}) dan UI.
 *
 * <p>Pola yang diimplementasikan: <strong>Adapter</strong> (GoF Structural).
 * {@code Game} adalah "Adaptee"; {@code GameState} adalah "Target interface".
 * {@code GameStateAdapter} adalah penghubungnya.
 *
 * <p>Kelas ini dibuat secara eksklusif dari dalam {@link Game#getState()}.
 * Tidak ada konstruktor publik yang memungkinkan pembuatan adapter tanpa game.
 */
public class GameStateAdapter implements GameState {

    private final Game game;

    /**
     * Konstruktor package-private — hanya boleh dipanggil dari {@link Game}.
     *
     * @param game referensi ke objek game yang diadaptasi; tidak boleh {@code null}
     */
    GameStateAdapter(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game reference cannot be null");
        }
        this.game = game;
    }

    // -------------------------------------------------------------------------
    // Implementasi GameState
    // -------------------------------------------------------------------------

    @Override
    public List<Player> getAllPlayers() {
        return game.getPlayers();
    }

    @Override
    public Player getActivePlayer() {
        return game.getActivePlayer();
    }

    @Override
    public Board getBoard() {
        return game.getBoard();
    }

    @Override
    public Bank getBank() {
        return game.getBank();
    }

    @Override
    public GamePhase getCurrentPhase() {
        return game.getCurrentPhase();
    }

    @Override
    public GameLog getGameLog() {
        return game.getGameLog();
    }

    @Override
    public int getTurnNumber() {
        return game.getTurnNumber();
    }
}

