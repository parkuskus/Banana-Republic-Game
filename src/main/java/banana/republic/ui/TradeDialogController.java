package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import banana.republic.ui.command.TradeCommandService;
import banana.republic.ui.command.TradeUiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Trade dialog controller.
 */
public class TradeDialogController implements Initializable, DialogController, GameAwareController {
    private enum ResourceSide { GIVE, RECEIVE }

    @FXML
    private Label tabDomestic;
    @FXML
    private Label tabMaritime;
    @FXML
    private VBox offerBox;
    @FXML
    private HBox offerTargetsBox;
    @FXML
    private Label offerStatusLabel;
    @FXML
    private HBox tradeResponseBox;
    @FXML
    private Button btnSubmitOffer;

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
    private final UiDialogs dialogs = new UiDialogs();
    private final TradeCommandService tradeUiService = new TradeUiService();
    private Player offerOwner;
    private Player selectedTarget;
    private Player pendingOfferer;
    private Player pendingResponder;
    private Map<ResourceType, Integer> pendingGive;
    private Map<ResourceType, Integer> pendingReceive;
    private boolean awaitingResponse;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int maxItems = 20;
        setupStepper(btnGiveWoodMinus, btnGiveWoodPlus, lblGiveWoodVal, 0, maxItems, ResourceSide.GIVE, ResourceType.WOOD);
        setupStepper(btnGiveWheatMinus, btnGiveWheatPlus, lblGiveWheatVal, 0, maxItems, ResourceSide.GIVE, ResourceType.WHEAT);
        setupStepper(btnGiveBrickMinus, btnGiveBrickPlus, lblGiveBrickVal, 0, maxItems, ResourceSide.GIVE, ResourceType.BRICK);
        setupStepper(btnGiveOreMinus, btnGiveOrePlus, lblGiveOreVal, 0, maxItems, ResourceSide.GIVE, ResourceType.ORE);
        setupStepper(btnGiveBananaMinus, btnGiveBananaPlus, lblGiveBananaVal, 0, maxItems, ResourceSide.GIVE, ResourceType.BANANA);

