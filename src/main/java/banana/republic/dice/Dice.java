package banana.republic.dice;

import java.util.Random;

public class Dice {

    private boolean manualMode;
    private int manualValue1;
    private int manualValue2;
    private final Random random;

    public Dice() {
        this.manualMode = false;
        this.manualValue1 = 1;
        this.manualValue2 = 1;
        this.random = new Random();
    }

    public DiceResult roll() {
        if (manualMode) {
            return new DiceResult(manualValue1, manualValue2);
        }
        int die1 = random.nextInt(6) + 1;
        int die2 = random.nextInt(6) + 1;
        return new DiceResult(die1, die2);
    }

    public void setManualMode(boolean enabled) {
        this.manualMode = enabled;
    }

    public void setManualValues(int die1, int die2) {
        validateDieValue(die1);
        validateDieValue(die2);
        this.manualValue1 = die1;
        this.manualValue2 = die2;
    }

    public boolean isManualMode() {
        return manualMode;
    }

    private void validateDieValue(int value) {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Die value must be between 1 and 6");
        }
    }
}
