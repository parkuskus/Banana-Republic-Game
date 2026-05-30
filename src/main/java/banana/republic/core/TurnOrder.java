package banana.republic.core;

/**
 * Arah urutan giliran pemain di Banana Republic. Pada giliran normal, urutan
 * selalu clockwise.
 */
public enum TurnOrder {

    CLOCKWISE("Searah Jarum Jam"),

    COUNTER_CLOCKWISE("Berlawanan Arah Jarum Jam");

    private final String displayName;

    TurnOrder(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    /**
     * Mengembalikan arah berlawanan, untuk transisi antara putaran pertama dan
     * putaran kedua fase setup.
     */
    public TurnOrder reversed() {
        return this == CLOCKWISE ? COUNTER_CLOCKWISE : CLOCKWISE;
    }
}