        setupStepper(btnReceiveWoodMinus, btnReceiveWoodPlus, lblReceiveWoodVal, 0, maxItems, ResourceSide.RECEIVE, ResourceType.WOOD);
        setupStepper(btnReceiveWheatMinus, btnReceiveWheatPlus, lblReceiveWheatVal, 0, maxItems, ResourceSide.RECEIVE, ResourceType.WHEAT);
        setupStepper(btnReceiveBrickMinus, btnReceiveBrickPlus, lblReceiveBrickVal, 0, maxItems, ResourceSide.RECEIVE, ResourceType.BRICK);
        setupStepper(btnReceiveOreMinus, btnReceiveOrePlus, lblReceiveOreVal, 0, maxItems, ResourceSide.RECEIVE, ResourceType.ORE);
        setupStepper(btnReceiveBananaMinus, btnReceiveBananaPlus, lblReceiveBananaVal, 0, maxItems, ResourceSide.RECEIVE, ResourceType.BANANA);
        renderOfferTargets();
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
        this.offerOwner = game != null ? game.getActivePlayer() : null;
        renderOfferTargets();
    }

    // untuk handle plus minus resource
    private void setupStepper(Button btnMinus, Button btnPlus, Label lblValue, int min, int max,
                              ResourceSide side, ResourceType type) {
        btnPlus.setOnAction(event -> {
            adjustResourceValue(lblValue, min, max, side, type, 1);
        });

        btnMinus.setOnAction(event -> {
            adjustResourceValue(lblValue, min, max, side, type, -1);
        });
    }

    private void adjustResourceValue(Label lblValue, int min, int max,
                                     ResourceSide side, ResourceType type, int delta) {
        if (isMaritimeMode()) {
            adjustMaritimeValue(side, type, delta);
            return;
        }

        int currentValue = parseLabel(lblValue);
        int nextValue = Math.max(min, Math.min(max, currentValue + delta));
        if (nextValue != currentValue) {
            lblValue.setText(String.valueOf(nextValue));
            renderOfferTargets();
        }
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
        resetAllResourceLabels();
        renderOfferTargets();
    }

    @FXML
    private void switchToMaritime() {
        ubahGayaTab(tabMaritime, tabDomestic);
        offerBox.setVisible(false);
        selectedTarget = null;
        resetAllResourceLabels();
    }

    @FXML
    private void executeTrade() {
        if (game == null) {
            dialogs.showError("Game tidak tersedia.");
            return;
        }
        Player active = game.getActivePlayer();
        if (active == null) {
            dialogs.showError("Tidak ada pemain aktif.");
            return;
        }
        if (game.getCurrentPhase() != GamePhase.TRADE_BUILD) {
            dialogs.showError("Trade hanya bisa dilakukan setelah lempar dadu pada fase Trade & Build.");
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
            dialogs.showError("Penawaran atau permintaan tidak boleh kosong.");
            return;
        }
        String validationError = validateExchange(give, receive);
        if (validationError != null) {
            dialogs.showError(validationError);
            return;
        }

        boolean isMaritime = tabMaritime.getStyleClass().contains("tab-active");

        if (isMaritime) {
            String maritimeError = validateMaritimeSelection(active, give, receive);
            if (maritimeError != null) {
                dialogs.showError(maritimeError);
                return;
            }
            var result = tradeUiService.executeMaritime(game, active, give, receive);
            if (result.isSuccess()) {
                closeDialog();
            } else {
                dialogs.showError(result.getMessage());
            }
            return;
        }

        if (awaitingResponse) {
            dialogs.showError("Selesaikan respon tawaran saat ini terlebih dahulu.");
            return;
        }
        if (offerOwner == null) offerOwner = active;
        if (selectedTarget == null) {
            dialogs.showError("Pilih target trade yang memiliki resource diminta.");
            return;
        }
        if (!involvesActivePlayer(active, offerOwner, selectedTarget)) {
            dialogs.showError("Semua trade harus melibatkan pemain aktif.");
            return;
        }
        String partyValidationError = validateTradeParties(offerOwner, selectedTarget, give, receive);
        if (partyValidationError != null) {
            dialogs.showError(partyValidationError);
            renderOfferTargets();
            return;
        }
        beginResponse(offerOwner, selectedTarget, give, receive);
    }

    private void beginResponse(Player offerer, Player responder,
                               Map<ResourceType, Integer> give,
                               Map<ResourceType, Integer> receive) {
        pendingOfferer = offerer;
        pendingResponder = responder;
        pendingGive = new EnumMap<>(give);
        pendingReceive = new EnumMap<>(receive);
        awaitingResponse = true;
        if (btnSubmitOffer != null) btnSubmitOffer.setDisable(true);

        if (responder.isBot() && responder instanceof banana.republic.player.BotPlayer botPlayer) {
            boolean accepted = botPlayer.getStrategy().shouldAcceptTrade(game.getState(), offerer, give, receive);
            if (accepted) acceptPendingOffer();
            else rejectPendingOffer(responder.getName() + " (bot) menolak tawaran dagang.");
            return;
        }

        renderResponseButtons();
    }

    private void renderResponseButtons() {
        if (offerStatusLabel != null) {
            offerStatusLabel.setText("Tawaran dari " + pendingOfferer.getName() + " ke "
                    + pendingResponder.getName() + ". " + pendingResponder.getName()
                    + " dapat menerima, menolak, atau mengajukan counter-offer.");
        }
        if (tradeResponseBox == null) return;
        tradeResponseBox.getChildren().clear();
        tradeResponseBox.getChildren().add(responseButton("ACCEPT", this::acceptPendingOffer));
        tradeResponseBox.getChildren().add(responseButton("REJECT", () ->
                rejectPendingOffer(pendingResponder.getName() + " menolak tawaran dagang.")));
        tradeResponseBox.getChildren().add(responseButton("COUNTER-OFFER", this::startCounterOffer));
    }

    private Button responseButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("trade-response-btn");
        button.setOnAction(event -> action.run());
        return button;
    }

    private void acceptPendingOffer() {
        var result = tradeUiService.executeDomestic(game, pendingOfferer, pendingResponder, pendingGive, pendingReceive);
        if (result.isSuccess()) {
            closeDialog();
        } else {
            dialogs.showError(result.getMessage());
            resetNegotiation(result.getMessage());
        }
    }

    private void rejectPendingOffer(String message) {
        resetNegotiation(message);
    }

    private void startCounterOffer() {
        Player nextOfferer = pendingResponder;
        Player nextTarget = pendingOfferer;
        setResourceLabels(pendingReceive, pendingGive);
        resetNegotiation(nextOfferer.getName() + " mengajukan counter-offer. Atur resource, lalu submit.");
        offerOwner = nextOfferer;
        selectedTarget = nextTarget;
        renderOfferTargets();
    }

    private void resetNegotiation(String status) {
        awaitingResponse = false;
        pendingOfferer = null;
        pendingResponder = null;
        pendingGive = null;
        pendingReceive = null;
        if (btnSubmitOffer != null) btnSubmitOffer.setDisable(false);
        if (tradeResponseBox != null) tradeResponseBox.getChildren().clear();
        if (offerStatusLabel != null) offerStatusLabel.setText(status);
    }

    private void renderOfferTargets() {
        if (offerTargetsBox == null) return;
        offerTargetsBox.getChildren().clear();
        if (game == null || tabMaritime == null || tabMaritime.getStyleClass().contains("tab-active")) return;

        Player active = game.getActivePlayer();
        Player currentOfferer = offerOwner != null ? offerOwner : active;
        if (active == null || currentOfferer == null) return;
        Map<ResourceType, Integer> requested = readResourcesFromLabels(
                lblReceiveWoodVal, lblReceiveBrickVal, lblReceiveWheatVal, lblReceiveOreVal, lblReceiveBananaVal);
        int totalRequested = requested.values().stream().mapToInt(Integer::intValue).sum();
        if (totalRequested <= 0) {
            if (offerStatusLabel != null) {
                offerStatusLabel.setText("Pilih resource yang ingin diminta untuk melihat target trade.");
            }
            selectedTarget = null;
            return;
        }

        List<Player> candidates;
        if (!currentOfferer.equals(active)) {
            candidates = hasRequestedResources(active, requested) ? List.of(active) : List.of();
        } else {
            candidates = game.getPlayers().stream()
                    .filter(player -> !player.equals(active))
                    .filter(player -> hasRequestedResources(player, requested))
                    .toList();
        }

        if (candidates.isEmpty()) {
            if (offerStatusLabel != null) offerStatusLabel.setText("Tidak ada pemain yang memiliki resource diminta.");
            selectedTarget = null;
            return;
        }

        if (selectedTarget != null && !candidates.contains(selectedTarget)) selectedTarget = null;
        if (selectedTarget == null && candidates.size() == 1 && !currentOfferer.equals(active)) {
            selectedTarget = candidates.get(0);
        }
        if (offerStatusLabel != null && !awaitingResponse) {
            offerStatusLabel.setText(currentOfferer.getName() + " memilih target trade.");
        }
        for (Player candidate : candidates) {
            offerTargetsBox.getChildren().add(playerCard(candidate));
        }
    }

    private VBox playerCard(Player player) {
        VBox card = new VBox(5);
        card.setAlignment(javafx.geometry.Pos.CENTER);
        card.getStyleClass().add("player-card");
        if (player.equals(selectedTarget)) card.getStyleClass().add("player-card-selected");

        Label avatar = new Label(playerInitial(player));
        avatar.getStyleClass().add("avatar-icon");
        avatar.getStyleClass().add(avatarStyle(player));
        Label name = new Label(player.getName());
        name.getStyleClass().add("player-name");
        card.getChildren().addAll(avatar, name);
        card.setOnMouseClicked(event -> {
            selectedTarget = player;
            renderOfferTargets();
        });
        return card;
    }

    private String playerInitial(Player player) {
        if (player == null || player.getName() == null || player.getName().isBlank()) return "P";
        return player.getName().trim().substring(0, 1).toUpperCase();
    }

    private String avatarStyle(Player player) {
        if (player == null || player.getColor() == null) return "avatar-blue";
        return switch (player.getColor()) {
            case RED -> "avatar-red";
            case BLUE -> "avatar-blue";
            case GREEN -> "avatar-green";
            case ORANGE -> "avatar-orange";
            default -> "avatar-blue";
        };
    }

    private boolean hasRequestedResources(Player player, Map<ResourceType, Integer> requested) {
        int totalRequested = requested.values().stream().mapToInt(Integer::intValue).sum();
        if (totalRequested <= 0) return false;
        for (Map.Entry<ResourceType, Integer> entry : requested.entrySet()) {
            if (entry.getValue() > 0 && !player.hasResource(entry.getKey(), entry.getValue())) return false;
        }
        return true;
    }

    private String validateExchange(Map<ResourceType, Integer> give, Map<ResourceType, Integer> receive) {
        int totalGive = give.values().stream().mapToInt(Integer::intValue).sum();
        int totalReceive = receive.values().stream().mapToInt(Integer::intValue).sum();
        if (totalGive <= 0 || totalReceive <= 0) {
            return "Tidak boleh memberi resource secara cuma-cuma. Kedua pihak harus saling menukar resource.";
        }
        for (ResourceType type : ResourceType.values()) {
            if (give.getOrDefault(type, 0) > 0 && receive.getOrDefault(type, 0) > 0) {
                return "Tidak boleh menukar sumber daya sejenis: " + type.getDisplayName() + ".";
            }
        }
        return null;
    }

    private String validateTradeParties(Player offerer, Player target,
                                        Map<ResourceType, Integer> give,
                                        Map<ResourceType, Integer> receive) {
        if (offerer == null || target == null) return "Pilih target trade terlebih dahulu.";
        if (offerer.equals(target)) return "Pemain tidak dapat berdagang dengan dirinya sendiri.";
        for (Map.Entry<ResourceType, Integer> entry : give.entrySet()) {
            if (entry.getValue() > 0 && !offerer.hasResource(entry.getKey(), entry.getValue())) {
                return offerer.getName() + " tidak memiliki cukup " + entry.getKey().getDisplayName() + ".";
            }
        }
        for (Map.Entry<ResourceType, Integer> entry : receive.entrySet()) {
            if (entry.getValue() > 0 && !target.hasResource(entry.getKey(), entry.getValue())) {
                return target.getName() + " tidak memiliki cukup " + entry.getKey().getDisplayName() + ".";
            }
        }
        return null;
    }

    private boolean involvesActivePlayer(Player active, Player offerer, Player target) {
        return active != null && (active.equals(offerer) || active.equals(target));
    }

    private void adjustMaritimeValue(ResourceSide side, ResourceType type, int delta) {
        Map<ResourceType, Integer> values = side == ResourceSide.GIVE ? currentGive() : currentReceive();
        int currentValue = values.getOrDefault(type, 0);
        int nextValue = Math.max(0, Math.min(20, currentValue + delta));

        if (side == ResourceSide.GIVE) {
            Map<ResourceType, Integer> nextGive = emptyResources();
            nextGive.put(type, nextValue);
            setGiveLabels(nextGive);
            return;
        }

        Map<ResourceType, Integer> nextReceive = emptyResources();
        nextReceive.put(type, nextValue);
        setReceiveLabels(nextReceive);
    }

    private String validateMaritimeSelection(Player active, Map<ResourceType, Integer> give,
                                             Map<ResourceType, Integer> receive) {
        ResourceType sellType = singlePositiveType(give);
        ResourceType buyType = singlePositiveType(receive);
        if (sellType == null || buyType == null) {
            return "Trade Maritim harus memilih tepat 1 jenis resource untuk dijual dan 1 jenis resource untuk dibeli.";
        }
        if (sellType == buyType) {
            return "Trade Maritim tidak boleh menukar resource sejenis.";
        }
        int buyAmount = receive.getOrDefault(buyType, 0);
        int requiredGive = game.getTradeRatio(active, sellType) * buyAmount;
        if (give.getOrDefault(sellType, 0) != requiredGive) {
            return "Trade Maritim " + sellType.getDisplayName() + " memakai rasio "
                    + game.getTradeRatio(active, sellType) + ":1, jadi butuh "
                    + requiredGive + " " + sellType.getDisplayName() + ".";
        }
        return null;
    }

    private ResourceType singlePositiveType(Map<ResourceType, Integer> resources) {
        ResourceType type = null;
        int count = 0;
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > 0) {
                type = entry.getKey();
                count++;
            }
        }
        return count == 1 ? type : null;
    }

    private boolean isMaritimeMode() {
        return tabMaritime != null && tabMaritime.getStyleClass().contains("tab-active");
    }

    private Map<ResourceType, Integer> currentGive() {
        return readResourcesFromLabels(lblGiveWoodVal, lblGiveBrickVal, lblGiveWheatVal, lblGiveOreVal, lblGiveBananaVal);
    }

    private Map<ResourceType, Integer> currentReceive() {
        return readResourcesFromLabels(lblReceiveWoodVal, lblReceiveBrickVal, lblReceiveWheatVal, lblReceiveOreVal, lblReceiveBananaVal);
    }

    private void resetAllResourceLabels() {
        Map<ResourceType, Integer> empty = emptyResources();
        setGiveLabels(empty);
        setReceiveLabels(empty);
    }

    private Map<ResourceType, Integer> emptyResources() {
        Map<ResourceType, Integer> values = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            values.put(type, 0);
        }
        return values;
    }

    private void setResourceLabels(Map<ResourceType, Integer> give, Map<ResourceType, Integer> receive) {
        setGiveLabels(give);
        setReceiveLabels(receive);
    }

    private void setGiveLabels(Map<ResourceType, Integer> values) {
        lblGiveWoodVal.setText(String.valueOf(values.getOrDefault(ResourceType.WOOD, 0)));
        lblGiveBrickVal.setText(String.valueOf(values.getOrDefault(ResourceType.BRICK, 0)));
        lblGiveWheatVal.setText(String.valueOf(values.getOrDefault(ResourceType.WHEAT, 0)));
        lblGiveOreVal.setText(String.valueOf(values.getOrDefault(ResourceType.ORE, 0)));
        lblGiveBananaVal.setText(String.valueOf(values.getOrDefault(ResourceType.BANANA, 0)));
    }

    private void setReceiveLabels(Map<ResourceType, Integer> values) {
        lblReceiveWoodVal.setText(String.valueOf(values.getOrDefault(ResourceType.WOOD, 0)));
        lblReceiveBrickVal.setText(String.valueOf(values.getOrDefault(ResourceType.BRICK, 0)));
        lblReceiveWheatVal.setText(String.valueOf(values.getOrDefault(ResourceType.WHEAT, 0)));
        lblReceiveOreVal.setText(String.valueOf(values.getOrDefault(ResourceType.ORE, 0)));
        lblReceiveBananaVal.setText(String.valueOf(values.getOrDefault(ResourceType.BANANA, 0)));
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

}
