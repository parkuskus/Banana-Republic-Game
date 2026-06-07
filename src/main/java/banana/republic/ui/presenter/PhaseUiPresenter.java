package banana.republic.ui.presenter;

import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class PhaseUiPresenter {

    private final Button rollDiceButton;
    private final Button setDiceButton;
    private final Button buildButton;
    private final Button tradeButton;
    private final Button cardButton;
    private final Button declareVictoryButton;
    private final Button settingsButton;
    private final Button endTurnButton;
    private final Button stealButton;
    private final Button discardButton;
    private final Button endGameButton;
    private final Button startSetupOrderButton;
    private final Label conditionLabel;

    public PhaseUiPresenter(Button rollDiceButton,
                            Button setDiceButton,
                            Button buildButton,
                            Button tradeButton,
                            Button cardButton,
                            Button declareVictoryButton,
                            Button settingsButton,
                            Button endTurnButton,
                            Button stealButton,
                            Button discardButton,
                            Button endGameButton,
                            Button startSetupOrderButton,
                            Label conditionLabel) {
        this.rollDiceButton = rollDiceButton;
        this.setDiceButton = setDiceButton;
        this.buildButton = buildButton;
        this.tradeButton = tradeButton;
        this.cardButton = cardButton;
        this.declareVictoryButton = declareVictoryButton;
        this.settingsButton = settingsButton;
        this.endTurnButton = endTurnButton;
        this.stealButton = stealButton;
        this.discardButton = discardButton;
        this.endGameButton = endGameButton;
        this.startSetupOrderButton = startSetupOrderButton;
        this.conditionLabel = conditionLabel;
    }

    public void render(Game game, boolean setupOrderPending, String modeName, boolean discardPending) {
        if (game == null) return;
        GamePhase phase = game.getCurrentPhase();
        boolean isSetup = phase.isSetupPhase();
        boolean isGathering = phase == GamePhase.RESOURCE_GATHERING;
        boolean isGameOver = phase == GamePhase.GAME_OVER;

        setDisabled(rollDiceButton, setupOrderPending || isSetup || !isGathering || isGameOver);
        setDisabled(setDiceButton, setupOrderPending || isSetup || isGameOver);
        setDisabled(buildButton, setupOrderPending || isGathering || isGameOver);
        setDisabled(tradeButton, setupOrderPending || isSetup || isGathering || isGameOver);
        setDisabled(cardButton, setupOrderPending || isSetup || isGameOver);
        setDisabled(declareVictoryButton, setupOrderPending || isSetup || isGameOver);
        setDisabled(endTurnButton, setupOrderPending || isSetup || isGathering || isGameOver);
        setDisabled(settingsButton, isGameOver);
        setDisabled(startSetupOrderButton, !setupOrderPending);
        setDisabled(stealButton, !"ROBBER".equals(modeName));
        setDisabled(discardButton, !discardPending);
        setDisabled(endGameButton, isGameOver);
        renderCondition(game, setupOrderPending, modeName);
    }

    public void renderCondition(Game game, boolean setupOrderPending, String modeName) {
        if (conditionLabel == null || game == null) return;
        if (setupOrderPending) {
            conditionLabel.setText("Fase: Tentukan Urutan");
            return;
        }
        switch (modeName) {
            case "SETTLEMENT" -> conditionLabel.setText("Mode: Build Pos Pantau");
            case "BUILD_OVERLAY" -> conditionLabel.setText("Mode: Build (Pos Pantau / Pipa)");
            case "ROAD" -> conditionLabel.setText("Mode: Build Pipa");
            case "CITY" -> conditionLabel.setText("Mode: Build Lab");
            case "ROBBER" -> conditionLabel.setText("Mode: Pindah Nimon");
            default -> renderPhaseCondition(game.getCurrentPhase());
        }
    }

    private void renderPhaseCondition(GamePhase phase) {
        if (phase == null) conditionLabel.setText("Menunggu...");
        else if (phase.isSetupPhase()) conditionLabel.setText("Fase: Setup Awal");
        else if (phase == GamePhase.RESOURCE_GATHERING) conditionLabel.setText("Fase: Roll Dadu");
        else if (phase == GamePhase.TRADE_BUILD) conditionLabel.setText("Fase: Trade & Build");
        else if (phase == GamePhase.GAME_OVER) conditionLabel.setText("Game Over!");
        else conditionLabel.setText("Menunggu Giliran");
    }

    private void setDisabled(Button button, boolean disabled) {
        if (button != null) button.setDisable(disabled);
    }
}
