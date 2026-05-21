package banana.republic.resource;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTypeTest {

    @Test
    void testDisplayNames() {
        assertEquals("Kayu", ResourceType.WOOD.getDisplayName());
        assertEquals("Batu Bata", ResourceType.BRICK.getDisplayName());
        assertEquals("Gandum", ResourceType.WHEAT.getDisplayName());
        assertEquals("Bijih", ResourceType.ORE.getDisplayName());
        assertEquals("Pisang", ResourceType.BANANA.getDisplayName());
    }

    @Test
    void testEmojis() {
        assertEquals("🪵", ResourceType.WOOD.getEmoji());
        assertEquals("🧱", ResourceType.BRICK.getEmoji());
        assertEquals("🌾", ResourceType.WHEAT.getEmoji());
        assertEquals("⛏", ResourceType.ORE.getEmoji());
        assertEquals("🍌", ResourceType.BANANA.getEmoji());
    }

    @Test
    void testAllValuesPresent() {
        assertEquals(5, ResourceType.values().length);
    }
}
