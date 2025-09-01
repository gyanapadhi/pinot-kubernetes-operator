package io.pinot.operator.controller;

import io.pinot.operator.api.PinotSchema;
import io.pinot.operator.service.PinotSchemaService;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Controller for managing Pinot schemas in Kubernetes
 * 
 * This controller watches for PinotSchema custom resources and manages
 * their lifecycle, including creation, updates, and deletion.
 */
@Component
public class PinotSchemaController {

    private static final Logger logger = LoggerFactory.getLogger(PinotSchemaController.class);
    
    private final KubernetesClient kubernetesClient;
    private final PinotSchemaService pinotSchemaService;
    private final ConcurrentMap<String, PinotSchema> managedSchemas = new ConcurrentHashMap<>();
    
    private Watcher<PinotSchema> schemaWatcher;

    @Autowired
    public PinotSchemaController(KubernetesClient kubernetesClient, PinotSchemaService pinotSchemaService) {
        this.kubernetesClient = kubernetesClient;
        this.pinotSchemaService = pinotSchemaService;
        initializeWatcher();
    }

    /**
     * Initialize the Kubernetes watcher for PinotSchema resources
     */
    private void initializeWatcher() {
        try {
            schemaWatcher = new Watcher<PinotSchema>() {
                @Override
                public void eventReceived(Action action, PinotSchema resource) {
                    handleSchemaEvent(action, resource);
                }

                @Override
                public void onClose(WatcherException cause) {
                    logger.error("PinotSchema watcher closed", cause);
                    // Reinitialize watcher after delay
                    try {
                        Thread.sleep(5000);
                        initializeWatcher();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            // Start watching PinotSchema resources across all namespaces
            kubernetesClient.resources(PinotSchema.class)
                    .inAnyNamespace()
                    .watch(schemaWatcher);

            logger.info("PinotSchema watcher initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize PinotSchema watcher", e);
        }
    }

    /**
     * Handle PinotSchema resource events
     */
    private void handleSchemaEvent(Watcher.Action action, PinotSchema resource) {
        String resourceKey = getResourceKey(resource);
        
        try {
            switch (action) {
                case ADDED:
                    handleSchemaAdded(resource);
                    break;
                case MODIFIED:
                    handleSchemaModified(resource);
                    break;
                case DELETED:
                    handleSchemaDeleted(resource);
                    break;
                case ERROR:
                    logger.error("Error event received for PinotSchema resource: {}", resourceKey);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling PinotSchema event: {} for resource: {}", action, resourceKey, e);
        }
    }

    /**
     * Handle PinotSchema resource addition
     */
    private void handleSchemaAdded(PinotSchema resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotSchema resource added: {}", resourceKey);
        
        managedSchemas.put(resourceKey, resource);
        pinotSchemaService.createOrUpdateSchema(resource);
    }

    /**
     * Handle PinotSchema resource modification
     */
    private void handleSchemaModified(PinotSchema resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotSchema resource modified: {}", resourceKey);
        
        PinotSchema existingResource = managedSchemas.get(resourceKey);
        if (existingResource != null) {
            managedSchemas.put(resourceKey, resource);
            pinotSchemaService.createOrUpdateSchema(resource);
        }
    }

    /**
     * Handle PinotSchema resource deletion
     */
    private void handleSchemaDeleted(PinotSchema resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotSchema resource deleted: {}", resourceKey);
        
        managedSchemas.remove(resourceKey);
        pinotSchemaService.deleteSchema(resource);
    }

    /**
     * Get a unique key for the resource
     */
    private String getResourceKey(PinotSchema resource) {
        ObjectMeta metadata = resource.getMetadata();
        if (metadata != null && metadata.getNamespace() != null) {
            return metadata.getNamespace() + "/" + metadata.getName();
        }
        return metadata != null ? metadata.getName() : "unknown";
    }

    /**
     * Periodic reconciliation of managed schemas
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reconcileSchemas() {
        logger.debug("Starting periodic reconciliation of {} managed schemas", managedSchemas.size());
        
        for (PinotSchema schema : managedSchemas.values()) {
            try {
                pinotSchemaService.reconcileSchema(schema);
            } catch (Exception e) {
                logger.error("Error reconciling schema: {}", getResourceKey(schema), e);
            }
        }
    }

    /**
     * Get list of managed schemas
     */
    public List<PinotSchema> getManagedSchemas() {
        return List.copyOf(managedSchemas.values());
    }

    /**
     * Get a specific managed schema
     */
    public PinotSchema getManagedSchema(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedSchemas.get(key);
    }

    /**
     * Check if a schema is being managed
     */
    public boolean isSchemaManaged(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedSchemas.containsKey(key);
    }
}
