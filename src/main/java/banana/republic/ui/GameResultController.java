package banana.republic.ui;

import banana.republic.App;
import javafx.fxml.FXML;

import java.io.IOException;

public class GameResultController {

    private Runnable viewBoardHandler;
    private Runnable mainMenuHandler;

    public void setViewBoardHandler(Runnable viewBoardHandler) {
        this.viewBoardHandler = viewBoardHandler;
    }

    public void setMainMenuHandler(Runnable mainMenuHandler) {
        this.mainMenuHandler = mainMenuHandler;
    }

    @FXML
    private void handleViewBoard() throws IOException{
        App.setRoot("game");
    }

    @FXML
    private void handleMainMenu() throws IOException {
        App.setRoot("main");
    }
}