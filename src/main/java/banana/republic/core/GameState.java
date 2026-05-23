package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import java.util.List;

/**
 * Interface read-only dari state permainan yang diekspor ke plugin dan lapisan
 * UI. Interface ini adalah titik Dependency Inversion utama sistem:
 *
 * Plugin ExperimentCard menggunakan GameState membaca/memodifikasi state saat
 * applyEffect() dipanggil
 *
 * Plugin PlayerStrategy menggunakan untuk memutuskan aksi bot di setiap
 * giliran.
 *
 * Layer UI (M5) menggunakan GameState sebagai sumber data tampilan.
 *
 * Note: Interface ini hanya boleh di-implement oleh GameStateAdapter, jangan
 * di-implement langsung oleh kelas lain.
 */
public interface GameState {

    List<Player> getAllPlayers();

    Player getActivePlayer();

    Board getBoard();

    Bank getBank();

    GamePhase getCurrentPhase();

    /**
     * Mengembalikan log kejadian permainan. Berguna untuk plugin yang ingin
     * membaca history aksi sebelumnya.
     */
    GameLog getGameLog();

    int getTurnNumber();
}
