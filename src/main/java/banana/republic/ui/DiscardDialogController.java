package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import banana.republic.ui.command.DiscardUiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DiscardDialogController implements Initializable, DialogController, GameAwareController {

    @FXML
    private Label lblHandCount;
    @FXML
    private Label lblMustDiscardCount;
    @FXML
    private Label lblSelectedCount;
    @FXML
    private Label lblRequiredCount;
    @FXML
    private Label lblWoodOwned, lblBrickOwned, lblWheatOwned, lblOreOwned, lblBananaOwned;

    @FXML
    private Label lblGiveWoodVal, lblGiveBrickVal, lblGiveWheatVal, lblGiveOreVal, lblGiveBananaVal;
    @FXML
    private Button btnGiveWoodMinus, btnGiveWoodPlus;
    @FXML
    private Button btnGiveBrickMinus, btnGiveBrickPlus;
    @FXML
    private Button btnGiveWheatMinus, btnGiveWheatPlus;
    @FXML
    private Button btnGiveOreMinus, btnGiveOrePlus;
    @FXML
    private Button btnGiveBananaMinus, btnGiveBananaPlus;

    private Runnable closeHandler;
    private Game game;
    private Player targetPlayer;
    private int requiredDiscard = 0;
    private final UiDialogs dialogs = new UiDialogs();
    private final DiscardUiService discardUiService = new DiscardUiService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int maxItems = 20;
        setupStepper(btnGiveWoodMinus, btnGiveWoodPlus, lblGiveWoodVal, 0, maxItems, this::updateSelectedCount);
        setupStepper(btnGiveBrickMinus, btnGiveBrickPlus, lblGiveBrickVal, 0, maxItems, this::updateSelectedCount);
        setupStepper(btnGiveWheatMinus, btnGiveWheatPlus, lblGiveWheatVal, 0, maxItems, this::updateSelectedCount);
        setupStepper(btnGiveOreMinus, btnGiveOrePlus, lblGiveOreVal, 0, maxItems, this::updateSelectedCount);
        setupStepper(btnGiveBananaMinus, btnGiveBananaPlus, lblGiveBananaVal, 0, maxItems, this::updateSelectedCount);
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    public void setDiscardingPlayer(Player player) {
        this.targetPlayer = player;
        populatePlayerData();
    }

    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    private void populatePlayerData() {
        if (game == null || targetPlayer == null) return;

        int totalResources = targetPlayer.getTotalResourceCount();
        requiredDiscard = totalResources > Game.HAND_LIMIT ? totalResources / 2 : 0;

        if (lblHandCount != null) lblHandCount.setText(String.valueOf(totalResources));
        if (lblMustDiscardCount != null) lblMustDiscardCount.setText(String.valueOf(requiredDiscard));
        if (lblRequiredCount != null) lblRequiredCount.setText(String.valueOf(requiredDiscard));
        if (lblSelectedCount != null) lblSelectedCount.setText("0");

        if (lblWoodOwned != null) lblWoodOwned.setText(String.valueOf(targetPlayer.getResourceCount(ResourceType.WOOD)));
        if (lblBrickOwned != null) lblBrickOwned.setText(String.valueOf(targetPlayer.getResourceCount(ResourceType.BRICK)));
        if (lblWheatOwned != null) lblWheatOwned.setText(String.valueOf(targetPlayer.getResourceCount(ResourceType.WHEAT)));
        if (lblOreOwned != null) lblOreOwned.setText(String.valueOf(targetPlayer.getResourceCount(ResourceType.ORE)));
        if (lblBananaOwned != null) lblBananaOwned.setText(String.valueOf(targetPlayer.getResourceCount(ResourceType.BANANA)));
    }

    private void updateSelectedCount() {
        if (lblSelectedCount == null) return;
        int total = 0;
        total += parseInt(lblGiveWoodVal);
        total += parseInt(lblGiveBrickVal);
        total += parseInt(lblGiveWheatVal);
        total += parseInt(lblGiveOreVal);
        total += parseInt(lblGiveBananaVal);
        lblSelectedCount.setText(String.valueOf(total));
    }

    @FXML
    private void closeDialog() {
        if (requiredDiscard > 0) {
            dialogs.showError("Kamu harus membuang tepat " + requiredDiscard + " kartu sebelum menutup.");
            return;
        }
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @FXML
    private void handleDiscard() {
        if (game == null || targetPlayer == null) return;

        Map<ResourceType, Integer> toDiscard = new EnumMap<>(ResourceType.class);
        toDiscard.put(ResourceType.WOOD, parseInt(lblGiveWoodVal));
        toDiscard.put(ResourceType.BRICK, parseInt(lblGiveBrickVal));
        toDiscard.put(ResourceType.WHEAT, parseInt(lblGiveWheatVal));
        toDiscard.put(ResourceType.ORE, parseInt(lblGiveOreVal));
        toDiscard.put(ResourceType.BANANA, parseInt(lblGiveBananaVal));

        var result = discardUiService.discard(game, targetPlayer, toDiscard, requiredDiscard);
        if (!result.isSuccess()) {
            dialogs.showError(result.getMessage());
            return;
        }

        requiredDiscard = 0;
        closeDialog();
    }

    private void setupStepper(Button btnMinus, Button btnPlus, Label lblValue, int min, int max, Runnable onUpdate) {
        btnPlus.setOnAction(event -> {
            int currentValue = parseInt(lblValue);
            if (currentValue < max) {
                lblValue.setText(String.valueOf(currentValue + 1));
                if (onUpdate != null) onUpdate.run();
            }
        });
        btnMinus.setOnAction(event -> {
            int currentValue = parseInt(lblValue);
            if (currentValue > min) {
                lblValue.setText(String.valueOf(currentValue - 1));
                if (onUpdate != null) onUpdate.run();
            }
        });
    }

    private int parseInt(Label lbl) {
        try {
            return Integer.parseInt(lbl.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
