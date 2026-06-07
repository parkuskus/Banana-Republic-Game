package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.ui.command.StealUiService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.IntConsumer;

public class StealDialogController implements DialogController, GameAwareController {

    private Runnable closeHandler;
    private Game game;
    private final StealUiService stealUiService = new StealUiService();

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
    private Label player1Avatar;
    @FXML
    private Label player2Avatar;
    @FXML
    private Label player3Avatar;
    @FXML
    private Label player4Avatar;

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
        Label[] avatarLabels = {player1Avatar, player2Avatar, player3Avatar, player4Avatar};

        for (VBox box : boxes) {
            if (box != null) {
                box.setVisible(false);
                box.setManaged(false);
                box.getStyleClass().removeAll("side-card-red", "side-card-blue", "side-card-green", "side-card-orange");
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
            boxes[i].getStyleClass().add(sideCardStyleClass(p.getColor()));
            if (avatarLabels[i] != null) {
                avatarLabels[i].setText(initial(p));
                avatarLabels[i].getStyleClass().removeAll("avatar-red", "avatar-blue", "avatar-green", "avatar-orange");
                avatarLabels[i].getStyleClass().add(avatarStyleClass(p.getColor()));
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

        stealUiService.steal(game, thief, victim);
        closeDialog();
    }

    private String initial(Player player) {
        if (player == null || player.getName() == null || player.getName().isBlank()) return "P";
        return player.getName().trim().substring(0, 1).toUpperCase();
    }

    private String sideCardStyleClass(PlayerColor color) {
        if (color == null) return "side-card-blue";
        return switch (color) {
            case RED -> "side-card-red";
            case BLUE -> "side-card-blue";
            case GREEN -> "side-card-green";
            case ORANGE -> "side-card-orange";
            default -> "side-card-blue";
        };
    }

    private String avatarStyleClass(PlayerColor color) {
        if (color == null) return "avatar-blue";
        return switch (color) {
            case RED -> "avatar-red";
            case BLUE -> "avatar-blue";
            case GREEN -> "avatar-green";
            case ORANGE -> "avatar-orange";
            default -> "avatar-blue";
        };
    }
}
