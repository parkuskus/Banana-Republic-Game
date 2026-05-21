package banana.republic.resource;

import java.util.List;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.player.Player;


public class ResourceProductionService {

    /**
     * Distributes resources for a single tile to affected players.
     * This is a helper for per-tile distribution.
     *
     * Precondition: caller should verify the tile is not blocked by Nimon Ungu.
     */
    public void distribute(HexTile tile, List<Player> players, Bank bank) {
        // Placeholder: actual implementation requires Board data (adjacency) from Module 1
        // For now, this method serves as a contract.
    }

    /**
     * Distributes resources for a given dice roll across the entire board.
     * This should be called by the game engine after dice are rolled.
     *
     * Edge cases:
     * - Roll is 7: no resources are produced (Nimon Ungu activation).
     * - Multiple players on same tile: handled per finite bank rules.
     */
    public void distributeForRoll(int roll, Board board, List<Player> players, Bank bank) {
        // Placeholder: actual implementation requires Board.getHexTilesByRoll(roll) from Module 1
        // For now, this method serves as a contract.
    }


    public boolean canDistribute(HexTile tile, List<Player> affected, Bank bank) {
        if (affected == null || affected.isEmpty()) {
            return false;
        }
        // Placeholder: actual logic needs resource type mapping from HexTile (Module 1)
        return true;
    }

    /**
     * Distributes initial resources after the second Pos Pantau placement.
     * Each player gets 1 resource card for each terrain hex adjacent to their second Pos Pantau.
     *
     * Edge cases:
     * - Bank shortage: same finite rules apply.
     * - Desert/Gurun tile: produces no resources.
     */
    public void distributeInitialResources(Player player, Intersection secondPosPantau,
                                             Bank bank, Board board) {
        // Placeholder: requires Board.getAdjacentHexTiles(intersection) from Module 1
        // For now, this method serves as a contract.
    }
}
