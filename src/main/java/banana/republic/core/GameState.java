package banana.republic.core;

import java.util.List;

import banana.republic.board.Board;
import banana.republic.player.Player;
import banana.republic.resource.Bank;


public interface GameState {
    List<Player> getAllPlayers();
    Bank getBank();
    Board getBoard();
}
