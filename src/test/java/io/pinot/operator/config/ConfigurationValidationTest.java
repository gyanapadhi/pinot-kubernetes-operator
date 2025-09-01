package io.pinot.operator.config;

import io.pinot.operator.api.Pinot;
import io.pinot.operator.api.Pinot.PinotSpec;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Test class for Configuration Validation
 * Tests input validation, error scenarios, and configuration constraints
 */
class ConfigurationValidationTest {

    @Test
    void testValidPinotConfiguration() {
        Pinot validPinot = createValidPinotResource();
        
        // Test that valid configuration passes validation
        assertTrue(isValidConfiguration(validPinot), "Valid configuration should pass validation");
    }

    @Test
    void testInvalidPinotConfiguration() {
        // Test with missing metadata
        Pinot invalidPinot1 = new Pinot();
        assertFalse(isValidConfiguration(invalidPinot1), "Configuration without metadata should fail");
        
        // Test with missing name
        Pinot invalidPinot2 = new Pinot();
        ObjectMeta metadata2 = new ObjectMeta();
        metadata2.setNamespace("default");
        invalidPinot2.setMetadata(metadata2);
        assertFalse(isValidConfiguration(invalidPinot2), "Configuration without name should fail");
        
        // Test with missing namespace
        Pinot invalidPinot3 = new Pinot();
        ObjectMeta metadata3 = new ObjectMeta();
        metadata3.setName("test-cluster");
        invalidPinot3.setMetadata(metadata3);
        assertFalse(isValidConfiguration(invalidPinot3), "Configuration without namespace should fail");
        
        // Test with missing spec
        Pinot invalidPinot4 = new Pinot();
        ObjectMeta metadata4 = new ObjectMeta();
        metadata4.setName("test-cluster");
        metadata4.setNamespace("default");
        invalidPinot4.setMetadata(metadata4);
        assertFalse(isValidConfiguration(invalidPinot4), "Configuration without spec should fail");
    }

    @Test
    void testDeploymentOrderValidation() {
        Pinot pinot = createValidPinotResource();
        
        // Test valid deployment order
        List<Pinot.PinotNodeType> validOrder = List.of(
            Pinot.PinotNodeType.CONTROLLER,
            Pinot.PinotNodeType.BROKER,
            Pinot.PinotNodeType.SERVER
        );
        pinot.getSpec().setDeploymentOrder(validOrder);
        assertTrue(isValidDeploymentOrder(validOrder), "Valid deployment order should pass validation");
        
        // Test empty deployment order
        List<Pinot.PinotNodeType> emptyOrder = List.of();
        assertFalse(isValidDeploymentOrder(emptyOrder), "Empty deployment order should fail validation");
        
        // Test null deployment order
        assertFalse(isValidDeploymentOrder(null), "Null deployment order should fail validation");
        
        // Test deployment order without controller
        List<Pinot.PinotNodeType> invalidOrder = List.of(
            Pinot.PinotNodeType.BROKER,
            Pinot.PinotNodeType.SERVER
        );
        assertFalse(isValidDeploymentOrder(invalidOrder), "Deployment order without controller should fail validation");
    }

    @Test
    void testNodeTypeValidation() {
        // Test all valid node types
        assertTrue(isValidNodeType(Pinot.PinotNodeType.CONTROLLER), "Controller should be valid");
        assertTrue(isValidNodeType(Pinot.PinotNodeType.BROKER), "Broker should be valid");
        assertTrue(isValidNodeType(Pinot.PinotNodeType.SERVER), "Server should be valid");
        assertTrue(isValidNodeType(Pinot.PinotNodeType.MINION), "Minion should be valid");
    }

