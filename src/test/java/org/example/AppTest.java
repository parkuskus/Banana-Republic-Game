package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppTest {
    @Test
    void testAppInitialization() {
        App app = new App();
        assertNotNull(app);
    }
}
