package banana.republic.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests untuk GamePhase enum.
 */
@DisplayName("GamePhase Tests")
public class GamePhaseTest {

    @Test
    @DisplayName("GamePhase should not contain ROBBER_PLACEMENT")
    void enumShouldNotContainRobberPlacement() {
        for (GamePhase phase : GamePhase.values()) {
            assertNotEquals("ROBBER_PLACEMENT", phase.name(),
                    "ROBBER_PLACEMENT is dead code and should have been removed");
        }
    }
}
