package banana.republic.player;


public enum PlayerColor {
    RED("#FF0000"),
    BLUE("#0000FF"),
    ORANGE("#FFA500"),
    WHITE("#FFFFFF");

    private final String hexCode;

    PlayerColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String toHexCode() {
        return hexCode;
    }
}
