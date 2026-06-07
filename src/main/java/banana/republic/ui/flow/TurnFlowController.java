package banana.republic.ui.flow;

import java.util.function.IntConsumer;

import banana.republic.core.Game;
import banana.republic.dice.DiceResult;

public class TurnFlowController {

    private final Game game;
    private final IntConsumer timerTick;
    private final Runnable refreshUi;
    private final Runnable updatePhaseUi;

    public TurnFlowController(Game game, IntConsumer timerTick, Runnable refreshUi, Runnable updatePhaseUi) {
        this.game = game;
        this.timerTick = timerTick;
        this.refreshUi = refreshUi;
        this.updatePhaseUi = updatePhaseUi;
    }

    public void afterDiceRoll(DiceResult result, Runnable sevenHandler) {
        if (result.isSeven()) {
            game.processDiscardPhase();
            refreshUi.run();
            sevenHandler.run();
        } else {
            game.startTradeBuildTimer(timerTick::accept);
        }
        updatePhaseUi.run();
    }

    public void endTurn(Runnable beforeEndTurn, Runnable afterEndTurn) {
        beforeEndTurn.run();
        game.endTurn();
        refreshUi.run();
        updatePhaseUi.run();
        afterEndTurn.run();
    }

    public static String formatTimer(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
