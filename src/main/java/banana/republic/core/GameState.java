package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Path;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import java.util.List;

/**
 * Interface read-only dari state permainan yang diekspor ke plugin dan lapisan UI.
 *
 * <p>Interface ini adalah titik Dependency Inversion utama sistem:
 * <ul>
 *   <li>Plugin {@code ExperimentCard} menggunakan {@link GameState} untuk
 *       membaca/memodifikasi state saat {@code applyEffect()} dipanggil</li>
 *   <li>Plugin {@code PlayerStrategy} menggunakan untuk memutuskan aksi bot
 *       di setiap giliran</li>
 *   <li>Layer UI (M5) menggunakan {@link GameState} sebagai sumber data tampilan</li>
 * </ul>
 *
 * <p><strong>Note:</strong> Interface ini hanya boleh di-implement oleh
 * {@link GameStateAdapter}, jangan di-implement langsung oleh kelas lain.
 */
public interface GameState {

    List<Player> getAllPlayers();

    Player getActivePlayer();

    Board getBoard();

    default Player getCurrentPlayer() { return null; }

    default HexTile chooseKnightTarget(Player player,
                                       List<HexTile> candidates) {
        return null;
    }

    default Player chooseKnightVictim(Player player, HexTile target,
                                      List<Player> candidates) {
        return null;
    }

    default List<Path> chooseRoadBuildingPaths(Player player,
                                               List<Path> candidates,
                                               int maxPlacements) {
        return List.of();
    }

    Bank getBank();

    banana.republic.card.CardDeck getCardDeck();

    HexTile getRobberPosition();

    GamePhase getCurrentPhase();

    /**
     * Mengembalikan log kejadian permainan. Berguna untuk plugin yang ingin
     * membaca history aksi sebelumnya.
     */
    GameLog getGameLog();

    default void activateRobber(banana.republic.board.HexTile target, banana.republic.player.Player activePlayer, banana.republic.player.Player victim) {
        // Default: no-op for mock/plugin contexts
    }

    int getTurnNumber();
}
