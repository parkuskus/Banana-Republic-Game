package banana.republic.ui.navigation;

import java.io.IOException;

import banana.republic.App;
import banana.republic.core.Game;
import banana.republic.ui.GameController;
import banana.republic.ui.GameResultController;
import banana.republic.ui.TransitionScreenController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * JavaFX/App-backed screen navigation implementation.
 */
public class AppUiNavigator implements UiNavigator {

    @Override
    public void showMainMenu() throws IOException {
        App.setRoot("main");
    }

    @Override
    public void showLobby() throws IOException {
        App.setRoot("lobby");
    }

    @Override
    public void showGame(Game game) throws IOException {
        FXMLLoader loader = App.getLoader("game");
        Parent root = loader.load();
        GameController controller = loader.getController();
        controller.setGame(game);
        App.setRootFromLoader(root);
    }

    @Override
    public void showResult(Game game) throws IOException {
        FXMLLoader loader = App.getLoader("result");
        Parent root = loader.load();
        GameResultController controller = loader.getController();
        controller.setGame(game);
        App.setRootFromLoader(root);
    }

    @Override
    public void showTransition(Game game, Runnable startTurnHandler) throws IOException {
        FXMLLoader loader = App.getLoader("transition");
        Parent root = loader.load();
        TransitionScreenController controller = loader.getController();
        controller.setGame(game);
        controller.setStartTurnHandler(startTurnHandler);
        App.setRootFromLoader(root);
    }
}
