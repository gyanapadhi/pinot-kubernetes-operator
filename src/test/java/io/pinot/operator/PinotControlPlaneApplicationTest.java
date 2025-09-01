package io.pinot.operator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PinotControlPlaneApplication
 * Verifies that the Spring Boot application context can load successfully
 */
@SpringBootTest
@ActiveProfiles("test")
class PinotControlPlaneApplicationTest {

    @Test
    void contextLoads() {
        // This test will pass if the Spring application context loads successfully
        assertTrue(true, "Application context should load without errors");
    }

    @Test
    void applicationStarts() {
        // Basic test to ensure the application class exists and can be instantiated
        PinotControlPlaneApplication app = new PinotControlPlaneApplication();
        assertNotNull(app, "Application should be instantiable");
    }
}
