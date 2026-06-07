package banana.republic.ui.flow;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.ui.board.BoardCoordinateMapper;
import javafx.scene.layout.StackPane;

public class RobberFlowController {

    private final Game game;
    private final Map<StackPane, HexTile> visualToModelTile;
    private final BoardCoordinateMapper coordinateMapper;
    private final Runnable removeOverlay;
    private final Runnable refreshUi;
    private final Runnable updatePhaseUi;
    private final Consumer<List<Player>> openStealDialog;
    private final IntConsumer timerTick;
    private final Consumer<String> errorReporter;

    public RobberFlowController(Game game,
                                Map<StackPane, HexTile> visualToModelTile,
                                BoardCoordinateMapper coordinateMapper,
                                Runnable removeOverlay,
                                Runnable refreshUi,
                                Runnable updatePhaseUi,
                                Consumer<List<Player>> openStealDialog,
                                IntConsumer timerTick,
                                Consumer<String> errorReporter) {
        this.game = game;
        this.visualToModelTile = visualToModelTile;
        this.coordinateMapper = coordinateMapper;
        this.removeOverlay = removeOverlay;
        this.refreshUi = refreshUi;
        this.updatePhaseUi = updatePhaseUi;
        this.openStealDialog = openStealDialog;
        this.timerTick = timerTick;
        this.errorReporter = errorReporter;
    }

    public boolean isCurrentRobberTile(StackPane visualTile) {
        HexTile modelTile = findTile(visualTile);
        return game != null
                && game.getRobber() != null
                && modelTile != null
                && modelTile.equals(game.getRobber().getCurrentTile());
    }

    public void handleRobberClick(StackPane visualTile) {
        if (game == null) return;
        HexTile target = findTile(visualTile);
        if (target == null) return;
        try {
            game.activateRobber(target, null);
            removeOverlay.run();
            refreshUi.run();
            updatePhaseUi.run();

            List<Player> eligible = game.getRobber().getEligibleVictims(game.getActivePlayer(), game.getBoard());
            eligible.remove(game.getActivePlayer());
            if (!eligible.isEmpty()) {
                openStealDialog.accept(eligible);
            } else {
                game.startTradeBuildTimer(timerTick::accept);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            errorReporter.accept(e.getMessage());
        }
    }

    private HexTile findTile(StackPane visualTile) {
        HexTile target = visualToModelTile.get(visualTile);
        if (target == null && game != null) {
            target = coordinateMapper.findHexTileByVisualFallback(game.getBoard(), visualTile, new java.util.HashSet<>());
        }
        return target;
    }
}
