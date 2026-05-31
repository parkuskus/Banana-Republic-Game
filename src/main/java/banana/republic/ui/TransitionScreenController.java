package banana.republic.ui;

import banana.republic.App;
import javafx.fxml.FXML;

import java.io.IOException;

public class TransitionScreenController {

    private Runnable startTurnHandler;

    public void setStartTurnHandler(Runnable startTurnHandler) {
        this.startTurnHandler = startTurnHandler;
    }

    @FXML
    private void handleStartTurn() throws IOException {
        App.setRoot("game");
    }
}