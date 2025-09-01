package io.pinot.operator.controller;

import io.pinot.operator.api.PinotTable;
import io.pinot.operator.service.PinotTableService;
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
 * Controller for managing Pinot tables in Kubernetes
 * 
 * This controller watches for PinotTable custom resources and manages
 * their lifecycle, including creation, updates, and deletion.
 */
@Component
public class PinotTableController {

    private static final Logger logger = LoggerFactory.getLogger(PinotTableController.class);
    
    private final KubernetesClient kubernetesClient;
    private final PinotTableService pinotTableService;
    private final ConcurrentMap<String, PinotTable> managedTables = new ConcurrentHashMap<>();
    
    private Watcher<PinotTable> tableWatcher;

    @Autowired
    public PinotTableController(KubernetesClient kubernetesClient, PinotTableService pinotTableService) {
        this.kubernetesClient = kubernetesClient;
        this.pinotTableService = pinotTableService;
        initializeWatcher();
    }

    /**
     * Initialize the Kubernetes watcher for PinotTable resources
     */
    private void initializeWatcher() {
        try {
            tableWatcher = new Watcher<PinotTable>() {
                @Override
                public void eventReceived(Action action, PinotTable resource) {
                    handleTableEvent(action, resource);
                }

                @Override
                public void onClose(WatcherException cause) {
                    logger.error("PinotTable watcher closed", cause);
                    // Reinitialize watcher after delay
                    try {
                        Thread.sleep(5000);
                        initializeWatcher();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            // Start watching PinotTable resources across all namespaces
            kubernetesClient.resources(PinotTable.class)
                    .inAnyNamespace()
                    .watch(tableWatcher);

            logger.info("PinotTable watcher initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize PinotTable watcher", e);
        }
    }

    /**
     * Handle PinotTable resource events
     */
    private void handleTableEvent(Watcher.Action action, PinotTable resource) {
        String resourceKey = getResourceKey(resource);
        
        try {
            switch (action) {
                case ADDED:
                    handleTableAdded(resource);
                    break;
                case MODIFIED:
                    handleTableModified(resource);
                    break;
                case DELETED:
                    handleTableDeleted(resource);
                    break;
                case ERROR:
                    logger.error("Error event received for PinotTable resource: {}", resourceKey);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling PinotTable event: {} for resource: {}", action, resourceKey, e);
        }
    }

    /**
     * Handle PinotTable resource addition
     */
    private void handleTableAdded(PinotTable resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTable resource added: {}", resourceKey);
        
        managedTables.put(resourceKey, resource);
        pinotTableService.createOrUpdateTable(resource);
    }

    /**
     * Handle PinotTable resource modification
     */
    private void handleTableModified(PinotTable resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTable resource modified: {}", resourceKey);
        
        PinotTable existingResource = managedTables.get(resourceKey);
        if (existingResource != null) {
            managedTables.put(resourceKey, resource);
            pinotTableService.createOrUpdateTable(resource);
        }
    }

    /**
     * Handle PinotTable resource deletion
     */
    private void handleTableDeleted(PinotTable resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("PinotTable resource deleted: {}", resourceKey);
        
        managedTables.remove(resourceKey);
        pinotTableService.deleteTable(resource);
    }

    /**
     * Get a unique key for the resource
     */
    private String getResourceKey(PinotTable resource) {
        ObjectMeta metadata = resource.getMetadata();
        if (metadata != null && metadata.getNamespace() != null) {
            return metadata.getNamespace() + "/" + metadata.getName();
        }
        return metadata != null ? metadata.getName() : "unknown";
    }

    /**
     * Periodic reconciliation of managed tables
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reconcileTables() {
        logger.debug("Starting periodic reconciliation of {} managed tables", managedTables.size());
        
        for (PinotTable table : managedTables.values()) {
            try {
                pinotTableService.reconcileTable(table);
            } catch (Exception e) {
                logger.error("Error reconciling table: {}", getResourceKey(table), e);
            }
        }
    }

    /**
     * Get list of managed tables
     */
    public List<PinotTable> getManagedTables() {
        return List.copyOf(managedTables.values());
    }

    /**
     * Get a specific managed table
     */
    public PinotTable getManagedTable(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedTables.get(key);
    }

    /**
     * Check if a table is being managed
     */
    public boolean isTableManaged(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedTables.containsKey(key);
    }
}
