package banana.republic.ui;

import banana.republic.App;
import banana.republic.core.Game;
import banana.republic.player.BotPlayer;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import banana.republic.trade.TradeOffer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Trade dialog controller.
 */
public class TradeDialogController implements Initializable, DialogController, GameAwareController {
    @FXML
    private Label tabDomestic;
    @FXML
    private Label tabMaritime;
    @FXML
    private VBox offerBox;

    @FXML
    private Button btnGiveWoodMinus, btnGiveBrickMinus, btnGiveWheatMinus, btnGiveOreMinus, btnGiveBananaMinus;
    @FXML
    private Label lblGiveWoodVal, lblGiveBrickVal, lblGiveWheatVal, lblGiveOreVal, lblGiveBananaVal;
    @FXML
    private Button btnGiveWoodPlus, btnGiveBrickPlus, btnGiveWheatPlus, btnGiveOrePlus, btnGiveBananaPlus;

    @FXML
    private Button btnReceiveWoodMinus, btnReceiveBrickMinus, btnReceiveWheatMinus, btnReceiveOreMinus, btnReceiveBananaMinus;
    @FXML
    private Label lblReceiveWoodVal, lblReceiveBrickVal, lblReceiveWheatVal, lblReceiveOreVal, lblReceiveBananaVal;
    @FXML
    private Button btnReceiveWoodPlus, btnReceiveBrickPlus, btnReceiveWheatPlus, btnReceiveOrePlus, btnReceiveBananaPlus;

    private Runnable closeHandler;
    private Game game;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int maxItems = 20;
        setupStepper(btnGiveWoodMinus, btnGiveWoodPlus, lblGiveWoodVal, 0, maxItems);
        setupStepper(btnGiveWheatMinus, btnGiveWheatPlus, lblGiveWheatVal, 0, maxItems);
        setupStepper(btnGiveBrickMinus, btnGiveBrickPlus, lblGiveBrickVal, 0, maxItems);
        setupStepper(btnGiveOreMinus, btnGiveOrePlus, lblGiveOreVal, 0, maxItems);
        setupStepper(btnGiveBananaMinus, btnGiveBananaPlus, lblGiveBananaVal, 0, maxItems);

