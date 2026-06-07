package banana.republic.ui.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.core.TurnOrder;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SetupOrderController {

    private final StackPane overlay;
    private final VBox playersBox;
    private final Label statusLabel;
    private final Button rollButton;
    private final Consumer<DiceResult> diceResultConsumer;

    private Game game;
    private boolean pending;
    private List<Integer> candidates = new ArrayList<>();
    private Map<Integer, DiceResult> rolls = new HashMap<>();
    private int cursor;

    public SetupOrderController(StackPane overlay,
                                VBox playersBox,
                                Label statusLabel,
                                Button rollButton,
                                Consumer<DiceResult> diceResultConsumer) {
        this.overlay = overlay;
        this.playersBox = playersBox;
        this.statusLabel = statusLabel;
        this.rollButton = rollButton;
        this.diceResultConsumer = diceResultConsumer;
    }

    public void initialize(Game game) {
        this.game = game;
        this.pending = detectPending(game);
        if (pending) {
            resetCandidates();
        }
        render();
    }

    public boolean isPending() {
        return pending;
    }

    public void rollCurrentPlayer() {
        if (game == null || !pending) return;
        if (candidates.isEmpty()) {
            resetCandidates();
        }

        int playerIndex = currentPlayerIndex();
        if (playerIndex < 0) return;

        Player player = game.getPlayers().get(playerIndex);
        DiceResult result = game.getDice().roll();
        rolls.put(playerIndex, result);
        if (diceResultConsumer != null) {
            diceResultConsumer.accept(result);
        }
        game.getGameLog().addEntry(
                LogEntry.EventType.SYSTEM,
                player.getName(),
                player.getName() + " melempar urutan: " + result.getDie1() +
                        " + " + result.getDie2() + " = " + result.getTotal()
        );

        cursor++;
        if (cursor >= candidates.size()) {
            resolveRound();
        }
        render();
    }

    public void render() {
        if (overlay != null) {
            overlay.setVisible(pending);
            overlay.setManaged(pending);
            if (pending) overlay.toFront();
        }
        if (!pending) return;
        renderPlayers();
    }

    private boolean detectPending(Game game) {
        if (game == null || game.getCurrentPhase() != GamePhase.SETUP_FIRST_ROUND || game.getSetupSettlementCount() != 0) {
            return false;
        }
        return game.getGameLog().getEntries().stream()
                .map(LogEntry::getMessage)
                .noneMatch(message -> message != null && message.contains("Fase Setup dimulai"));
    }

    private void resetCandidates() {
        candidates = new ArrayList<>();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            candidates.add(i);
        }
        rolls = new HashMap<>();
        cursor = 0;
    }

    private int currentPlayerIndex() {
        if (!pending || candidates.isEmpty() || cursor >= candidates.size()) {
            return -1;
        }
        return candidates.get(cursor);
    }

    private void renderPlayers() {
        if (playersBox == null || game == null) return;
        playersBox.getChildren().clear();

        int currentIndex = currentPlayerIndex();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            DiceResult roll = rolls.get(i);
            String resultText = roll == null ? "belum roll" : roll.getDie1() + " + " + roll.getDie2() + " = " + roll.getTotal();
            Label row = new Label((i + 1) + ". " + player.getName() + " - " +
                    colorName(player.getColor()) + " | " + resultText);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setWrapText(true);
            row.setStyle("-fx-font-size: 14px; -fx-text-fill: #444; -fx-padding: 4 6;");
            if (i == currentIndex) {
                row.setStyle(row.getStyle() + "-fx-font-weight: bold; -fx-background-color: #fff3cd; -fx-background-radius: 6;");
            }
            playersBox.getChildren().add(row);
        }

        if (statusLabel != null) {
            if (currentIndex >= 0) {
                statusLabel.setText("Giliran roll: " + game.getPlayers().get(currentIndex).getName());
            } else {
                statusLabel.setText("Menentukan pemenang roll...");
            }
        }

        if (rollButton != null) {
            rollButton.setText(currentIndex >= 0 ? "Roll untuk " + game.getPlayers().get(currentIndex).getName() : "Lanjut");
            rollButton.setDisable(!pending);
        }
    }

    private void resolveRound() {
        int highest = candidates.stream()
                .map(rolls::get)
                .filter(result -> result != null)
                .mapToInt(DiceResult::getTotal)
                .max()
                .orElse(0);

        List<Integer> winners = candidates.stream()
                .filter(index -> rolls.get(index) != null && rolls.get(index).getTotal() == highest)
                .toList();

        if (winners.size() == 1) {
            int winnerIndex = winners.get(0);
            Player winner = game.getPlayers().get(winnerIndex);
            game.getTurnManager().setActiveIndex(winnerIndex);
            game.getTurnManager().setOrder(TurnOrder.CLOCKWISE);
            game.getGameLog().addEntry(
                    LogEntry.EventType.TURN_CHANGE,
                    winner.getName(),
                    winner.getName() + " memulai pertama (dadu tertinggi: " + highest + ")"
            );
            pending = false;
            if (statusLabel != null) {
                statusLabel.setText(winner.getName() + " memulai pertama.");
            }
            return;
        }

        String names = String.join(", ", winners.stream()
                .map(index -> game.getPlayers().get(index).getName())
                .toList());
        game.getGameLog().addEntry(LogEntry.EventType.SYSTEM,
                "Seri urutan pemain: " + names + ". Roll ulang untuk pemain yang seri.");
        candidates = new ArrayList<>(winners);
        rolls = new HashMap<>();
        cursor = 0;
    }

    private String colorName(PlayerColor color) {
        if (color == null) return "Unknown";
        return switch (color) {
            case RED -> "Red";
            case BLUE -> "Blue";
            case GREEN -> "Green";
            case ORANGE -> "Orange";
            default -> color.name();
        };
    }
}
