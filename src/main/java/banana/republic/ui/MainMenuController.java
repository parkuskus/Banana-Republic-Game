package banana.republic.ui;

import banana.republic.App;
import banana.republic.core.Game;
import banana.republic.ui.dialog.FileDialogService;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.IOException;

public class MainMenuController {

    private final UiDialogs dialogs = new UiDialogs();
    private final UiNavigator navigator = new AppUiNavigator();
    private final FileDialogService fileDialogService = new FileDialogService();

    @FXML
    private void handleNewGame() throws IOException {
        navigator.showLobby();
    }

    @FXML
    private void handleLoadGame() {
        var file = fileDialogService.chooseLoadGame(null);
        if (file.isEmpty()) return;

        try {
            Game loadedGame = Game.loadGame(file.get().getAbsolutePath());
            if (loadedGame == null) {
                dialogs.showError("Gagal memuat file save: file tidak valid.");
                return;
            }

            navigator.showGame(loadedGame);
        } catch (Exception e) {
            dialogs.showError("Gagal memuat save: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        if (dialogs.confirm("Konfirmasi Keluar", null, "Apakah Anda yakin ingin keluar?")) {
            Platform.exit();
            System.exit(0);
        }
    }
}
