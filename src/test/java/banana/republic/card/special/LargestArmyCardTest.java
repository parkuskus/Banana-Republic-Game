package banana.republic.card.special;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import banana.republic.player.HumanPlayer;
import banana.republic.player.PlayerColor;

@DisplayName("LargestArmyCard Tests")
class LargestArmyCardTest {

    @Test
    @DisplayName("Tie should keep current holder when still qualifying")
    void tieKeepsCurrentHolder() {
        LargestArmyCard card = new LargestArmyCard();
        HumanPlayer first = new HumanPlayer("First", PlayerColor.RED);
        HumanPlayer second = new HumanPlayer("Second", PlayerColor.BLUE);
        first.incrementKnightsPlayed();
        first.incrementKnightsPlayed();
        first.incrementKnightsPlayed();
        second.incrementKnightsPlayed();
        second.incrementKnightsPlayed();
        second.incrementKnightsPlayed();

        card.transfer(first);
        card.evaluate(List.of(first, second));

        assertTrue(card.isActive());
        assertSame(first, card.getHolder());
        assertEquals(3, card.getCurrentQualifyingCount());
    }

    @Test
    @DisplayName("Higher knight count should transfer holder")
    void higherKnightCountTransfersHolder() {
        LargestArmyCard card = new LargestArmyCard();
        HumanPlayer first = new HumanPlayer("First", PlayerColor.RED);
        HumanPlayer second = new HumanPlayer("Second", PlayerColor.BLUE);
        first.incrementKnightsPlayed();
        first.incrementKnightsPlayed();
        first.incrementKnightsPlayed();
        second.incrementKnightsPlayed();
        second.incrementKnightsPlayed();
        second.incrementKnightsPlayed();
        second.incrementKnightsPlayed();

        card.transfer(first);
        card.evaluate(List.of(first, second));

        assertSame(second, card.getHolder());
        assertEquals(4, card.getCurrentQualifyingCount());
    }
}