        setupStepper(btnReceiveWoodMinus, btnReceiveWoodPlus, lblReceiveWoodVal, 0, maxItems);
        setupStepper(btnReceiveWheatMinus, btnReceiveWheatPlus, lblReceiveWheatVal, 0, maxItems);
        setupStepper(btnReceiveBrickMinus, btnReceiveBrickPlus, lblReceiveBrickVal, 0, maxItems);
        setupStepper(btnReceiveOreMinus, btnReceiveOrePlus, lblReceiveOreVal, 0, maxItems);
        setupStepper(btnReceiveBananaMinus, btnReceiveBananaPlus, lblReceiveBananaVal, 0, maxItems);
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    // untuk handle plus minus resource
    private void setupStepper(Button btnMinus, Button btnPlus, Label lblValue, int min, int max) {
        btnPlus.setOnAction(event -> {
            int currentValue = Integer.parseInt(lblValue.getText());
            if (currentValue < max) {
                lblValue.setText(String.valueOf(currentValue + 1));
            }
        });

        btnMinus.setOnAction(event -> {
            int currentValue = Integer.parseInt(lblValue.getText());
            if (currentValue > min) {
                lblValue.setText(String.valueOf(currentValue - 1));
            }
        });
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
    private void switchToDomestic() {
        ubahGayaTab(tabDomestic, tabMaritime);
        offerBox.setVisible(true);
    }

    @FXML
    private void switchToMaritime() {
        ubahGayaTab(tabMaritime, tabDomestic);
        offerBox.setVisible(false);
    }

    @FXML
    private void executeTrade() {
        if (game == null) {
            showError("Game tidak tersedia.");
            return;
        }
        Player active = game.getActivePlayer();
        if (active == null) {
            showError("Tidak ada pemain aktif.");
            return;
        }

        Map<ResourceType, Integer> give = readResourcesFromLabels(
            lblGiveWoodVal, lblGiveBrickVal, lblGiveWheatVal, lblGiveOreVal, lblGiveBananaVal
        );
        Map<ResourceType, Integer> receive = readResourcesFromLabels(
            lblReceiveWoodVal, lblReceiveBrickVal, lblReceiveWheatVal, lblReceiveOreVal, lblReceiveBananaVal
        );

        int totalGive = give.values().stream().mapToInt(Integer::intValue).sum();
        int totalReceive = receive.values().stream().mapToInt(Integer::intValue).sum();

        if (totalGive == 0 || totalReceive == 0) {
            showError("Penawaran atau permintaan tidak boleh kosong.");
            return;
        }

        boolean isMaritime = tabMaritime.getStyleClass().contains("tab-active");

        if (isMaritime) {
            ResourceType sellType = null;
            ResourceType buyType = null;
            int typesGiven = 0;
            int typesReceived = 0;

            for (Map.Entry<ResourceType, Integer> entry : give.entrySet()) {
                if (entry.getValue() > 0) {
                    sellType = entry.getKey();
                    typesGiven++;
                }
            }
            for (Map.Entry<ResourceType, Integer> entry : receive.entrySet()) {
                if (entry.getValue() > 0) {
                    buyType = entry.getKey();
                    typesReceived++;
                }
            }

            if (typesGiven != 1 || typesReceived != 1) {
                showError("Trade Maritim hanya dapat menukar 1 jenis sumber daya dengan 1 jenis lainnya.");
                return;
            }

            try {
                banana.republic.trade.ValidationResult result = game.tradeWithBank(active, sellType, buyType);
                if (!result.isValid()) {
                    showError("Trade Maritim gagal: " + result.getReason());
                } else {
                    closeDialog();
                }
            } catch (Exception e) {
                showError("Error: " + e.getMessage());
            }
            return;
        }

        // DOMESTIC TRADE
        for (Map.Entry<ResourceType, Integer> entry : give.entrySet()) {
            if (!active.hasResource(entry.getKey(), entry.getValue())) {
                showError("Anda tidak punya cukup " + entry.getKey().getDisplayName() + " untuk ditukar.");
                return;
            }
        }

        java.util.List<Player> otherPlayers = game.getPlayers().stream()
                .filter(p -> !p.equals(active))
                .toList();

        if (otherPlayers.isEmpty()) {
            showError("Tidak ada pemain lain.");
            return;
        }

        javafx.scene.control.ChoiceDialog<Player> dialog = new javafx.scene.control.ChoiceDialog<>(otherPlayers.get(0), otherPlayers);
        dialog.setTitle("Pilih Target Trade");
        dialog.setHeaderText("Pilih pemain yang akan menerima tawaran dagangmu.");
        dialog.setContentText("Target:");
        dialog.showAndWait().ifPresent(target -> {
            // Validasi pemain target punya resource yang diminta
            for (Map.Entry<ResourceType, Integer> entry : receive.entrySet()) {
                if (!target.hasResource(entry.getKey(), entry.getValue())) {
                    showError(target.getName() + " tidak punya cukup " + entry.getKey().getDisplayName() + ".");
                    return;
                }
            }

            if (target.isBot() && target instanceof BotPlayer botPlayer) {
                boolean accepted = botPlayer.getStrategy().shouldAcceptTrade(
                    game.getState(), active, give, receive);
                if (!accepted) {
                    showError(target.getName() + " (bot) menolak tawaran dagang.");
                    return;
                }
            } else {
                javafx.scene.control.Alert confirmDialog = new javafx.scene.control.Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Konfirmasi Trade");
                confirmDialog.setHeaderText("Tawaran dari " + active.getName() + " ke " + target.getName());
                confirmDialog.setContentText(target.getName() + ", apakah kamu menerima trade ini?");
                java.util.Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();
                if (result.isEmpty() || result.get() != javafx.scene.control.ButtonType.OK) {
                    showError(target.getName() + " menolak tawaran dagang.");
                    return;
                }
            }

            try {
                for (Map.Entry<ResourceType, Integer> entry : give.entrySet()) {
                    active.removeResource(entry.getKey(), entry.getValue());
                    target.addResource(entry.getKey(), entry.getValue());
                }
                for (Map.Entry<ResourceType, Integer> entry : receive.entrySet()) {
                    target.removeResource(entry.getKey(), entry.getValue());
                    active.addResource(entry.getKey(), entry.getValue());
                }
                game.getGameLog().addEntry(
                    banana.republic.core.LogEntry.EventType.TRADE,
                    active.getName(),
                    active.getName() + " berdagang dengan " + target.getName()
                );
                closeDialog();
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        });
    }

    private Map<ResourceType, Integer> readResourcesFromLabels(
            Label wood, Label brick, Label wheat, Label ore, Label banana) {
        Map<ResourceType, Integer> map = new EnumMap<>(ResourceType.class);
        map.put(ResourceType.WOOD, parseLabel(wood));
        map.put(ResourceType.BRICK, parseLabel(brick));
        map.put(ResourceType.WHEAT, parseLabel(wheat));
        map.put(ResourceType.ORE, parseLabel(ore));
        map.put(ResourceType.BANANA, parseLabel(banana));
        return map;
    }

    private int parseLabel(Label lbl) {
        try {
            return Integer.parseInt(lbl.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void ubahGayaTab(Label tabAktif, Label tabInaktif) {
        tabAktif.getStyleClass().remove("tab-inactive");
        if (!tabAktif.getStyleClass().contains("tab-active")) {
            tabAktif.getStyleClass().add("tab-active");
        }
        tabInaktif.getStyleClass().remove("tab-active");
        if (!tabInaktif.getStyleClass().contains("tab-inactive")) {
            tabInaktif.getStyleClass().add("tab-inactive");
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
