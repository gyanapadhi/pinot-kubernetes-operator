package io.pinot.operator.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Pinot Custom Resource
 * Verifies the Pinot CRD structure and functionality
 */
class PinotTest {

    @Test
    void pinotResourceCreation() {
        // Test that we can create a Pinot resource
        Pinot pinot = new Pinot();
        assertNotNull(pinot, "Pinot resource should be creatable");
    }

    @Test
    void pinotSpecAndStatus() {
        // Test that spec and status can be set
        Pinot pinot = new Pinot();
        
        Pinot.PinotSpec spec = new Pinot.PinotSpec();
        Pinot.PinotStatus status = new Pinot.PinotStatus();
        
        pinot.setSpec(spec);
        pinot.setStatus(status);
        
        assertNotNull(pinot.getSpec(), "Spec should be set");
        assertNotNull(pinot.getStatus(), "Status should be set");
    }

    @Test
    void pinotConstants() {
        // Test that the constants are correctly defined
        assertEquals("Pinot", Pinot.KIND, "KIND should be 'Pinot'");
        assertEquals("pinot.io", Pinot.GROUP, "GROUP should be 'pinot.io'");
        assertEquals("v1", Pinot.VERSION, "VERSION should be 'v1'");
        assertEquals("pinot.io/v1", Pinot.API_VERSION, "API_VERSION should be 'pinot.io/v1'");
    }
}
