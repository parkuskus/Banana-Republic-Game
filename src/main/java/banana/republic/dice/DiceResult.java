package banana.republic.dice;

public class DiceResult {

    private final int die1;
    private final int die2;

    public DiceResult(int die1, int die2) {
        validateDieValue(die1);
        validateDieValue(die2);
        this.die1 = die1;
        this.die2 = die2;
    }

    public int getDie1() {
        return die1;
    }

    public int getDie2() {
        return die2;
    }

    public int getTotal() {
        return die1 + die2;
    }

    public boolean isSeven() {
        return getTotal() == 7;
    }

    private void validateDieValue(int value) {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Die value must be between 1 and 6");
        }
    }
}
