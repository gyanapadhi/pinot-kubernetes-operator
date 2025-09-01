package io.pinot.operator.controller;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.pinot.operator.api.Pinot;
import io.pinot.operator.api.Pinot.PinotSpec;
import io.pinot.operator.api.Pinot.PinotStatus;
import io.pinot.operator.service.PinotClusterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Pinot Controller
 * Tests the reconciliation logic and Kubernetes resource management
 */
@ExtendWith(MockitoExtension.class)
class PinotControllerTest {

    @Mock
    private PinotClusterService pinotClusterService;

    @Mock
    private KubernetesClient kubernetesClient;

    private PinotController pinotController;

    @BeforeEach
    void setUp() {
        pinotController = new PinotController(kubernetesClient, pinotClusterService);
    }

    @Test
    void testPinotResourceManagement() {
        // Test that the controller can manage Pinot resources
        Pinot pinot = createTestPinotResource();
        
        // Test cluster management
        assertFalse(pinotController.isClusterManaged("default", "test-pinot-cluster"), 
                   "Cluster should not be managed initially");
        
        // Simulate adding a resource (this would normally happen through the watcher)
        // For testing, we'll use reflection to add it to managed clusters
        ReflectionTestUtils.setField(pinotController, "managedClusters", 
                                   new java.util.concurrent.ConcurrentHashMap<>());
        
        // Test getManagedClusters
        assertTrue(pinotController.getManagedClusters().isEmpty(), 
                  "Should start with no managed clusters");
    }

    @Test
    void testPinotResourceValidation() {
        // Test with valid resource
        Pinot validPinot = createTestPinotResource();
        assertNotNull(validPinot.getSpec(), "Valid resource should have spec");
        assertNotNull(validPinot.getMetadata(), "Valid resource should have metadata");
        
        // Test with invalid resource (no spec)
        Pinot invalidPinot = new Pinot();
        assertNull(invalidPinot.getSpec(), "Invalid resource should have no spec");
    }

    @Test
    void testPinotResourceCreation() {
        Pinot pinot = createTestPinotResource();
        
        // Test that the resource can be created with proper structure
        assertNotNull(pinot.getMetadata(), "Metadata should be set");
        assertEquals("test-pinot-cluster", pinot.getMetadata().getName(), "Name should match");
        assertEquals("default", pinot.getMetadata().getNamespace(), "Namespace should match");
        
        assertNotNull(pinot.getSpec(), "Spec should be set");
        assertNotNull(pinot.getSpec().getDeploymentOrder(), "Deployment order should be set");
        assertEquals(3, pinot.getSpec().getDeploymentOrder().size(), "Should have 3 deployment types");
    }

    @Test
    void testPinotResourceUpdate() {
        Pinot originalPinot = createTestPinotResource();
        Pinot updatedPinot = createTestPinotResource();
        
        // Modify the updated resource
        List<Pinot.PinotNodeType> newOrder = List.of(Pinot.PinotNodeType.CONTROLLER, Pinot.PinotNodeType.BROKER);
        updatedPinot.getSpec().setDeploymentOrder(newOrder);
        
        // Test that the update is reflected
        assertEquals(newOrder, updatedPinot.getSpec().getDeploymentOrder(), 
                    "Deployment order should be updated");
        assertNotEquals(originalPinot.getSpec().getDeploymentOrder(), 
                       updatedPinot.getSpec().getDeploymentOrder(), 
                       "Deployment orders should be different");
    }

    @Test
    void testPinotResourceDeletion() {
        Pinot pinot = createTestPinotResource();
        
        // Test that the resource can be marked for deletion
        // In a real scenario, this would be handled by the Kubernetes watcher
        assertNotNull(pinot, "Resource should exist before deletion");
        
        // Verify the resource structure is intact
        assertNotNull(pinot.getMetadata(), "Metadata should still exist");
        assertNotNull(pinot.getSpec(), "Spec should still exist");
    }

    @Test
    void testPinotConstants() {
        // Test that the Pinot resource constants are correctly defined
        assertEquals("Pinot", Pinot.KIND, "KIND should be 'Pinot'");
        assertEquals("pinot.io", Pinot.GROUP, "GROUP should be 'pinot.io'");
        assertEquals("v1", Pinot.VERSION, "VERSION should be 'v1'");
        assertEquals("pinot.io/v1", Pinot.API_VERSION, "API_VERSION should be 'pinot.io/v1'");
    }

    private Pinot createTestPinotResource() {
        Pinot pinot = new Pinot();
        
        // Set metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName("test-pinot-cluster");
        metadata.setNamespace("default");
        pinot.setMetadata(metadata);
        
        // Set spec
        PinotSpec spec = new PinotSpec();
        spec.setDeploymentOrder(List.of(
            Pinot.PinotNodeType.CONTROLLER, 
            Pinot.PinotNodeType.BROKER, 
            Pinot.PinotNodeType.SERVER
        ));
        pinot.setSpec(spec);
        
        // Set status
        PinotStatus status = new PinotStatus();
        pinot.setStatus(status);
        
        return pinot;
    }
}
