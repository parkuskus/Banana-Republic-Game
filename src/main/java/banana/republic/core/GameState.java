package banana.republic.core;

import java.util.List;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Path;
import banana.republic.player.Player;
import banana.republic.resource.Bank;


public interface GameState {
    List<Player> getAllPlayers();
    Bank getBank();
    Board getBoard();

    default Player getCurrentPlayer() {
        return null;
    }

    default HexTile chooseKnightTarget(Player player, List<HexTile> candidates) {
        return null;
    }

    default Player chooseKnightVictim(Player player, HexTile target, List<Player> candidates) {
        return null;
    }

    default List<Path> chooseRoadBuildingPaths(Player player, List<Path> candidates, int maxPlacements) {
        return List.of();
    }

}