    @Test
    void testResourceNameValidation() {
        // Test valid names
        assertTrue(isValidResourceName("pinot-cluster"), "Valid name should pass validation");
        assertTrue(isValidResourceName("pinot-cluster-123"), "Valid name with numbers should pass validation");
        assertTrue(isValidResourceName("pinot-cluster-test"), "Valid name with hyphens should pass validation");
        
        // Test invalid names
        assertFalse(isValidResourceName(""), "Empty name should fail validation");
        assertFalse(isValidResourceName(null), "Null name should fail validation");
        assertFalse(isValidResourceName("Pinot-Cluster"), "Name with uppercase should fail validation");
        assertFalse(isValidResourceName("pinot_cluster"), "Name with underscore should fail validation");
        assertFalse(isValidResourceName("pinot.cluster"), "Name with dot should fail validation");
    }

    @Test
    void testNamespaceValidation() {
        // Test valid namespaces
        assertTrue(isValidNamespace("default"), "Default namespace should be valid");
        assertTrue(isValidNamespace("pinot-system"), "Custom namespace should be valid");
        assertTrue(isValidNamespace("pinot-system-123"), "Namespace with numbers should be valid");
        
        // Test invalid namespaces
        assertFalse(isValidNamespace(""), "Empty namespace should fail validation");
        assertFalse(isValidNamespace(null), "Null namespace should fail validation");
        assertFalse(isValidNamespace("Default"), "Namespace with uppercase should fail validation");
        assertFalse(isValidNamespace("pinot.system"), "Namespace with dot should fail validation");
    }

    @Test
    void testConfigurationConstraints() {
        Pinot pinot = createValidPinotResource();
        
        // Test minimum deployment order length
        List<Pinot.PinotNodeType> minOrder = List.of(Pinot.PinotNodeType.CONTROLLER);
        pinot.getSpec().setDeploymentOrder(minOrder);
        assertTrue(isValidConfiguration(pinot), "Configuration with minimum deployment order should be valid");
        
        // Test maximum deployment order length (reasonable limit)
        List<Pinot.PinotNodeType> maxOrder = List.of(
            Pinot.PinotNodeType.CONTROLLER,
            Pinot.PinotNodeType.BROKER,
            Pinot.PinotNodeType.SERVER,
            Pinot.PinotNodeType.MINION,
            Pinot.PinotNodeType.CONTROLLER, // Duplicate for testing
            Pinot.PinotNodeType.BROKER      // Duplicate for testing
        );
        pinot.getSpec().setDeploymentOrder(maxOrder);
        assertTrue(isValidConfiguration(pinot), "Configuration with maximum deployment order should be valid");
    }

    // Helper methods for validation
    private boolean isValidConfiguration(Pinot pinot) {
        if (pinot == null) return false;
        
        ObjectMeta metadata = pinot.getMetadata();
        if (metadata == null || metadata.getName() == null || metadata.getNamespace() == null) {
            return false;
        }
        
        PinotSpec spec = pinot.getSpec();
        if (spec == null || spec.getDeploymentOrder() == null || spec.getDeploymentOrder().isEmpty()) {
            return false;
        }
        
        return isValidResourceName(metadata.getName()) && 
               isValidNamespace(metadata.getNamespace()) &&
               isValidDeploymentOrder(spec.getDeploymentOrder());
    }

    private boolean isValidDeploymentOrder(List<Pinot.PinotNodeType> deploymentOrder) {
        if (deploymentOrder == null || deploymentOrder.isEmpty()) {
            return false;
        }
        
        // Must contain at least a controller
        return deploymentOrder.contains(Pinot.PinotNodeType.CONTROLLER);
    }

    private boolean isValidNodeType(Pinot.PinotNodeType nodeType) {
        return nodeType != null;
    }

    private boolean isValidResourceName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Kubernetes naming convention: lowercase alphanumeric characters, '-', and '.'
        return name.matches("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    }

    private boolean isValidNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return false;
        }
        
        // Kubernetes namespace naming convention
        return namespace.matches("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    }

    private Pinot createValidPinotResource() {
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
        
        return pinot;
    }
}
