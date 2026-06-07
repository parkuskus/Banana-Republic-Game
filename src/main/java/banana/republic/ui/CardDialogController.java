package banana.republic.ui;

import banana.republic.card.DevelopmentCard;
import banana.republic.card.ExperimentCard;
import banana.republic.card.KnightCard;
import banana.republic.card.ProgressCard;
import banana.republic.card.VictoryPointCard;
import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.ui.command.CardUiService;
import banana.republic.ui.command.GameActionUiService;
import banana.republic.ui.dialog.ChoiceDialogService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

public class CardDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;
    private final UiDialogs dialogs = new UiDialogs();
    private final UiNavigator navigator = new AppUiNavigator();
    private final CardUiService cardUiService = new CardUiService();
    private final GameActionUiService gameActionUiService = new GameActionUiService();
    private final ChoiceDialogService choiceDialogService = new ChoiceDialogService();

    @FXML
    private VBox cardPurple;
    @FXML
    private VBox cardGreen;
    @FXML
    private VBox cardOrange;
    @FXML
    private VBox cardGray;
    @FXML
    private VBox handCardsBox;

    private ExperimentCard selectedCard;

    @Override
    public void setGame(Game game) {
        this.game = game;
        updateCardStatus();
    }

    /**
     * Update label status pada kartu berdasarkan hand pemain aktif.
     * Tidak mengubah tampilan visual kartu, hanya status text.
     */
    private void updateCardStatus() {
        if (game == null) return;
        var player = game.getActivePlayer();
        if (player == null) return;
        List<ExperimentCard> hand = player.getHandCards();

        setCardStatus(cardPurple, countType(hand, KnightCard.class));
        setCardStatus(cardGreen, countType(hand, ProgressCard.class));
        setCardStatus(cardOrange, countType(hand, VictoryPointCard.class));
        setCardStatus(cardGray, countOtherCards(hand));
        renderHandCards(hand);
    }

    private int countType(List<ExperimentCard> hand, Class<? extends ExperimentCard> type) {
        return (int) hand.stream().filter(type::isInstance).count();
    }

    private void setCardStatus(VBox cardBox, int count) {
        if (cardBox == null) return;
        // Find status label inside the card box
        for (javafx.scene.Node node : cardBox.getChildren()) {
            if (node instanceof VBox inner) {
                for (javafx.scene.Node n : inner.getChildren()) {
                    if (n instanceof Label lbl && lbl.getStyleClass().contains("dev-card-status")) {
                        lbl.setText(count > 0 ? "DIMILIKI: " + count : "TIDAK ADA");
                        return;
                    }
                }
            }
        }
    }

    private int countOtherCards(List<ExperimentCard> hand) {
        int count = 0;
        for (ExperimentCard card : hand) {
            if (!(card instanceof KnightCard)
                    && !(card instanceof ProgressCard)
                    && !(card instanceof VictoryPointCard)) {
                count++;
            }
        }
        return count;
    }

    private void renderHandCards(List<ExperimentCard> hand) {
        if (handCardsBox == null) return;
        handCardsBox.getChildren().clear();

        Label title = new Label("KARTU DI TANGAN");
        title.getStyleClass().add("section-subtitle");
        handCardsBox.getChildren().add(title);

        if (hand.isEmpty()) {
            Label empty = new Label("Tidak ada kartu.");
            empty.getStyleClass().add("dev-card-status");
            handCardsBox.getChildren().add(empty);
            return;
        }

        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        for (ExperimentCard card : hand) {
            Label chip = new Label(cardLabel(card));
            chip.getStyleClass().add("card-hand-chip");
            if (card == selectedCard) chip.getStyleClass().add("card-hand-chip-selected");
            chip.setOnMouseClicked(event -> {
                clearSelection();
                selectedCard = card;
                renderHandCards(hand);
            });
            row.getChildren().add(chip);
        }
        handCardsBox.getChildren().add(row);
    }

    private String cardLabel(ExperimentCard card) {
        StringBuilder label = new StringBuilder(card.getCardName());
        if (card instanceof DevelopmentCard developmentCard && developmentCard.isNewlyDrawn()) {
            label.append(" [baru]");
        }
        if (card.isSecret()) {
            label.append(" [rahasia]");
        }
        if (card.isPluginCard()) {
            label.append(" [plugin]");
        }
        if (!card.isPlayable() && !(card instanceof VictoryPointCard)) {
            label.append(" (belum bisa)");
        }
        return label.toString();
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
    private void playSelectedCard() {
        if (game == null) {
            dialogs.showError("Game tidak tersedia.");
            return;
        }
        var player = game.getActivePlayer();
        if (player == null) {
            dialogs.showError("Tidak ada pemain aktif.");
            return;
        }

        ExperimentCard cardToPlay = selectedCard;
        if (cardToPlay == null && isSelected(cardPurple)) {
            cardToPlay = findFirstInHand(player.getHandCards(), KnightCard.class);
        } else if (cardToPlay == null && isSelected(cardGreen)) {
            // Cek ada ProgressCard apa saja
            List<ExperimentCard> progressCards = player.getHandCards().stream()
                .filter(c -> c instanceof ProgressCard)
                .toList();
                
            if (progressCards.isEmpty()) {
                cardToPlay = null;
            } else if (progressCards.size() == 1) {
                cardToPlay = progressCards.get(0);
            } else {
                // Ada lebih dari 1, tanya user mana yang mau dipakai (cek tipe unik)
                List<String> types = progressCards.stream().map(c -> c.getClass().getSimpleName()).distinct().toList();
                if (types.size() > 1) {
                    var res = choiceDialogService.choose(
                            "Pilih Progress Card",
                            "Kamu punya lebih dari satu jenis Progress Card.",
                            "Pilih yang ingin digunakan:",
                            types);
                    if (res.isPresent()) {
                        String chosenType = res.get();
                        cardToPlay = progressCards.stream().filter(c -> c.getClass().getSimpleName().equals(chosenType)).findFirst().orElse(null);
                    } else {
                        return; // user cancel
                    }
                } else {
                    cardToPlay = progressCards.get(0);
                }
            }
        } else if (cardToPlay == null && isSelected(cardOrange)) {
            cardToPlay = findFirstInHand(player.getHandCards(), VictoryPointCard.class);
        } else if (cardToPlay == null && isSelected(cardGray)) {
            cardToPlay = findFirstOtherCard(player.getHandCards());
        }

        if (cardToPlay == null) {
            dialogs.showError("Pilih kartu yang valid untuk dimainkan.");
            return;
        }

        if (cardToPlay instanceof VictoryPointCard) {
            handleVictoryPointReveal(player);
            return;
        }

        if (cardToPlay instanceof banana.republic.card.MonopolyCard mc) {
            var res = choiceDialogService.choose(
                    "Pilih Resource",
                    "Monopoly Nimon: Pilih satu resource untuk diambil dari semua pemain.",
                    "Resource:",
                    Arrays.asList(banana.republic.resource.ResourceType.values()));
            if (res.isPresent()) {
                mc.setTargetResource(res.get());
            } else {
                return; // cancel
            }
        }

        var result = cardUiService.playSelectedCard(game, player, cardToPlay);
        if (result.isSuccess()) {
            closeDialog();
        } else {
            dialogs.showError(result.getMessage());
        }
    }

    private void handleVictoryPointReveal(Player player) {
        if (game.getVPTotal(player) < Game.VICTORY_POINTS_TO_WIN) {
            dialogs.showError("Kartu Poin Prestasi Rahasia tetap tersembunyi sampai kamu mencapai 10 Poin Prestasi.");
            return;
        }

        Player winner = gameActionUiService.checkVictory(game);
        if (winner == null) {
            dialogs.showError("Kamu belum mencapai 10 Poin Prestasi.");
            return;
        }

        closeDialog();
        try {
            navigator.showResult(game);
        } catch (Exception e) {
            dialogs.showError("Gagal membuka layar hasil: " + e.getMessage());
        }
    }

    private boolean isSelected(VBox card) {
        return card != null && card.getStyleClass().contains("card-selected");
    }

    private ExperimentCard findFirstInHand(List<ExperimentCard> hand,
                                            Class<? extends ExperimentCard> type) {
        for (ExperimentCard card : hand) {
            if (type.isInstance(card)) {
                return card;
            }
        }
        return null;
    }

    @FXML
    private void togglePurple() {
        clearSelection();
        toggleCard(cardPurple);
    }

    @FXML
    private void toggleOrange() {
        clearSelection();
        toggleCard(cardOrange);
    }

    @FXML
    private void toggleGreen() {
        clearSelection();
        toggleCard(cardGreen);
    }

    @FXML
    private void toggleGray() {
        clearSelection();
        toggleCard(cardGray);
    }

    private void clearSelection() {
        removeSelection(cardPurple);
        removeSelection(cardOrange);
        removeSelection(cardGreen);
        removeSelection(cardGray);
        selectedCard = null;
        if (game != null && game.getActivePlayer() != null) {
            renderHandCards(game.getActivePlayer().getHandCards());
        }
    }

    private void removeSelection(VBox card) {
        if (card != null) {
            card.getStyleClass().remove("card-selected");
        }
    }

    private void toggleCard(VBox card) {
        if (card == null) return;
        if (!card.getStyleClass().contains("card-selected")) {
            card.getStyleClass().add("card-selected");
        }
    }

    private ExperimentCard findFirstOtherCard(List<ExperimentCard> hand) {
        for (ExperimentCard card : hand) {
            if (!(card instanceof KnightCard)
                    && !(card instanceof ProgressCard)
                    && !(card instanceof VictoryPointCard)) {
                return card;
            }
        }
        return null;
    }

}
