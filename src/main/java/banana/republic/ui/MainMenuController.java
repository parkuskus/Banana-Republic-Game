package banana.republic.ui;

import banana.republic.App;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

/**
 * Main menu controller.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class MainMenuController {
    @FXML
    private void handleNewGame() throws IOException {
        // Memanggil metode global di App.java untuk pindah ke lobby.fxml
        App.setRoot("lobby");
    }
    @FXML
    private void handleExit() throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Keluar");
        alert.setHeaderText(null);
        alert.setContentText("Apakah Anda yakin ingin keluar?");

        Optional result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
            System.exit(0);
        }
    }
}
