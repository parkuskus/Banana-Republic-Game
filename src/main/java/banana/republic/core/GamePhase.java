package banana.republic.core;

/**
 * Fase-fase permainan Banana Republic.
 */
public enum GamePhase {

    /** Penempatan Pos Pantau awal, arah clockwise. */
    SETUP_FIRST_ROUND,

    /** Penempatan Pos Pantau kedua, arah counter-clockwise. */
    SETUP_SECOND_ROUND,

    /** Lempar dadu dan distribusi resource (atau Nimon Ungu jika dadu 7). */
    RESOURCE_GATHERING,

    // Pindahkan Nimon Ungu (setelah dadu 7)
    ROBBER_PLACEMENT,

    /** Fase dagang dan bangun dengan timer aktif. */
    TRADE_BUILD,

    /** Permainan selesai. */
    GAME_OVER;

    public boolean isSetupPhase() {
        return this == SETUP_FIRST_ROUND || this == SETUP_SECOND_ROUND;
    }

    public boolean isActionPhase() { return this == TRADE_BUILD; }
}
