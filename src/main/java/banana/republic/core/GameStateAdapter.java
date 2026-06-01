package banana.republic.core;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Path;
import banana.republic.card.CardDeck;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter yang membungkus Game menjadi GameState yang dapat diserahkan ke
 * plugin ExperimentCard, PlayerStrategy dan UI.
 *
 * <p>Adapter Pattern (GoF Structural).
 *
 * <p>Game adalah "Adaptee"; GameState adalah "Target interface". GameStateAdapter
 * adalah penghubungnya.
 *
 * <p>Kelas ini dibuat secara eksklusif dari dalam Game#getState(). Tidak ada
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
    public Player getCurrentPlayer() {
        Player active = game.getActivePlayer();
        if (active == null) {
            throw new IllegalStateException("Tidak ada pemain aktif saat ini");
        }
        return active;
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
    public CardDeck getCardDeck() {
        return game.getCardDeck();
    }

    @Override
    public GamePhase getCurrentPhase() {
        return game.getCurrentPhase();
    }

    @Override
    public HexTile getRobberPosition() {
        return game.getBoard().getRobberTile()
            .orElseThrow(() -> new IllegalStateException(
                "Tidak ada tile yang menampung Nimon Ungu di board"));
    }

    @Override
    public GameLog getGameLog() {
        return game.getGameLog();
    }

    @Override
    public int getTurnNumber() {
        return game.getTurnNumber();
    }

    // ============================================================
    // Plugin-facing selection methods (default selection strategy)
    // ============================================================

    /**
     * Pilih target tile untuk Knight Card.
     * Strategi: pilih tile pertama yang bukan posisi robber saat ini.
     */
    @Override
    public HexTile chooseKnightTarget(Player player, List<HexTile> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("Daftar tile target tidak boleh kosong");
        }
        HexTile current = getRobberPosition();
        for (HexTile tile : candidates) {
            if (!tile.equals(current)) {
                return tile;
            }
        }
        throw new IllegalStateException(
            "Tidak ada tile kandidat yang berbeda dari posisi robber saat ini");
    }

    /**
     * Pilih korban pencurian dari daftar kandidat.
     * Strategi: pilih korban dengan jumlah sumber daya terbanyak.
     * Boleh null jika tidak ada kandidat (tidak wajib mencuri).
     */
    @Override
    public Player chooseKnightVictim(Player player, HexTile target,
                                     List<Player> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        Player bestVictim = null;
        int maxResources = -1;
        for (Player candidate : candidates) {
            int count = candidate.getTotalResourceCount();
            if (count > maxResources) {
                maxResources = count;
                bestVictim = candidate;
            }
        }
        return bestVictim;
    }

    /**
     * Pilih path untuk Road Building Card.
     * Strategi: pilih path kosong pertama yang valid hingga maxPlacements.
     */
    @Override
    public List<Path> chooseRoadBuildingPaths(Player player,
                                                List<Path> candidates,
                                                int maxPlacements) {
        if (candidates == null || candidates.isEmpty() || maxPlacements <= 0) {
            return List.of();
        }
        List<Path> chosen = new ArrayList<>();
        for (Path path : candidates) {
            if (chosen.size() >= maxPlacements) {
                break;
            }
            if (path.isEmpty()) {
                chosen.add(path);
            }
        }
        return chosen;
    }
}
