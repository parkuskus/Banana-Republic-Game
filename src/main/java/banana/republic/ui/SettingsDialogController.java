package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.ui.command.PluginUiService;
import banana.republic.ui.dialog.FileDialogService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.File;

public class SettingsDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;
    private final UiDialogs dialogs = new UiDialogs();
    private final UiNavigator navigator = new AppUiNavigator();
    private final FileDialogService fileDialogService = new FileDialogService();
    private final PluginUiService pluginUiService = new PluginUiService();

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
    private void handleSaveState(MouseEvent event) {
        if (game == null) {
            dialogs.showError("Game tidak tersedia untuk disimpan.");
            return;
        }
        Node ownerNode = event != null && event.getSource() instanceof Node node ? node : null;
        var file = fileDialogService.chooseSaveGame(ownerNode);
        if (file.isPresent()) {
            try {
                String path = file.get().getAbsolutePath();
                if (!path.endsWith(".json")) {
                    path += ".json";
                }
                game.saveGame(path);
                dialogs.showInfo("Permainan berhasil disimpan ke:\n" + path);
                closeDialog();
            } catch (Exception e) {
                dialogs.showError("Gagal menyimpan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadState() {
        var file = fileDialogService.chooseLoadGame(null);
        if (file.isPresent()) {
            try {
                Game loaded = Game.loadGame(file.get().getAbsolutePath());
                navigator.showGame(loaded);
                dialogs.showInfo("Load berhasil! Memasuki game yang dimuat.");
            } catch (Exception e) {
                dialogs.showError("Gagal memuat save: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBrowseFiles(MouseEvent event) {
        Node ownerNode = event != null && event.getSource() instanceof Node node ? node : null;
        var file = fileDialogService.chooseJarPlugin(ownerNode, "Load Card Plugin");

        if (file.isPresent()) {
            try {
                var results = pluginUiService.loadCardPlugin(game, file.get().getAbsolutePath());
                long successCount = results.stream().filter(banana.republic.plugin.PluginLoadResult::isSuccess).count();
                if (successCount > 0) {
                    dialogs.showInfo("Plugin kartu berhasil dimuat ke deck: " + successCount + " kartu.");
                } else {
                    dialogs.showError("Tidak ada kartu plugin valid di file tersebut.");
                }
            } catch (Exception e) {
                dialogs.showError("Gagal memuat plugin kartu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleApplyChanges() {
        closeDialog();
    }
}
