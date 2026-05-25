package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import java.util.List;

/**
 * Adapter yang membungkus Game menjadi GameState yang dapat diserahkan ke
 * plugin ExperimentCard, PlayerStrategy dan UI.
 *
 * Adapter Pattern (GoF Structural).
 *
 * Game adalah "Adaptee"; GameState adalah "Target interface". GameStateAdapter
 * adalah penghubungnya.
 *
 * Kelas ini dibuat secara eksklusif dari dalam Game#getState(). Tidak ada
 * konstruktor publik yang memungkinkan pembuatan adapter tanpa game.
 */

public class GameStateAdapter implements GameState {

    private final Game game;

    GameStateAdapter(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game reference cannot be null");
        }
        this.game = game;
    }

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
