package banana.republic.ui.flow;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.dice.DiceResult;
import banana.republic.player.Action;
import banana.republic.player.BotPlayer;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class BotTurnController {

    private final Game game;
    private final Consumer<DiceResult> dicePresenter;
    private final IntConsumer timerTick;
    private final Runnable refreshUi;
    private final Runnable updatePhaseUi;

    public BotTurnController(Game game,
                             Consumer<DiceResult> dicePresenter,
                             IntConsumer timerTick,
                             Runnable refreshUi,
                             Runnable updatePhaseUi) {
        this.game = game;
        this.dicePresenter = dicePresenter;
        this.timerTick = timerTick;
        this.refreshUi = refreshUi;
        this.updatePhaseUi = updatePhaseUi;
    }

    public void runIfBotActive(Runnable continueBotTurns) {
        if (game == null) return;
        Player bot = game.getActivePlayer();
        if (bot == null || !bot.isBot()) return;

        PauseTransition thinkDelay = new PauseTransition(Duration.millis(500));
        thinkDelay.setOnFinished(event -> executeBotTurn(bot, continueBotTurns));
        thinkDelay.play();
    }

    private void executeBotTurn(Player bot, Runnable continueBotTurns) {
        List<Action> botActions = getBotActions(bot);
        rollForBot();
        executeBotActions(bot, botActions);
        refreshUi.run();

        PauseTransition endDelay = new PauseTransition(Duration.millis(1000));
        endDelay.setOnFinished(event -> {
            if (game == null) return;
            game.endTurn();
            refreshUi.run();
            updatePhaseUi.run();
            if (game.getActivePlayer() != null && game.getActivePlayer().isBot() && continueBotTurns != null) {
                continueBotTurns.run();
            }
        });
        endDelay.play();
    }

    private List<Action> getBotActions(Player bot) {
        if (bot instanceof BotPlayer botPlayer) {
            try {
                return botPlayer.executeTurn(game.getState());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private void rollForBot() {
        try {
            DiceResult result = game.rollDice();
            dicePresenter.accept(result);
            refreshUi.run();
            if (result.isSeven()) {
                game.processDiscardPhase();
                refreshUi.run();
                moveRobberToFirstAvailableTile();
            }
            game.startTradeBuildTimer(timerTick::accept);
        } catch (Exception ignored) {
            // Bot fallback should not interrupt the UI.
        }
    }

    private void moveRobberToFirstAvailableTile() {
        HexTile target = null;
        for (HexTile tile : game.getBoard().getAllHexTiles()) {
            if (!tile.equals(game.getRobber().getCurrentTile())) {
                target = tile;
                break;
            }
        }
        if (target != null) game.activateRobber(target, null);
    }

    private void executeBotActions(Player bot, List<Action> actions) {
        if (actions == null) return;
        for (Action action : actions) {
            executeBotAction(bot, action);
        }
    }

    private void executeBotAction(Player bot, Action action) {
        if (action == null || bot == null) return;
        try {
            switch (action.getActionType()) {
                case TRADE_MARITIME -> {
                    ResourceType sell = (ResourceType) action.getParameter("sellType");
                    ResourceType buy = (ResourceType) action.getParameter("buyType");
                    if (sell != null && buy != null) game.tradeWithBank(bot, sell, buy);
                }
                case BUY_DEV_CARD -> game.buyDevelopmentCard(bot);
                case BUILD_ROAD -> {
                    Integer pathId = (Integer) action.getParameter("pathId");
                    if (pathId != null) game.getBoard().getPathById(pathId).ifPresent(path -> game.buildRoad(bot, path));
                }
                case BUILD_SETTLEMENT -> {
                    Integer interId = (Integer) action.getParameter("intersectionId");
                    if (interId != null) game.getBoard().getIntersectionById(interId).ifPresent(inter -> game.buildSettlement(bot, inter));
                }
                case BUILD_CITY -> {
                    Integer interId = (Integer) action.getParameter("intersectionId");
                    if (interId != null) game.getBoard().getIntersectionById(interId).ifPresent(inter -> game.buildCity(bot, inter));
                }
                case PLAY_KNIGHT -> {
                    Integer tileId = (Integer) action.getParameter("tileId");
                    Integer victimIdx = (Integer) action.getParameter("victimIndex");
                    if (tileId == null) return;
                    HexTile target = game.getBoard().getHexTileById(tileId).orElse(null);
                    if (target == null) return;
                    Player victim = null;
                    if (victimIdx != null && victimIdx >= 0 && victimIdx < game.getPlayers().size()) {
                        victim = game.getPlayers().get(victimIdx);
                    }
                    game.activateRobber(target, victim);
                }
                case END_TURN -> {
                    // End turn is owned by this controller after all actions are processed.
                }
                default -> {
                    // Unsupported bot actions are ignored by design.
                }
            }
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            // Invalid bot action should not stop the game.
        }
    }
}
