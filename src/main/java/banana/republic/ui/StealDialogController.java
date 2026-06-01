package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.IntConsumer;

public class StealDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;

    @FXML
    private VBox player1;
    @FXML
    private VBox player2;
    @FXML
    private VBox player3;
    @FXML
    private VBox player4;

    @FXML
    private Label player1Name;
    @FXML
    private Label player2Name;
    @FXML
    private Label player3Name;
    @FXML
    private Label player4Name;

    @FXML
    private Label resCount1;
    @FXML
    private Label resCount2;
    @FXML
    private Label resCount3;
    @FXML
    private Label resCount4;

    private List<Player> eligibleVictims;

    private int selectedIndex = -1;

    public void setEligibleVictims(List<Player> victims) {
        this.eligibleVictims = victims;
        populatePlayers();
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    private void populatePlayers() {
        if (game == null) return;

        VBox[] boxes = {player1, player2, player3, player4};
        Label[] nameLabels = {player1Name, player2Name, player3Name, player4Name};
        Label[] resLabels = {resCount1, resCount2, resCount3, resCount4};

        for (VBox box : boxes) {
            if (box != null) {
                box.setVisible(false);
                box.setManaged(false);
            }
        }

        if (eligibleVictims == null) return;
        for (int i = 0; i < eligibleVictims.size() && i < 4; i++) {
            Player p = eligibleVictims.get(i);
            boxes[i].setVisible(true);
            boxes[i].setManaged(true);
            boxes[i].setDisable(false);
            if (nameLabels[i] != null) {
                nameLabels[i].setText(p.getName());
            }
            if (resLabels[i] != null) {
                resLabels[i].setText("Resources: " + p.getTotalResourceCount());
            }
        }

        selectedIndex = -1;
    }

    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @FXML
    private void selectPlayer1() { selectPlayer(0); }
    @FXML
    private void selectPlayer2() { selectPlayer(1); }
    @FXML
    private void selectPlayer3() { selectPlayer(2); }
    @FXML
    private void selectPlayer4() { selectPlayer(3); }

    private void selectPlayer(int idx) {
        VBox[] boxes = {player1, player2, player3, player4};
        for (VBox box : boxes) {
            box.getStyleClass().remove("card-selected");
        }
        if (idx >= 0 && idx < boxes.length && boxes[idx] != null) {
            boxes[idx].getStyleClass().add("card-selected");
            selectedIndex = idx;
        }
    }

    @FXML
    private void confirmSteal() {
        if (game == null || selectedIndex < 0 || eligibleVictims == null) return;
        if (selectedIndex >= eligibleVictims.size()) return;

        Player victim = eligibleVictims.get(selectedIndex);
        Player thief = game.getActivePlayer();
        if (victim.equals(thief)) return;

        try {
            if (victim.getTotalResourceCount() > 0) {
                game.getRobber().stealRandomResource(thief, victim);
                game.getGameLog().addEntry(
                    banana.republic.core.LogEntry.EventType.STEAL,
                    thief.getName(),
                    thief.getName() + " mencuri resource dari " + victim.getName());
            }
        } catch (Exception e) {
            // ignore
        }
        closeDialog();
    }
}
