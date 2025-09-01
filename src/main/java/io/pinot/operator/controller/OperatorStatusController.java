package io.pinot.operator.controller;

import io.pinot.operator.api.Pinot;
import io.pinot.operator.api.PinotSchema;
import io.pinot.operator.api.PinotTable;
import io.pinot.operator.api.PinotTenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for monitoring and status of the Pinot Operator
 * 
 * This controller provides HTTP endpoints to monitor the operator's health,
 * view managed resources, and check the overall status of the system.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class OperatorStatusController {

    private final PinotController pinotController;
    private final PinotSchemaController schemaController;
    private final PinotTableController tableController;
    private final PinotTenantController tenantController;

    @Autowired
    public OperatorStatusController(
            PinotController pinotController,
            PinotSchemaController schemaController,
            PinotTableController tableController,
            PinotTenantController tenantController) {
        this.pinotController = pinotController;
        this.schemaController = schemaController;
        this.tableController = tableController;
        this.tenantController = tenantController;
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("operator", "Pinot Kubernetes Operator");
        health.put("version", "0.1.0");
        return ResponseEntity.ok(health);
    }

    /**
     * Get overall operator status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        
        // Get counts of managed resources
        List<Pinot> clusters = pinotController.getManagedClusters();
        List<PinotSchema> schemas = schemaController.getManagedSchemas();
        List<PinotTable> tables = tableController.getManagedTables();
        List<PinotTenant> tenants = tenantController.getManagedTenants();
        
        status.put("clusters", clusters.size());
        status.put("schemas", schemas.size());
        status.put("tables", tables.size());
        status.put("tenants", tenants.size());
        status.put("totalResources", clusters.size() + schemas.size() + tables.size() + tenants.size());
        status.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Get all managed Pinot clusters
     */
    @GetMapping("/clusters")
    public ResponseEntity<List<Pinot>> getClusters() {
        List<Pinot> clusters = pinotController.getManagedClusters();
        return ResponseEntity.ok(clusters);
    }

    /**
     * Get a specific Pinot cluster
     */
    @GetMapping("/clusters/{namespace}/{name}")
    public ResponseEntity<Pinot> getCluster(@PathVariable String namespace, @PathVariable String name) {
        Pinot cluster = pinotController.getManagedCluster(namespace, name);
        if (cluster != null) {
            return ResponseEntity.ok(cluster);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all managed schemas
     */
    @GetMapping("/schemas")
    public ResponseEntity<List<PinotSchema>> getSchemas() {
        List<PinotSchema> schemas = schemaController.getManagedSchemas();
        return ResponseEntity.ok(schemas);
    }

    /**
     * Get a specific schema
     */
    @GetMapping("/schemas/{namespace}/{name}")
    public ResponseEntity<PinotSchema> getSchema(@PathVariable String namespace, @PathVariable String name) {
        PinotSchema schema = schemaController.getManagedSchema(namespace, name);
        if (schema != null) {
            return ResponseEntity.ok(schema);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all managed tables
     */
    @GetMapping("/tables")
    public ResponseEntity<List<PinotTable>> getTables() {
        List<PinotTable> tables = tableController.getManagedTables();
        return ResponseEntity.ok(tables);
    }

    /**
     * Get a specific table
     */
    @GetMapping("/tables/{namespace}/{name}")
    public ResponseEntity<PinotTable> getTable(@PathVariable String namespace, @PathVariable String name) {
        PinotTable table = tableController.getManagedTable(namespace, name);
        if (table != null) {
            return ResponseEntity.ok(table);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all managed tenants
     */
    @GetMapping("/tenants")
    public ResponseEntity<List<PinotTenant>> getTenants() {
        List<PinotTenant> tenants = tenantController.getManagedTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get a specific tenant
     */
    @GetMapping("/tenants/{namespace}/{name}")
    public ResponseEntity<PinotTenant> getTenant(@PathVariable String namespace, @PathVariable String name) {
        PinotTenant tenant = tenantController.getManagedTenant(namespace, name);
        if (tenant != null) {
            return ResponseEntity.ok(tenant);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a resource is being managed
     */
    @GetMapping("/managed/{resourceType}/{namespace}/{name}")
    public ResponseEntity<Map<String, Object>> isManaged(
            @PathVariable String resourceType,
            @PathVariable String namespace,
            @PathVariable String name) {
        
        Map<String, Object> result = new HashMap<>();
        boolean isManaged = false;
        
        switch (resourceType.toLowerCase()) {
            case "cluster":
                isManaged = pinotController.isClusterManaged(namespace, name);
                break;
            case "schema":
                isManaged = schemaController.isSchemaManaged(namespace, name);
                break;
            case "table":
                isManaged = tableController.isTableManaged(namespace, name);
                break;
            case "tenant":
                isManaged = tenantController.isTenantManaged(namespace, name);
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid resource type: " + resourceType));
        }
        
        result.put("resourceType", resourceType);
        result.put("namespace", namespace);
        result.put("name", name);
        result.put("managed", isManaged);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Root endpoint
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Pinot Kubernetes Operator");
        info.put("version", "0.1.0");
        info.put("description", "Kubernetes operator for Apache Pinot");
        info.put("endpoints", Map.of(
            "health", "/api/v1/health",
            "status", "/api/v1/status",
            "clusters", "/api/v1/clusters",
            "schemas", "/api/v1/schemas",
            "tables", "/api/v1/tables",
            "tenants", "/api/v1/tenants"
        ));
        return ResponseEntity.ok(info);
    }
}
