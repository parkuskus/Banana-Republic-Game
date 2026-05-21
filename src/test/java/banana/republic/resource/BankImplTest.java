package banana.republic.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BankImplTest {

    private Bank bank;

    @BeforeEach
    void setUp() {
        bank = new BankImpl();
    }

    @Test
    void testInitialState() {
        for (ResourceType type : ResourceType.values()) {
            assertEquals(19, bank.getCount(type));
            assertTrue(bank.hasResource(type, 19));
            assertFalse(bank.hasResource(type, 20));
        }
    }

    @Test
    void testTakeResource() {
        bank.takeResource(ResourceType.WOOD, 5);
        assertEquals(14, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testTakeAllResource() {
        bank.takeResource(ResourceType.WOOD, 19);
        assertEquals(0, bank.getCount(ResourceType.WOOD));
        assertFalse(bank.hasResource(ResourceType.WOOD, 1));
    }

    @Test
    void testTakeResourceInsufficient() {
        bank.takeResource(ResourceType.WOOD, 19);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bank.takeResource(ResourceType.WOOD, 1));
        assertTrue(ex.getMessage().contains("Insufficient"));
    }

    @Test
    void testTakeResourceNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bank.takeResource(ResourceType.WOOD, -1));
        assertTrue(ex.getMessage().contains("Amount cannot be negative"));
    }

    @Test
    void testReturnResource() {
        bank.takeResource(ResourceType.WOOD, 5);
        bank.returnResource(ResourceType.WOOD, 3);
        assertEquals(17, bank.getCount(ResourceType.WOOD));
    }

    @Test
    void testReturnResourceExceedsMax() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bank.returnResource(ResourceType.WOOD, 1));
        assertTrue(ex.getMessage().contains("Would exceed max capacity"));
    }

    @Test
    void testReturnResourceNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> bank.returnResource(ResourceType.WOOD, -1));
        assertTrue(ex.getMessage().contains("Amount cannot be negative"));
    }

    @Test
    void testCanFulfillAll() {
        Map<ResourceType, Integer> requests = new EnumMap<>(ResourceType.class);
        requests.put(ResourceType.WOOD, 5);
        requests.put(ResourceType.BRICK, 10);
        assertTrue(bank.canFulfillAll(requests));
    }

    @Test
    void testCanFulfillAllInsufficient() {
        Map<ResourceType, Integer> requests = new EnumMap<>(ResourceType.class);
        requests.put(ResourceType.WOOD, 20);
        assertFalse(bank.canFulfillAll(requests));
    }

    @Test
    void testCanFulfillAllNullOrEmpty() {
        assertTrue(bank.canFulfillAll(null));
        assertTrue(bank.canFulfillAll(new EnumMap<>(ResourceType.class)));
    }

    @Test
    void testCanFulfillAllNegativeAmount() {
        Map<ResourceType, Integer> requests = new EnumMap<>(ResourceType.class);
        requests.put(ResourceType.WOOD, -1);
        assertFalse(bank.canFulfillAll(requests));
    }

    @Test
    void testCanFulfillAllNullValue() {
        Map<ResourceType, Integer> requests = new EnumMap<>(ResourceType.class);
        requests.put(ResourceType.WOOD, null);
        assertFalse(bank.canFulfillAll(requests));
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        // Simulate multiple threads taking from bank
        // Total attempts = 20, bank only has 19
        // With proper synchronization, final count should be >= 0
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    bank.takeResource(ResourceType.WOOD, 1);
                } catch (IllegalArgumentException ignored) {
                    // Expected when bank runs out
                }
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    bank.takeResource(ResourceType.WOOD, 1);
                } catch (IllegalArgumentException ignored) {
                    // Expected when bank runs out
                }
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        // Final count should never go below 0 with proper synchronization
        assertTrue(bank.getCount(ResourceType.WOOD) >= 0,
            "Bank count should not go below 0, but was: " + bank.getCount(ResourceType.WOOD));
    }
}
