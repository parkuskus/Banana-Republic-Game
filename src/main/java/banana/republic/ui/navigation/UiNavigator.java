package banana.republic.ui.navigation;

import java.io.IOException;

import banana.republic.core.Game;

public interface UiNavigator {
    void showMainMenu() throws IOException;
    void showLobby() throws IOException;
    void showGame(Game game) throws IOException;
    void showResult(Game game) throws IOException;
    void showTransition(Game game, Runnable startTurnHandler) throws IOException;
}
