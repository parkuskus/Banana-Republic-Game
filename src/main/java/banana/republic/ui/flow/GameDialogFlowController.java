package banana.republic.ui.flow;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.ui.DiscardDialogController;
import banana.republic.ui.StealDialogController;
import banana.republic.ui.dialog.DialogHostService;
import javafx.scene.layout.StackPane;

public class GameDialogFlowController {

    private final DialogHostService dialogHostService;
    private final Game game;
    private final StackPane tradeOverlay;
    private final StackPane cardOverlay;
    private final StackPane stealOverlay;
    private final StackPane settingsOverlay;
    private final StackPane victoryOverlay;
    private final StackPane discardOverlay;
    private final Runnable refreshUi;
    private final Runnable enterRobberMode;
    private final IntConsumer timerTick;
    private final Consumer<String> errorReporter;
    private final Queue<Player> humanDiscardQueue = new LinkedList<>();

    public GameDialogFlowController(DialogHostService dialogHostService,
                                    Game game,
                                    StackPane tradeOverlay,
                                    StackPane cardOverlay,
                                    StackPane stealOverlay,
                                    StackPane settingsOverlay,
                                    StackPane victoryOverlay,
                                    StackPane discardOverlay,
                                    Runnable refreshUi,
                                    Runnable enterRobberMode,
                                    IntConsumer timerTick,
                                    Consumer<String> errorReporter) {
        this.dialogHostService = dialogHostService;
        this.game = game;
        this.tradeOverlay = tradeOverlay;
        this.cardOverlay = cardOverlay;
        this.stealOverlay = stealOverlay;
        this.settingsOverlay = settingsOverlay;
        this.victoryOverlay = victoryOverlay;
        this.discardOverlay = discardOverlay;
        this.refreshUi = refreshUi;
        this.enterRobberMode = enterRobberMode;
        this.timerTick = timerTick;
        this.errorReporter = errorReporter;
    }

    public void openCard() {
        open("card", cardOverlay);
    }

    public void openTrade() {
        open("trade", tradeOverlay);
    }

    public void openSettings() {
        open("settings", settingsOverlay);
    }

    public void openVictory() {
        open("victory", victoryOverlay);
    }

    public void openSteal() {
        open("steal", stealOverlay);
    }

    public void openDiscard() {
        open("discard", discardOverlay);
    }

    public void openSteal(List<Player> eligibleVictims) {
        if (stealOverlay == null) return;
        try {
            Object controller = dialogHostService.open("steal", stealOverlay, game, null, () -> {
                refreshUi.run();
                game.startTradeBuildTimer(timerTick::accept);
            });
            if (controller instanceof StealDialogController stealController) {
                stealController.setEligibleVictims(eligibleVictims);
            }
        } catch (IOException e) {
            errorReporter.accept("Gagal membuka dialog steal.");
        }
    }

    public void queueDiscardsAndShow(Collection<Player> players) {
        humanDiscardQueue.clear();
        humanDiscardQueue.addAll(players);
        showNextDiscardDialog();
    }

    public boolean hasPendingDiscards() {
        return !humanDiscardQueue.isEmpty();
    }

    private void showNextDiscardDialog() {
        if (humanDiscardQueue.isEmpty()) {
            enterRobberMode.run();
            return;
        }
        Player player = humanDiscardQueue.poll();
        try {
            if (discardOverlay == null) return;
            Object controller = dialogHostService.open("discard", discardOverlay, game, null, () -> {
                refreshUi.run();
                showNextDiscardDialog();
            });
            if (controller instanceof DiscardDialogController discardController) {
                discardController.setDiscardingPlayer(player);
            }
        } catch (IOException e) {
            errorReporter.accept("Gagal membuka dialog discard.");
        }
    }

    private void open(String fxmlName, StackPane overlay) {
        try {
            dialogHostService.open(fxmlName, overlay, game, null, refreshUi);
        } catch (IOException e) {
            errorReporter.accept("Gagal membuka dialog " + fxmlName + ".");
        }
    }
}
