package banana.republic.player;

import java.util.List;

import banana.republic.board.HexTile;
import banana.republic.core.GameState;


public interface PlayerStrategy {

 
    List<Action> takeTurn(GameState state);


    Player chooseRobberTarget(GameState state, List<Player> candidates);


    HexTile chooseRobberPlacement(GameState state);
}
