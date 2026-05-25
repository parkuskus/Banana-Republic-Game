package banana.republic.ui;

import banana.republic.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;

/**
 * Main menu controller.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class LobbyController {

    @FXML
    private ComboBox<String> numPlayersBox;
    @FXML
    private HBox player1Box;
    @FXML
    private HBox player2Box;
    @FXML
    private HBox player3Box;
    @FXML
    private HBox player4Box;

    @FXML
    public void initialize() {
        numPlayersBox.getItems().addAll("2 Players", "3 Players", "4 Players");
        // 2. Pasang Listener untuk mendeteksi perubahan secara real-time
        numPlayersBox.valueProperty().addListener((observable, nilaiLama, nilaiBaru) -> {
            if (nilaiBaru != null) {
                // Mengambil angka depan dari teks (Misal: "3 Players" diubah jadi angka 3)
                int jumlahPemain = Integer.parseInt(nilaiBaru.split(" ")[0]);
                changePlayerBox(jumlahPemain);
            }
        });
        numPlayersBox.setValue("2 Players");
    }

    // Fungsi khusus untuk mengatur visibilitas baris HBox
    private void changePlayerBox(int jumlah) {
        player1Box.setVisible(true);
        player1Box.setManaged(true);

        player2Box.setVisible(true);
        player2Box.setManaged(true);

        boolean showP3 = (jumlah >= 3);
        player3Box.setVisible(showP3);
        player3Box.setManaged(showP3);

        boolean showP4 = (jumlah >= 4);
        player4Box.setVisible(showP4);
        player4Box.setManaged(showP4);
    }

    @FXML
    private void handleLoadMap(MouseEvent event) {
        // Membuat objek FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Map Plugin");

        // mengatur format file input
        FileChooser.ExtensionFilter extFilterJAR = new FileChooser.ExtensionFilter(".jar files (*.jar)", "*.json");

        // Memasukkan filter ke dalam FileChooser
        fileChooser.getExtensionFilters().addAll(extFilterJAR);

        // Bikin popup window
        Window window = ((Node) event.getSource()).getScene().getWindow();

        // munculkan dialog box ketika file baru dipilih
        File fileYangDipilih = fileChooser.showOpenDialog(window);

        // Mengecek apakah pemain benar-benar memilih file atau batal
        if (fileYangDipilih != null) {
            System.out.println("File dipilih: " + fileYangDipilih.getAbsolutePath());

            // TODO: load game

        } else {
            System.out.println("Pemain membatalkan pemilihan file.");
        }
    }


    @FXML
    private void exit() throws IOException {
        // Memanggil metode global di App.java untuk pindah ke lobby.fxml
        App.setRoot("main");
    }
}
