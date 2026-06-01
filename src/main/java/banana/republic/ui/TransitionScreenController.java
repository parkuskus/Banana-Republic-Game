package banana.republic.ui;

import banana.republic.core.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TransitionScreenController {

    private Runnable startTurnHandler;
    private Game game;

    @FXML
    private Label turnHeadingLabel;

    public void setGame(Game game) {
        this.game = game;
        if (game != null && game.getActivePlayer() != null) {
            String playerName = game.getActivePlayer().getName();
            turnHeadingLabel.setText("Waiting for " + playerName + "...");
        }
    }

    public void setStartTurnHandler(Runnable startTurnHandler) {
        this.startTurnHandler = startTurnHandler;
    }

    @FXML
    private void handleStartTurn() {
        if (startTurnHandler != null) {
            startTurnHandler.run();
        }
    }
}
