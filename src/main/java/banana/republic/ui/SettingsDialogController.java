package banana.republic.ui;

import banana.republic.App;
import banana.republic.core.Game;
import banana.republic.plugin.PluginLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

public class SettingsDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;
    private final PluginLoader pluginLoader = new PluginLoader();

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @FXML
    private void handleSaveState() {
        if (game == null) {
            showError("Game tidak tersedia untuk disimpan.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                String path = file.getAbsolutePath();
                if (!path.endsWith(".json")) {
                    path += ".json";
                }
                game.saveGame(path);
                showInfo("Permainan berhasil disimpan ke:\n" + path);
                closeDialog();
            } catch (Exception e) {
                showError("Gagal menyimpan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadState() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                Game loaded = Game.loadGame(file.getAbsolutePath());
                FXMLLoader gameLoader = App.getLoader("game");
                Parent gameRoot = gameLoader.load();
                GameController gameCtrl = gameLoader.getController();
                gameCtrl.setGame(loaded);
                App.setRootFromLoader(gameRoot);
                showInfo("Load berhasil! Memasuki game yang dimuat.");
            } catch (IOException e) {
                showError("Gagal memuat UI game: " + e.getMessage());
            } catch (Exception e) {
                showError("Gagal memuat save: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBrowseFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Card Plugin");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar")
        );

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try {
                var card = pluginLoader.loadExperimentCard(file.getAbsolutePath());
                if (game != null) {
                    game.getCardDeck().addCard(card);
                    showInfo("Plugin kartu berhasil dimuat dan ditambahkan ke deck:\n" + card.getCardName());
                } else {
                    showInfo("Plugin kartu berhasil dimuat (belum ada game aktif):\n" + card.getCardName());
                }
            } catch (Exception e) {
                showError("Gagal memuat plugin kartu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleApplyChanges() {
        closeDialog();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
