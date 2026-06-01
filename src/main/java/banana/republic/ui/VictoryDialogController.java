package banana.republic.ui;

import banana.republic.App;
import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.core.Game;
import banana.republic.core.VictoryPointBreakdown;
import banana.republic.player.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class VictoryDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;

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

        Player winner = game.checkVictory();
        if (winner == null) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Belum Menang");
            alert.setHeaderText(null);
            alert.setContentText("Kamu belum mencapai 10 Poin Prestasi!");
            alert.showAndWait();
            return;
        }

        if (closeHandler != null) {
            closeHandler.run();
        }

        try {
            FXMLLoader loader = App.getLoader("result");
            Parent root = loader.load();
            GameResultController controller = loader.getController();
            controller.setGame(game);
            App.setRootFromLoader(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
