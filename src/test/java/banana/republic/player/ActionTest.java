package banana.republic.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionTest {

    @Test
    void testActionCreation() {
        Action action = new Action(ActionType.BUILD_ROAD);
        assertEquals(ActionType.BUILD_ROAD, action.getActionType());
        assertTrue(action.getParameters().isEmpty());
    }

    @Test
    void testActionNullType() {
        assertThrows(IllegalArgumentException.class, () -> new Action(null));
    }

    @Test
    void testSetAndGetParameter() {
        Action action = new Action(ActionType.BUILD_SETTLEMENT);
        action.setParameter("x", 10);
        assertEquals(10, action.getParameter("x"));
        assertNull(action.getParameter("y"));
    }

    @Test
    void testSetParameterNullKey() {
        Action action = new Action(ActionType.END_TURN);
        assertThrows(IllegalArgumentException.class, () -> action.setParameter(null, "value"));
    }

    @Test
    void testToString() {
        Action action = new Action(ActionType.TRADE_DOMESTIC);
        String s = action.toString();
        assertTrue(s.contains("TRADE_DOMESTIC"));
    }
}
