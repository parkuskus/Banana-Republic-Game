package banana.republic.player;


public enum PlayerColor {
    RED("#c21a09"),
    BLUE("#305cde"),
    ORANGE("#ff7f00"),
    GREEN("#4fc978");

    private final String hexCode;

    PlayerColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String toHexCode() {
        return hexCode;
    }
}
