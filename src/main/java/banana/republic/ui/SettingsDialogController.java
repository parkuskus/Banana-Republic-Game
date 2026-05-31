package banana.republic.ui;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class SettingsDialogController implements DialogController{

    private Runnable closeHandler;

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
        System.out.println("Menyimpan status permainan (Save State)...");
        // TODO: Panggil fungsi save game di sini, sambungin ke backend
    }

    @FXML
    private void handleBrowseFiles(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Card Plugin");
        FileChooser.ExtensionFilter extFilterJAR = new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar");
        fileChooser.getExtensionFilters().addAll(extFilterJAR);

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File fileYangDipilih = fileChooser.showOpenDialog(window);

        if (fileYangDipilih != null) {
            System.out.println("File dipilih: " + fileYangDipilih.getAbsolutePath());
        }
    }

    @FXML
    private void handleApplyChanges() {
        //TODO: sambungkan ke backend
        System.out.println("Menerapkan pengaturan...");
        closeDialog();
    }
}