package banana.republic.ui.dialog;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class FileDialogService {

    private static final DateTimeFormatter SAVE_FILE_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    public Optional<File> chooseSaveGame(Node ownerNode) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Game");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );
        chooser.setInitialFileName("banana-republic-" +
            SAVE_FILE_FORMAT.format(LocalDateTime.now()) + ".json");
        setInitialDirectory(chooser, new File("saves"));
        return Optional.ofNullable(chooser.showSaveDialog(ownerWindow(ownerNode)));
    }

    public Optional<File> chooseLoadGame(Node ownerNode) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Game");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );
        setInitialDirectory(chooser, new File("saves"));
        return Optional.ofNullable(chooser.showOpenDialog(ownerWindow(ownerNode)));
    }

    public Optional<File> chooseJarPlugin(Node ownerNode, String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar")
        );
        return Optional.ofNullable(chooser.showOpenDialog(ownerWindow(ownerNode)));
    }

    private void setInitialDirectory(FileChooser chooser, File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (directory.isDirectory()) {
            chooser.setInitialDirectory(directory);
        }
    }

    private Window ownerWindow(Node node) {
        return node != null && node.getScene() != null ? node.getScene().getWindow() : null;
    }
}
