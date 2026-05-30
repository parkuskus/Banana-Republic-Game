package banana.republic.ui;

import banana.republic.App;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.io.File;
import java.io.IOException;

/**
 * Main menu controller.
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

    private Paint[] selectedColors = new Paint[4];

    @FXML
    public void initialize() {
        numPlayersBox.getItems().addAll("3 Players", "4 Players");
        numPlayersBox.valueProperty().addListener((observable, nilaiLama, nilaiBaru) -> {
            if (nilaiBaru != null) {
                int jumlahPemain = Integer.parseInt(nilaiBaru.split(" ")[0]);
                changePlayerBox(jumlahPemain);
            }
        });
        numPlayersBox.setValue("3 Players");

        setupColorPickers();
    }

    private void changePlayerBox(int jumlah) {
        player1Box.setVisible(true); player1Box.setManaged(true);
        player2Box.setVisible(true); player2Box.setManaged(true);

        boolean showP4 = (jumlah >= 4);
        player4Box.setVisible(showP4); player4Box.setManaged(showP4);

        // Reset warna jika pemain dikurangi
        for (int i = jumlah; i < 4; i++) {
            selectedColors[i] = null;
        }
        updateColorVisuals();
    }

    private void setupColorPickers() {
        HBox[] playerBoxes = {player1Box, player2Box, player3Box, player4Box};

        for (int i = 0; i < 4; i++) {
            final int playerIndex = i;
            if (playerBoxes[i] == null) continue;

            for (Node child : playerBoxes[i].getChildren()) {
                if (child instanceof HBox) {
                    HBox innerHBox = (HBox) child;

                    for (Node circleNode : innerHBox.getChildren()) {
                        if (circleNode instanceof Circle) {
                            Circle circle = (Circle) circleNode;
                            circle.setOnMouseClicked(event -> handleColorClick(playerIndex, circle));
                            circle.setStyle("-fx-cursor: hand;");
                        }
                    }
                }
            }
        }
    }

    private void handleColorClick(int playerIndex, Circle clickedCircle) {
        Paint chosenColor = clickedCircle.getFill();
        if (chosenColor.equals(selectedColors[playerIndex])) { // kalau klik warna sama maka direset, ibarat cancel
            selectedColors[playerIndex] = null;
            updateColorVisuals();
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (i != playerIndex && chosenColor.equals(selectedColors[i])) {
                return;
            }
        }

        selectedColors[playerIndex] = chosenColor;
        updateColorVisuals();
    }

    private void updateColorVisuals() {
        HBox[] playerBoxes = {player1Box, player2Box, player3Box, player4Box};

        for (int pIndex = 0; pIndex < 4; pIndex++) { // cek semua player
            if (playerBoxes[pIndex] == null) continue;

            for (Node child : playerBoxes[pIndex].getChildren()) { // cek elemen dalam hbox utama
                if (child instanceof HBox) {
                    HBox innerHBox = (HBox) child;

                    for (Node circleNode : innerHBox.getChildren()) { // lingkaran warna
                        if (circleNode instanceof Circle) {
                            Circle circle = (Circle) circleNode;
                            Paint circleColor = circle.getFill();

                            boolean isTakenByOther = false;
                            boolean isSelectedByMe = false;

                            for (int i = 0; i < 4; i++) {
                                if (circleColor.equals(selectedColors[i])) {
                                    if (i == pIndex) isSelectedByMe = true;
                                    else isTakenByOther = true;
                                }
                            }

                            // Efek visual
                            if (isTakenByOther) {
                                circle.setOpacity(0.2); // redupkan warna yang sama untuk player lain
                                circle.setDisable(true); // disable untuk player lain karena udah dipilih
                                circle.setEffect(null);
                            } else if (isSelectedByMe) {
                                circle.setOpacity(1.0);
                                circle.setDisable(false); // tidak disable biar bisa cancel
                                circle.setEffect(new DropShadow(20, Color.BLACK)); // efek terpilih
                            } else {
                                // kondisi default
                                circle.setOpacity(1.0);
                                circle.setDisable(false);
                                circle.setEffect(null);
                            }
                        }
                    }
                }
            }
        }
    }

    @FXML
    private void handleLoadMap(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Map Plugin");
        FileChooser.ExtensionFilter extFilterJAR = new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar");
        fileChooser.getExtensionFilters().addAll(extFilterJAR);

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File fileYangDipilih = fileChooser.showOpenDialog(window);

        if (fileYangDipilih != null) {
            System.out.println("File dipilih: " + fileYangDipilih.getAbsolutePath());
        }
    }

    @FXML
    private void startGame() throws IOException {
        App.setRoot("game");
    }

    @FXML
    private void exit() throws IOException {
        App.setRoot("main");
    }
}