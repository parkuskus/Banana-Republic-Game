package banana.republic.ui;

import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.core.VictoryPointBreakdown;
import banana.republic.player.Player;
import banana.republic.ui.command.GameActionUiService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class VictoryDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;
    private final UiDialogs dialogs = new UiDialogs();
    private final UiNavigator navigator = new AppUiNavigator();
    private final GameActionUiService gameActionUiService = new GameActionUiService();

    @FXML
    private Label publicPointsLabel;
    @FXML
    private Label secretPointsLabel;
    @FXML
    private VBox revealedCardsContainer;
    @FXML
    private HBox knightCardRow;
    @FXML
    private HBox progressCardRow;
    @FXML
    private HBox vpCardRow;

    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
        populateVictoryData();
    }

    private void populateVictoryData() {
        if (game == null) return;
        Player active = game.getActivePlayer();
        if (active == null) return;

        VictoryPointBreakdown vp = game.getVPBreakdown(active);
        int publicVP = vp.getPublicTotal();
        int secretVP = vp.getVictoryCardPoints();

        if (publicPointsLabel != null) {
            publicPointsLabel.setText(String.valueOf(publicVP));
        }
        if (secretPointsLabel != null) {
            secretPointsLabel.setText(String.valueOf(secretVP));
        }

        List<ExperimentCard> hand = active.getHandCards();
        boolean hasKnight = hand.stream().anyMatch(c -> c.getCardType() == CardType.KNIGHT);
        boolean hasProgress = hand.stream().anyMatch(c ->
            c.getCardType() == CardType.ROAD_BUILDING || c.getCardType() == CardType.MONOPOLY);
        boolean hasVP = hand.stream().anyMatch(c -> c.getCardType() == CardType.VICTORY_POINT);

        if (knightCardRow != null) {
            knightCardRow.setVisible(hasKnight);
            knightCardRow.setManaged(hasKnight);
        }
        if (progressCardRow != null) {
            progressCardRow.setVisible(hasProgress);
            progressCardRow.setManaged(hasProgress);
        }
        if (vpCardRow != null) {
            vpCardRow.setVisible(hasVP);
            vpCardRow.setManaged(hasVP);
        }
    }

    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @FXML
    private void handleRevealAndWin() {
        if (game == null) return;

        Player winner = gameActionUiService.checkVictory(game);
        if (winner == null) {
            dialogs.showError("Kamu belum mencapai 10 Poin Prestasi!");
            return;
        }

        if (closeHandler != null) {
            closeHandler.run();
        }

        try {
            navigator.showResult(game);
        } catch (Exception e) {
            dialogs.showError("Gagal membuka layar hasil: " + e.getMessage());
        }
    }
}
