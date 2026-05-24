package banana.republic.card.special;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.player.HumanPlayer;
import banana.republic.player.PlayerColor;

@DisplayName("LongestRoadCard Tests")
class LongestRoadCardTest {

    @Test
    @DisplayName("Tie should keep current holder when still in tied list")
    void tieKeepsCurrentHolder() {
        LongestRoadCard card = new LongestRoadCard();
        HumanPlayer first = new HumanPlayer("First", PlayerColor.RED);
        HumanPlayer second = new HumanPlayer("Second", PlayerColor.BLUE);

        card.transfer(first);
        card.handleTie(List.of(first, second));

        assertTrue(card.isActive());
        assertSame(first, card.getHolder());
    }

    @Test
    @DisplayName("Tie should revoke when holder is not among tied players")
    void tieRevokesWhenHolderMissing() {
        LongestRoadCard card = new LongestRoadCard();
        HumanPlayer first = new HumanPlayer("First", PlayerColor.RED);
        HumanPlayer second = new HumanPlayer("Second", PlayerColor.BLUE);

        card.transfer(first);
        card.handleTie(List.of(second));

        assertFalse(card.isActive());
        assertEquals(null, card.getHolder());
    }
}