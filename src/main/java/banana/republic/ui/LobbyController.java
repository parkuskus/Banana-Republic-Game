package banana.republic.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import banana.republic.App;
import banana.republic.core.Game;
import banana.republic.player.BotPlayer;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.player.PlayerStrategy;
import banana.republic.plugin.MapGeneratorPlugin;
import banana.republic.plugin.PluginLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

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
    private final PluginLoader pluginLoader = new PluginLoader();
    private MapGeneratorPlugin loadedMapPlugin = null;
    private PlayerStrategy loadedBotStrategy = null;

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
            try {
                loadedMapPlugin = pluginLoader.loadMapGenerator(fileYangDipilih.getAbsolutePath());
                showInfo("Map plugin berhasil dimuat: " + fileYangDipilih.getName());
            } catch (Exception e) {
                showAlert("Gagal memuat map plugin", e.getMessage());
            }
        }
    }

    @FXML
    private void handleLoadBot(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Bot Strategy Plugin");
        FileChooser.ExtensionFilter extFilterJAR = new FileChooser.ExtensionFilter("JAR files (*.jar)", "*.jar");
        fileChooser.getExtensionFilters().addAll(extFilterJAR);

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File fileYangDipilih = fileChooser.showOpenDialog(window);

        if (fileYangDipilih != null) {
            try {
                loadedBotStrategy = pluginLoader.loadBotStrategy(fileYangDipilih.getAbsolutePath());
                showInfo("Bot strategy plugin berhasil dimuat: " + fileYangDipilih.getName());
            } catch (Exception e) {
                showAlert("Gagal memuat bot plugin", e.getMessage());
            }
        }
    }

    @FXML
    private void startGame() throws IOException {
        int playerCount = Integer.parseInt(numPlayersBox.getValue().split(" ")[0]);
        HBox[] playerBoxes = {player1Box, player2Box, player3Box, player4Box};

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String name = getPlayerNameFromBox(playerBoxes[i]);

            // Validasi 1: Cek apakah nama sudah diisi
            if (name == null || name.trim().isEmpty()) {
                showAlert("Data Tidak Lengkap", "Mohon isi nama untuk Player " + (i + 1) + "!");
                return; // Membatalkan proses ke game berikutnya
            }

            // Validasi 2: Cek apakah warna sudah dipilih
            if (selectedColors[i] == null) {
                showAlert("Data Tidak Lengkap", "Mohon pilih warna untuk Player " + (i + 1) + "!");
                return; // Membatalkan proses ke game berikutnya
            }

            PlayerColor color = paintToPlayerColor(selectedColors[i]);
            if (color == null) {
                showAlert("Warna Tidak Valid", "Warna yang dipilih untuk Player " + (i + 1) + " tidak terdaftar dalam sistem!");
                return;
            }

            if (loadedBotStrategy != null && i == playerCount - 1) {
                players.add(new BotPlayer(name, color, loadedBotStrategy));
            } else {
                players.add(new HumanPlayer(name, color));
            }
        }

        Game game = new Game(players, loadedMapPlugin);

        // Initialize deck and inject any loaded card plugins
        game.getCardDeck().buildDefaultDeck();
        java.util.List<banana.republic.card.ExperimentCard> pluginCards =
            pluginLoader.getLoadedCardPlugins();
        if (!pluginCards.isEmpty()) {
            game.getCardDeck().injectPluginCards(pluginCards);
        }

        FXMLLoader loader = App.getLoader("game");
        Parent root = loader.load();
        GameController controller = loader.getController();
        controller.initialize(game);
        App.setRootFromLoader(root);
    }

    // Helper method untuk memunculkan pop-up peringatan JavaFX
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        showInfo("Information", message);
    }

    private String getPlayerNameFromBox(HBox box) {
        if (box == null) return null;
        for (Node child : box.getChildren()) {
            if (child instanceof TextField tf) {
                return tf.getText();
            }
        }
        return null;
    }

    private PlayerColor paintToPlayerColor(Paint paint) {
        if (paint instanceof Color c) {
            String hex = String.format("#%02x%02x%02x",
                    (int) (c.getRed() * 255),
                    (int) (c.getGreen() * 255),
                    (int) (c.getBlue() * 255));
            return switch (hex.toLowerCase()) {
                case "#c21a09" -> PlayerColor.RED;
                case "#305cde" -> PlayerColor.BLUE;
                case "#4fc978" -> PlayerColor.GREEN;
                case "#ff7f00" -> PlayerColor.ORANGE;
                default -> null;
            };
        }
        return null;
    }

    @FXML
    private void exit() throws IOException {
        App.setRoot("main");
    }
}
