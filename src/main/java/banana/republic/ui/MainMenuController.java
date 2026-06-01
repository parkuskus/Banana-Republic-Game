package banana.republic.ui;

import banana.republic.App;
import banana.republic.core.Game;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class MainMenuController {

    @FXML
    private void handleNewGame() throws IOException {
        App.setRoot("lobby");
    }

    @FXML
    private void handleLoadGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Saved Game");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        try {
            Game loadedGame = Game.loadGame(file.getAbsolutePath());
            if (loadedGame == null) {
                showError("Gagal memuat file save: file tidak valid.");
                return;
            }

            FXMLLoader loader = App.getLoader("game");
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initialize(loadedGame);
            App.setRootFromLoader(root);
        } catch (Exception e) {
            showError("Gagal memuat save: " + e.getMessage());
        }
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Keluar");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin keluar?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
