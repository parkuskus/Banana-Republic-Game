package banana.republic.board;

public class NumberToken {

    private final int value;
    private final boolean isRed;

    public NumberToken(int value) {
        if (value < 2 || value > 12 || value == 7) {
            throw new IllegalArgumentException("Token value must be between 2 and 12");
        }
        this.value = value;
        this.isRed = value == 6 || value == 8;
    }

    public int getValue() {
        return value;
    }

    public boolean isRed() {
        return isRed;
    }

    public boolean isHighProbability() {
        return value == 6 || value == 8 || value == 5 || value == 9;
    }

    public int getDotCount() {
        switch (value) {
            case 2:
            case 12:
                return 1;
            case 3:
            case 11:
                return 2;
            case 4:
            case 10:
                return 3;
            case 5:
            case 9:
                return 4;
            case 6:
            case 8:
                return 5;
            default:
                return 0;
        }
    }
}
