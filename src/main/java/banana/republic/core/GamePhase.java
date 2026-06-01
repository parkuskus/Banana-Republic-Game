package banana.republic.core;

// Game phases
public enum GamePhase {

    // Pos pantau, clockwise rotation
    SETUP_FIRST_ROUND,

    // taruh node, counter clockwise rotation
    SETUP_SECOND_ROUND,

    // Lempar dadu + resource gathering (or Nimon Ungu if 7)
    RESOURCE_GATHERING,

    // Pindahkan Nimon Ungu (setelah dadu 7)
    ROBBER_PLACEMENT,

    // Dagang/bangun, timer start
    TRADE_BUILD,

    // Game finished
    GAME_OVER;

    public boolean isSetupPhase() {
        return this == SETUP_FIRST_ROUND || this == SETUP_SECOND_ROUND;
    }

    public boolean isActionPhase() { return this == TRADE_BUILD; }
}
