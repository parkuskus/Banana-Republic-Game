package banana.republic.core;

import java.util.List;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.board.Path;
import banana.republic.card.CardDeck;
import banana.republic.player.Player;
import banana.republic.resource.Bank;


public interface GameState {

    List<Player> getAllPlayers();

    Player getActivePlayer();

    Board getBoard();


    Player getCurrentPlayer();

    Bank getBank();

    CardDeck getCardDeck();

    GamePhase getCurrentPhase();


    HexTile getRobberPosition();


    GameLog getGameLog();

    int getTurnNumber();

    HexTile chooseKnightTarget(Player player, List<HexTile> candidates);

    
    Player chooseKnightVictim(Player player, HexTile target, List<Player> candidates);

    List<Path> chooseRoadBuildingPaths(Player player, List<Path> candidates,
                                       int maxPlacements);
}
