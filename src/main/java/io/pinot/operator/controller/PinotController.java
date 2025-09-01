package io.pinot.operator.controller;

import io.pinot.operator.api.Pinot;
import io.pinot.operator.service.PinotClusterService;
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
 * Main controller for managing Pinot clusters in Kubernetes
 * 
 * This controller watches for Pinot custom resources and manages
 * their lifecycle, including deployment, updates, and deletion.
 */
@Component
public class PinotController {

    private static final Logger logger = LoggerFactory.getLogger(PinotController.class);
    
    private final KubernetesClient kubernetesClient;
    private final PinotClusterService pinotClusterService;
    private final ConcurrentMap<String, Pinot> managedClusters = new ConcurrentHashMap<>();
    
    private Watcher<Pinot> pinotWatcher;

    @Autowired
    public PinotController(KubernetesClient kubernetesClient, PinotClusterService pinotClusterService) {
        this.kubernetesClient = kubernetesClient;
        this.pinotClusterService = pinotClusterService;
        initializeWatcher();
    }

    /**
     * Initialize the Kubernetes watcher for Pinot resources
     */
    private void initializeWatcher() {
        try {
            pinotWatcher = new Watcher<Pinot>() {
                @Override
                public void eventReceived(Action action, Pinot resource) {
                    handlePinotEvent(action, resource);
                }

                @Override
                public void onClose(WatcherException cause) {
                    logger.error("Pinot watcher closed", cause);
                    // Reinitialize watcher after delay
                    try {
                        Thread.sleep(5000);
                        initializeWatcher();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };

            // Start watching Pinot resources across all namespaces
            kubernetesClient.resources(Pinot.class)
                    .inAnyNamespace()
                    .watch(pinotWatcher);

            logger.info("Pinot watcher initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Pinot watcher", e);
        }
    }

    /**
     * Handle Pinot resource events
     */
    private void handlePinotEvent(Watcher.Action action, Pinot resource) {
        String resourceKey = getResourceKey(resource);
        
        try {
            switch (action) {
                case ADDED:
                    handlePinotAdded(resource);
                    break;
                case MODIFIED:
                    handlePinotModified(resource);
                    break;
                case DELETED:
                    handlePinotDeleted(resource);
                    break;
                case ERROR:
                    logger.error("Error event received for Pinot resource: {}", resourceKey);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error handling Pinot event: {} for resource: {}", action, resourceKey, e);
        }
    }

    /**
     * Handle Pinot resource addition
     */
    private void handlePinotAdded(Pinot resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("Pinot resource added: {}", resourceKey);
        
        managedClusters.put(resourceKey, resource);
        pinotClusterService.createOrUpdateCluster(resource);
    }

    /**
     * Handle Pinot resource modification
     */
    private void handlePinotModified(Pinot resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("Pinot resource modified: {}", resourceKey);
        
        Pinot existingResource = managedClusters.get(resourceKey);
        if (existingResource != null) {
            managedClusters.put(resourceKey, resource);
            pinotClusterService.createOrUpdateCluster(resource);
        }
    }

    /**
     * Handle Pinot resource deletion
     */
    private void handlePinotDeleted(Pinot resource) {
        String resourceKey = getResourceKey(resource);
        logger.info("Pinot resource deleted: {}", resourceKey);
        
        managedClusters.remove(resourceKey);
        pinotClusterService.deleteCluster(resource);
    }

    /**
     * Get a unique key for the resource
     */
    private String getResourceKey(Pinot resource) {
        ObjectMeta metadata = resource.getMetadata();
        if (metadata != null && metadata.getNamespace() != null) {
            return metadata.getNamespace() + "/" + metadata.getName();
        }
        return metadata != null ? metadata.getName() : "unknown";
    }

    /**
     * Periodic reconciliation of managed clusters
     */
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void reconcileClusters() {
        logger.debug("Starting periodic reconciliation of {} managed clusters", managedClusters.size());
        
        for (Pinot cluster : managedClusters.values()) {
            try {
                pinotClusterService.reconcileCluster(cluster);
            } catch (Exception e) {
                logger.error("Error reconciling cluster: {}", getResourceKey(cluster), e);
            }
        }
    }

    /**
     * Get list of managed clusters
     */
    public List<Pinot> getManagedClusters() {
        return List.copyOf(managedClusters.values());
    }

    /**
     * Get a specific managed cluster
     */
    public Pinot getManagedCluster(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedClusters.get(key);
    }

    /**
     * Check if a cluster is being managed
     */
    public boolean isClusterManaged(String namespace, String name) {
        String key = namespace + "/" + name;
        return managedClusters.containsKey(key);
    }
}
