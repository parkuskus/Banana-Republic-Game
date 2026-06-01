package banana.republic.ui;

import banana.republic.card.ExperimentCard;
import banana.republic.card.KnightCard;
import banana.republic.card.ProgressCard;
import banana.republic.card.VictoryPointCard;
import banana.republic.core.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class CardDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;

    @FXML
    private VBox cardPurple;
    @FXML
    private VBox cardGreen;
    @FXML
    private VBox cardOrange;
    @FXML
    private VBox cardGray;

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
        setCardStatus(cardGray, hand.size());
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
            showError("Game tidak tersedia.");
            return;
        }
        var player = game.getActivePlayer();
        if (player == null) {
            showError("Tidak ada pemain aktif.");
            return;
        }

        ExperimentCard cardToPlay = null;
        if (isSelected(cardPurple)) {
            cardToPlay = findFirstInHand(player.getHandCards(), KnightCard.class);
        } else if (isSelected(cardGreen)) {
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
                    javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(types.get(0), types);
                    dialog.setTitle("Pilih Progress Card");
                    dialog.setHeaderText("Kamu punya lebih dari satu jenis Progress Card.");
                    dialog.setContentText("Pilih yang ingin digunakan:");
                    var res = dialog.showAndWait();
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
        } else if (isSelected(cardOrange)) {
            cardToPlay = findFirstInHand(player.getHandCards(), VictoryPointCard.class);
        }

        if (cardToPlay == null) {
            showError("Pilih kartu yang valid untuk dimainkan.");
            return;
        }

        if (cardToPlay instanceof banana.republic.card.MonopolyCard mc) {
            javafx.scene.control.ChoiceDialog<banana.republic.resource.ResourceType> dialog = new javafx.scene.control.ChoiceDialog<>(banana.republic.resource.ResourceType.WOOD, banana.republic.resource.ResourceType.values());
            dialog.setTitle("Pilih Resource");
            dialog.setHeaderText("Monopoly Nimon: Pilih satu resource untuk diambil dari semua pemain.");
            var res = dialog.showAndWait();
            if (res.isPresent()) {
                mc.setTargetResource(res.get());
            } else {
                return; // cancel
            }
        }

        try {
            game.playCard(player, cardToPlay);
            
            // For Victory Point card, auto-check victory
            if (cardToPlay instanceof VictoryPointCard) {
                if (game.checkVictory() != null) {
                    closeDialog();
                    // Let GameController detect victory automatically, or do nothing as btnDeclareVictory is used
                }
            }
            
            closeDialog();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
